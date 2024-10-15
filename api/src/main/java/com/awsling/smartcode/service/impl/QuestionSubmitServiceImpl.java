package com.awsling.smartcode.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.awsling.smartcode.common.ErrorCode;
import com.awsling.smartcode.constant.CommonConstant;
import com.awsling.smartcode.exception.BusinessException;
import com.awsling.smartcode.judge.JudgeService;
import com.awsling.smartcode.mapper.QuestionSubmitMapper;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.awsling.smartcode.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.awsling.smartcode.model.entity.Question;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.entity.User;
import com.awsling.smartcode.model.enums.QuestionSubmitLanguageEnum;
import com.awsling.smartcode.model.enums.QuestionSubmitStatusEnum;
import com.awsling.smartcode.model.vo.QuestionSubmitVO;
import com.awsling.smartcode.service.QuestionService;
import com.awsling.smartcode.service.QuestionSubmitService;
import com.awsling.smartcode.service.UserService;
import com.awsling.smartcode.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

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
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(questionSubmitAddRequest.getLanguage());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不合法");
        }
        if (StringUtils.isBlank(questionSubmitAddRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码不能为空");
        }
        long userId = loginUser.getId();


        // 每个用户串行题目提交
        // TODO 优化
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionSubmitAddRequest.getQuestionId());
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());

        //  设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交题目失败");
        }
        // 执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        return questionSubmitId;
    }

    /**
     * 获取查询包装类，需要知道用户会根据哪些字段查询。
     * 根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        // 从请求对象中获取查询条件
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(language), "title", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 获取封装类
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看到自己（userId 和 loginUser 不同）提交的代码的代码
        long userId = loginUser.getId();
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }

        return questionSubmitVO;
    }

    /**
     * 分页查询提交信息
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }

        // 涉及了多次用户信息的查询
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());

        // // 1. 关联查询用户信息（分页）
        // // 先将提交信息中的 userId 收集起来，放在集合中，然后根据id集合获取对应的用户信息
        // Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        // Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
        //         .collect(Collectors.groupingBy(User::getId));
        //
        // // 填充信息
        // // 如果map中存在当前提交信息的 userId，那么将对应的用户信息填充到对应属性中（此时user信息已经在内存中了）
        // List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
        //     QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        //     Long userId = questionSubmit.getUserId();
        //     User user = null;
        //     if (userIdUserListMap.containsKey(userId)) {
        //         user = userIdUserListMap.get(userId).get(0);
        //     }
        //     questionSubmitVO.setUserVO(userService.getUserVO(user));
        //     return questionSubmitVO;
        // }).collect(Collectors.toList());

        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

}




