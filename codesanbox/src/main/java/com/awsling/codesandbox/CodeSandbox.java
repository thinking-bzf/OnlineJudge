package com.awsling.codesandbox;


import com.awsling.codesandbox.model.ExecuteCodeRequest;
import com.awsling.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
