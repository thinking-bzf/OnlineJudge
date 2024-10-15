package com.awsling.smartcode.service;

import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.entity.User;
import com.awsling.smartcode.model.vo.QuestionSubmitVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目提交封装
     *
     * @param QuestionSubmitSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit QuestionSubmitSubmit, User loginUser);

    /**
     * 分页获取题目提交封装
     *
     * @param QuestionSubmitSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> QuestionSubmitSubmitPage, User loginUser);

}
