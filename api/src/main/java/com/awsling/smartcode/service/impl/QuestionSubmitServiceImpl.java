package com.awsling.smartcode.service.impl;

import com.awsling.smartcode.common.ErrorCode;
import com.awsling.smartcode.exception.BusinessException;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.awsling.smartcode.model.entity.Question;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.entity.User;
import com.awsling.smartcode.model.enums.QuestionSubmitEnum;
import com.awsling.smartcode.service.QuestionService;
import com.awsling.smartcode.service.QuestionSubmitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.service.QuestionSubmitService;
import com.awsling.smartcode.mapper.QuestionSubmitMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author awsling
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2024-09-25 14:56:22
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return 提交记录ID
     */
    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionSubmitAddRequest.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // TODO 判断编程语言是否合理

        // 是否已题目提交
        long userId = loginUser.getId();
        // 每个用户串行题目提交
        // TODO 优化
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionSubmitAddRequest.getQuestionId());
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());

        // TODO 设置初始状态
        questionSubmit.setStatus(QuestionSubmitEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交题目失败");
        }

        return questionSubmit.getId();
    }

}




