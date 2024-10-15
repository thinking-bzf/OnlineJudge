package com.awsling.smartcode.judge;

public interface JudgeService {

    /**
     * 执行判题，根据提交的问题ID进行判题
     *
     * @param questionSubmitId 提交id
     * @return 题目提交后的实体
     */
    void doJudge(long questionSubmitId);

}
