package com.awsling.smartcode.model.vo;

import cn.hutool.json.JSONUtil;
import com.awsling.smartcode.model.dto.questionsubmit.JudgeInfo;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class QuestionSubmitVO {
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 评测信息 json 对象
     */
    private JudgeInfo judgeInfo;

    /**
     * 评测状态(0-待判题，1-判题中，2-成功，3-失败)
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;


    private static final long serialVersionUID = 1L;


    /**
     * 包装类转对象
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        // 拷贝能复制的属性
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);

        // 处理VO类和实体类字段不一致的情况
        JudgeInfo judgeInfoObj = questionSubmitVO.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        // tags 的转换
        JudgeInfo judgeInfoObj = JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class);
        questionSubmitVO.setJudgeInfo(judgeInfoObj);
        return questionSubmitVO;
    }
}
