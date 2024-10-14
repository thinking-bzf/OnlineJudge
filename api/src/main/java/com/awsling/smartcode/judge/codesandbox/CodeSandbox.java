package com.awsling.smartcode.judge.codesandbox;

import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
