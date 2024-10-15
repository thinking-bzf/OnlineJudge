package com.awsling.smartcode.judge;

import com.awsling.smartcode.judge.strategy.DefaultJudgeStrategy;
import com.awsling.smartcode.judge.strategy.JavaJudgeStrategy;
import com.awsling.smartcode.judge.strategy.JudgeContext;
import com.awsling.smartcode.judge.strategy.JudgeStrategy;
import com.awsling.smartcode.model.dto.questionsubmit.JudgeInfo;
import com.awsling.smartcode.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题策略管理（简化调用方调用方式）
 */
@Service
public class JudgeManager {

    /**
     * 根据提交信息和题目进行策略切换
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);

    }
}
