package com.awsling.smartcode.judge.codesandbox.impl;

import com.awsling.smartcode.judge.codesandbox.CodeSandbox;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱（实际业务使用）
 */
public class RemoteCodeSandbox implements CodeSandbox {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱执行代码");
        return null;
    }
}
