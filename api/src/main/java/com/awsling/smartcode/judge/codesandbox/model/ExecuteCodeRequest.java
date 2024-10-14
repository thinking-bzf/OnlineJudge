package com.awsling.smartcode.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 输入样例
     */
    private List<String> inputList;

    /**
     * 执行代码
     */
    private String code;

    /**
     * 代码的语言
     */
    private String language;


    /**
     * 代码执行时间限制（根据具体场景添加）
     */
    // private Integer timeLimit;
}
