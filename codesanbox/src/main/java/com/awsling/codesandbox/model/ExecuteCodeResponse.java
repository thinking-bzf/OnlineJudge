package com.awsling.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {
    /**
     * 代码执行结果
     */
    private List<String> outputList;

    /**
     * 接口返回的消息
     */
    private String message;

    /**
     * 代码执行状态
     */
    private Integer status;

    /**
     * 代码执行信息
     */
    private JudgeInfo judgeInfo;

}
