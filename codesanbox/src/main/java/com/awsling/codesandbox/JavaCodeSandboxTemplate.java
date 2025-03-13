package com.awsling.codesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.awsling.codesandbox.model.ExecuteCodeRequest;
import com.awsling.codesandbox.model.ExecuteCodeResponse;
import com.awsling.codesandbox.model.ExecuteMessage;
import com.awsling.codesandbox.model.JudgeInfo;
import com.awsling.codesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {


    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    /**
     * 1. 将用户代码保存到文件中
     *
     * @param code 用户代码
     * @return 代码文件
     */
    public File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        // 判断全局代码目录是否存在，如果不存在，则创建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 2. 编译代码文件
     *
     * @param userCodeFile 用户代码文件
     * @return 编译信息
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());

        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
            // 编译失败
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            return getExecuteMessage(e);
        }
    }

    /**
     * @param inputList    输入样例
     * @param userCodeFile 用户代码文件
     * @return
     */
    public List<ExecuteMessage> runCode(List<String> inputList, File userCodeFile) {
        // 下面实现具体逻辑
        return new ArrayList<>();
    }

    /**
     * 4. 获取输出结果
     *
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
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
        // 设置判题信息（时间和内存）
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }

    /**
     * 5. 文件清理，释放空间
     *
     * @param userCodeFile 用户文件
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
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

    public ExecuteMessage getExecuteMessage(Throwable e) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        executeMessage.setExitValue(1);
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
