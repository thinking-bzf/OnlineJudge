package com.awsling.smartcode.judge.strategy;

import com.awsling.smartcode.model.dto.question.JudgeCase;
import com.awsling.smartcode.model.dto.questionsubmit.JudgeInfo;
import com.awsling.smartcode.model.entity.Question;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCases;

    private Question question;

    private QuestionSubmit questionSubmit;

}
