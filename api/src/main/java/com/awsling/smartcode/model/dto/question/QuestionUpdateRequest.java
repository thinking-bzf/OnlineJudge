package com.awsling.smartcode.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 * 给管理员使用
 *
 * @author <a href="https://github.com/thinking-bzf">awsling</a>
 */
@Data
public class QuestionUpdateRequest implements Serializable {


    /**
     * id
     */
    private Long id;


    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 答案
     */
    private String answer;

    /**
     * 测试用例 json 数组
     */
    private JudgeCase judgeCase;

    /**
     * 评测配置 json 对象
     */
    private JudgeConfig judgeConfig;


    private static final long serialVersionUID = 1L;
}