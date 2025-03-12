package com.awsling.smartcode.judge.codesandbox.impl;

import com.awsling.smartcode.judge.codesandbox.CodeSandbox;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;
import com.awsling.smartcode.judge.codesandbox.model.JudgeInfo;
import com.awsling.smartcode.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱
 */
public class ExampleCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        executeCodeResponse.setMessage("代码执行成功");
        executeCodeResponse.setOutputList(inputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("代码执行成功");
        judgeInfo.setTime(1000L);
        judgeInfo.setMemory(1000L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
