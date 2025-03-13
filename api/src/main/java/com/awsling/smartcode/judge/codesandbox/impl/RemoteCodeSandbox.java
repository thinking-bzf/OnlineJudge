package com.awsling.smartcode.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.awsling.smartcode.common.ErrorCode;
import com.awsling.smartcode.exception.BusinessException;
import com.awsling.smartcode.judge.codesandbox.CodeSandbox;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱（实际业务使用）
 */
public class RemoteCodeSandbox implements CodeSandbox {

    // 鉴权请求头
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱执行代码");
        String url = "http://localhost:8081/execute-code";
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(jsonStr)
                .execute()
                .body();

        if (StringUtils.isBlank(responseStr)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
