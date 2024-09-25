package com.awsling.smartcode.service;

import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author awsling
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2024-09-25 14:56:22
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 帖子题目提交（内部服务）
     *
     * @param userId
     * @param questionId
     * @return
     */
    // int doQuestionSubmitInner(long userId, long questionId);

}
