package com.awsling.smartcode.model.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题用例
 */
@NoArgsConstructor
@Data
public class JudgeCase {

    /**
     * 输入样例
     */
    @JsonProperty("input")

    private String input;

    /**
     * 输出样例
     */
    @JsonProperty("output")
    private String output;
}
