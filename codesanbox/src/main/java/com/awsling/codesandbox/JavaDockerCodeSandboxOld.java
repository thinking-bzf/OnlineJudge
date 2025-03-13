package com.awsling.codesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.awsling.codesandbox.model.ExecuteCodeRequest;
import com.awsling.codesandbox.model.ExecuteCodeResponse;
import com.awsling.codesandbox.model.ExecuteMessage;
import com.awsling.codesandbox.model.JudgeInfo;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Deprecated
public class JavaDockerCodeSandboxOld extends JavaCodeSandboxTemplate {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    private static final String SECCOMP_CONFIG_PATH = "seccomp/profile.json";
    private static final Long TIME_OUT = 1200L;

    public static void main(String[] args) {
        JavaDockerCodeSandboxOld javaNativeCodeSandbox = new JavaDockerCodeSandboxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
        String code = ResourceUtil.readStr("testCode/simpleCompleteArgs/Main.java", StandardCharsets.UTF_8);
        // String code = ResourceUtil.readStr("UnsafeCode/TimeOutCode.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("Java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 2. 编译代码，得到 class 文件


        // 3. 创建容器，上传编译文件
        // 1) 拉取jdk镜像，需要做一下是否拉取的判断
        // 2) 配置内存、CPU等限制
        // 3) 将本地的文件同步到容器中，volume 绑定
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd
                    .exec(pullImageResultCallback)
                    .awaitCompletion();
        } catch (InterruptedException e) {
            System.out.println("拉取镜像失败");
            throw new RuntimeException(e);
        }
        System.out.println("下载完成");

        // seccomp 配置
        String profileConfig = ResourceUtil.readUtf8Str(SECCOMP_CONFIG_PATH);
        // docker 配置
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L); // 内存限制
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L); // cpu 限制
        hostConfig.withReadonlyRootfs(true);
        hostConfig.withSecurityOpts(Arrays.asList("seccomp=" + profileConfig));
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withNetworkDisabled(true)
                .withTty(true) // 交互式终端
                .exec();
        System.out.println(createContainerResponse);


        // 4. 运行容器 id，并获取输出结果
        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String[] inputArray = input.split(" ");
            // 在容器中创建命令
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main",}, inputArray);
            ExecCreateCmdResponse createCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();

            // 创建一个回调，获取命令的输出，将输出放在 response 中
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errMessage = {null};
            ResultCallback.Adapter<Frame> startCmdCallback = new ResultCallback.Adapter<Frame>() {


                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + new String(frame.getPayload()));

                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + new String(frame.getPayload()));
                    }
                    super.onNext(frame);
                }
            };
            // 获取容器状态，从中获取到内存占用
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            // 内存监听一般都是间隔一段时间进行获取的，取这一段时间内最大值即可。
            final Long[] maxMemory = {0L};
            ResultCallback.Adapter<Statistics> statsCmdCallback = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    Long memoryUsage = statistics.getMemoryStats().getUsage();
                    maxMemory[0] = Math.max(memoryUsage != null ? memoryUsage : 0L, maxMemory[0]);
                }
            };
            statsCmd.exec(statsCmdCallback);
            // 开始执行命令
            long time;
            String cmdId = createCmdResponse.getId();
            try {
                // 获取执行该程序的时间（当前输出样例）
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                dockerClient.execStartCmd(cmdId)
                        .exec(startCmdCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            // 收集当前输入样例的结果信息
            executeMessage.setMessage(message[0]);
            executeMessage.setErrMessage(errMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }

        // 4. 收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        // 获取最大时间，方便判题
        long maxTime = 0;
        long maxMemory = 0;
        List<String> outputList = new ArrayList<>();
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (!StrUtil.isBlank(executeMessage.getErrMessage())) {
                executeCodeResponse.setMessage(executeMessage.getErrMessage());
                // 用户运行代码错误
                executeCodeResponse.setStatus(3);
                break;
            }
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }

            outputList.add(executeMessage.getMessage());
        }
        // 获取的输出列表大小和执行信息的大小相同，则表示成功
        if (executeMessageList.size() == outputList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        // 设置运行时间
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // 暂不实现，需要借助其他工具
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        // 5. 文件清理，释放空间

        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }

        return executeCodeResponse;
    }

    public ExecuteMessage getExecuteMessage(Throwable e) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        executeMessage.setErrMessage(e.getMessage());
        return executeMessage;
    }

    public ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
