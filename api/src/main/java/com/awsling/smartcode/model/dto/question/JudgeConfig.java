package com.awsling.smartcode.model.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测配置
 */
@NoArgsConstructor
@Data
public class JudgeConfig {

    /**
     * 时间限制（ms）
     */
    @JsonProperty("timeLimit")
    private Long timeLimit;

    /**
     * 内存限制（KB）
     */
    @JsonProperty("memoryLimit")
    private Long memoryLimit;
    /**
     * 堆栈限制（KB）
     */
    @JsonProperty("stackLimit")
    private Long stackLimit;
}
