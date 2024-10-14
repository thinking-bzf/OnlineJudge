package com.awsling.smartcode.judge.codesandbox.impl;

import com.awsling.smartcode.judge.codesandbox.CodeSandbox;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 集成第三方代码沙箱
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方沙箱执行代码");
        return null;
    }
}
