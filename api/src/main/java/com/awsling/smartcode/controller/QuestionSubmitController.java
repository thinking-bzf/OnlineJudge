package com.awsling.smartcode.controller;

import com.awsling.smartcode.common.BaseResponse;
import com.awsling.smartcode.common.ErrorCode;
import com.awsling.smartcode.common.ResultUtils;
import com.awsling.smartcode.exception.BusinessException;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.entity.User;
import com.awsling.smartcode.model.vo.QuestionSubmitVO;
import com.awsling.smartcode.service.QuestionSubmitService;
import com.awsling.smartcode.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/thinking-bzf">awsling</a>
 */
@RestController
// @RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    // @Resource
    // private QuestionSubmitService questionSubmitService;
    //
    // @Resource
    // private UserService userService;
    //
    // /**
    //  * 提交题目
    //  *
    //  * @param questionSubmitAddRequest
    //  * @param request
    //  * @return
    //  */
    // @PostMapping("/")
    // public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
    //     if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
    //         throw new BusinessException(ErrorCode.PARAMS_ERROR);
    //     }
    //     // 登录才能点赞
    //     final User loginUser = userService.getLoginUser(request);
    //     long questionId = questionSubmitAddRequest.getQuestionId();
    //     Long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
    //     return ResultUtils.success(result);
    // }
    //
    //
    // /**
    //  * 分页获取题目提交列表（除了管理员外，普通用户智能看到非答案，提交代码等空开信息）
    //  *
    //  * @param questionSubmitQueryRequest
    //  * @return
    //  */
    // @PostMapping("/list/page")
    // public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
    //                                                                      HttpServletRequest request) {
    //     long current = questionSubmitQueryRequest.getCurrent();
    //     long size = questionSubmitQueryRequest.getPageSize();
    //     // 从数据库中直接查询分页信息
    //     Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
    //             questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
    //
    //     User loginUser = userService.getLoginUser(request);
    //     // 返回脱敏信息，传入request是为了获取当前登录用户信息
    //     return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    // }

}
