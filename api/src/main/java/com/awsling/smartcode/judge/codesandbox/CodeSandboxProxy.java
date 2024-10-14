package com.awsling.smartcode.judge.codesandbox;

import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

    private final CodeSandbox codeSandbox;

    /**
     * 必须传入原本的代码沙箱实例
     *
     * @param codeSandbox
     */
    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息:" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("代码沙箱响应信息:" + executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
