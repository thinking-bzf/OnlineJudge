package com.awsling.smartcode.judge;

import cn.hutool.json.JSONUtil;
import com.awsling.smartcode.common.ErrorCode;
import com.awsling.smartcode.exception.BusinessException;
import com.awsling.smartcode.judge.codesandbox.CodeSandbox;
import com.awsling.smartcode.judge.codesandbox.CodeSandboxFactory;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;
import com.awsling.smartcode.judge.strategy.JudgeContext;
import com.awsling.smartcode.model.dto.question.JudgeCase;
import com.awsling.smartcode.model.dto.questionsubmit.JudgeInfo;
import com.awsling.smartcode.model.entity.Question;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import com.awsling.smartcode.model.enums.QuestionSubmitStatusEnum;
import com.awsling.smartcode.service.QuestionService;
import com.awsling.smartcode.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    QuestionService questionService;

    @Resource
    QuestionSubmitService questionSubmitService;


    @Resource
    JudgeManager judgeManager;

    @Value("${codesandbox.type}")
    private String type;


    /**
     * 执行判题，根据提交的问题ID进行判题
     *
     * @param questionSubmitId 题目提交 id
     * @return
     */
    public void doJudge(long questionSubmitId) {
        // 1. 传入题目的提交 id，获取对应的题目、提交信息（包括代码、编程语言）
        // 检查提交是否为空，题目是否为空
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2. 如果题目提交状态不为等待中，就不用重复执行
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3. 更改判题（题目提交）的状态为 "判题中"，防止重复执行，也让用户能够后即使看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());

        questionSubmitService.updateById(questionSubmitUpdate);
        // 4. 调用沙箱，获取到执行结果
        // 获取代码、语言、测试用例
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCases = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCases.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        // 调用代码沙箱
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();

        JudgeInfo execjudgeInfo = executeCodeResponse.getJudgeInfo();

        // 5. 根据沙箱的执行结果，设置判题的状态和信息
        // 设置上下文
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(execjudgeInfo);
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCases(judgeCases);
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeContext.setQuestion(question);

        // 根据题目信息和提交信息完成策略选择，并执行
        JudgeInfo judgeInfoResult = judgeManager.doJudge(judgeContext);

        // 6. 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setJudgeInfo(judgeInfoResult.toString());
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);

        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }
    }
}
