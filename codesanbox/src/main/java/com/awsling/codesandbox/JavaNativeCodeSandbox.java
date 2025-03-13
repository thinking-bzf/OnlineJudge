package com.awsling.codesandbox;

import com.awsling.codesandbox.model.ExecuteCodeRequest;
import com.awsling.codesandbox.model.ExecuteCodeResponse;
import com.awsling.codesandbox.model.ExecuteMessage;
import com.awsling.codesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {

    private static final Long TIME_OUT = 1200L;

    /**
     * @param inputList    输入样例
     * @param userCodeFile 用户代码文件
     * @return
     */
    @Override
    public List<ExecuteMessage> runCode(List<String> inputList, File userCodeFile) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            // String runCmd = String.format("java -Dfile.encoding=utf-8 -cp %s:%s -Djava.security.manager=DenySecurityManager Main %s", SECURITY_MANAGER_PATH, userCodeParentPath, inputArgs);
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            String runCmd = String.format("java -Dfile.encoding=utf-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制，起守护线程
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        if (runProcess.isAlive()) {
                            System.out.println("超时了");
                            runProcess.destroy();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException("执行错误", e);
            }
        }
        return executeMessageList;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

}
