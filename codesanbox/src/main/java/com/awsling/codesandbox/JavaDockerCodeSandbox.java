package com.awsling.codesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.awsling.codesandbox.model.ExecuteCodeRequest;
import com.awsling.codesandbox.model.ExecuteCodeResponse;
import com.awsling.codesandbox.model.ExecuteMessage;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final String SECCOMP_CONFIG_PATH = "seccomp/profile.json";
    private static final String JDK_IMAGE_NAME = "openjdk:8-alpine";
    private static final DockerClient dockerClient;
    private static final Long TIME_OUT = 1200L;


    // docker 配置
    static {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    @Override
    public List<ExecuteMessage> runCode(List<String> inputList, File userCodeFile) {
        // 1. 创建容器
        String containerId = createContainer(userCodeFile.getParentFile().getAbsolutePath());

        // 2. 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 获取容器状态，从中获取到内存占用
        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        // Docker stats 命令的更新频率由 Docker daemon 决定
        // 一般都是间隔 1s 统计一次，取统计时段中的最大值即可。
        final Long[] maxMemory = {0L};
        ResultCallback.Adapter<Statistics> statsCmdCallback = new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                log.info("memory usage: {}", statistics.getMemoryStats().getUsage());
                Long memoryUsage = statistics.getMemoryStats().getUsage();
                maxMemory[0] = Math.max(memoryUsage != null ? memoryUsage : 0L, maxMemory[0]);
            }
        };
        statsCmd.exec(statsCmdCallback);

        // 3. 计算所有的测试样例
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String input : inputList) {
            // 构造容器命令
            String[] inputArray = input.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main",}, inputArray);
            ExecCreateCmdResponse createCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            String cmdId = createCmdResponse.getId();
            // 在容器中执行命令
            ExecuteMessage executeMessage = executeContainerCmd(containerId, cmdId);
            executeMessage.setMemory(maxMemory[0] / 1024);
            executeMessageList.add(executeMessage);
        }
        try {
            // 由于 Docker 监听状态为1s，所以先睡 1s，确保内存占用已经被获取，在关闭监听6流
            Thread.sleep(1000);
            statsCmdCallback.close();
        } catch (IOException e) {
            log.error("stats cmd callback 关闭失败", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            statsCmd.close();
        }
        return executeMessageList;
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2. 编译代码，得到 class 文件
        ExecuteMessage compileMessage = compileFile(userCodeFile);
        System.out.println(compileMessage);
        if (compileMessage.getExitValue() != 0) {
            // 编译失败
            return getErrorResponse(new RuntimeException("编译错误"));
        }

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runCode(inputList, userCodeFile);

        // 4. 整理输出，
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

        // 5. 删除文件，释放资源
        boolean del = deleteFile(userCodeFile);
        if (!del) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }

        return outputResponse;
    }

    private boolean checkImageExist() {
        List<Image> images = dockerClient.listImagesCmd()
                .withImageNameFilter(JDK_IMAGE_NAME)
                .exec();
        if (images != null && !images.isEmpty()) {
            log.info("镜像已存在");
            return true;
        }
        return false;
    }

    /**
     * 3.1 拉取jdk镜像，创建容器，并将编译文件上传
     *
     * @param userCodeParentPath 用户文件
     * @return containerId
     */
    private String createContainer(String userCodeParentPath) {

        // 3. 创建容器，上传编译文件
        // 1) 拉取jdk镜像，需要做一下是否拉取的判断
        // 2) 配置内存、CPU等限制
        // 3) 将本地的文件同步到容器中，volume 绑定
        // 最后返回容器id
        if (!checkImageExist()) {

            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JDK_IMAGE_NAME);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    log.info("下载镜像：{}", item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                log.error("拉取镜像失败", e);
                throw new RuntimeException(e);
            }
            log.info("拉取镜像成功");
        }

        // 容器配置
        // seccomp 配置
        String profileConfig = ResourceUtil.readUtf8Str(SECCOMP_CONFIG_PATH);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L); // 内存限制
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L); // cpu 限制
        hostConfig.withReadonlyRootfs(true);
        hostConfig.withSecurityOpts(Collections.singletonList("seccomp=" + profileConfig));
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(JDK_IMAGE_NAME);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withNetworkDisabled(true)
                .withTty(true) // 交互式终端
                .exec();
        return createContainerResponse.getId();
    }

    /**
     * 3.3 在容器中执行指定的命令，并统计相关运行信息
     *
     * @param containerId
     * @param cmdId
     * @return
     */
    private ExecuteMessage executeContainerCmd(String containerId, String cmdId) {
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
                    log.info("输出错误结果：{}", new String(frame.getPayload()));
                } else {
                    message[0] = new String(frame.getPayload()).trim();
                    log.info("输出结果：{}", new String(frame.getPayload()));
                }
                super.onNext(frame);
            }
        };

        // 开始执行命令
        long time;
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
            log.error("程序执行异常", e);
            throw new RuntimeException(e);
        }
        // 收集当前输入样例的结果信息
        executeMessage.setMessage(message[0]);
        executeMessage.setErrMessage(errMessage[0]);
        executeMessage.setTime(time);
        // 结束之后关闭统计信息
        return executeMessage;
    }


}
