package com.awsling.smartcode.model.dto.questionsubmit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题信息
 */
@NoArgsConstructor
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 时间使用（ms）
     */
    @JsonProperty("time")
    private Long time;
    /**
     * 内存使用（KB）
     */
    @JsonProperty("memory")
    private Long memory;

}
