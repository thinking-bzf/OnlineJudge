package com.awsling.smartcode.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.awsling.smartcode.model.dto.question.JudgeCase;
import com.awsling.smartcode.model.dto.question.JudgeConfig;
import com.awsling.smartcode.model.dto.questionsubmit.JudgeInfo;
import com.awsling.smartcode.model.entity.Question;
import com.awsling.smartcode.model.enums.JudgeInfoMessageEnum;

import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy {
    /**
     * 默认的判题策略
     *
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {

        // 获取上下文
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long execMemory = judgeInfo.getMemory();
        Long execTime = judgeInfo.getTime();

        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        List<JudgeCase> judgeCases = judgeContext.getJudgeCases();
        Question question = judgeContext.getQuestion();

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(execTime);
        judgeInfoResponse.setMemory(execMemory);

        // 判断结果是否正确
        if (outputList.size() != inputList.size()) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < outputList.size(); i++) {
            if (!outputList.get(i).equals(judgeCases.get(i).getOutput())) {
                judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                return judgeInfoResponse;
            }
        }

        // 判断时空限制是否满足
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long timeLimit = judgeConfig.getTimeLimit();
        Long memoryLimit = judgeConfig.getMemoryLimit();

        if (timeLimit < execTime) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        if (memoryLimit < execMemory) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        return judgeInfoResponse;
    }
}
