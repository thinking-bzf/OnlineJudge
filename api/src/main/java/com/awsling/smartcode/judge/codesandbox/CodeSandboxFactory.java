package com.awsling.smartcode.judge.codesandbox;

import com.awsling.smartcode.judge.codesandbox.impl.ExampleCodeSandbox;
import com.awsling.smartcode.judge.codesandbox.impl.RemoteCodeSandbox;
import com.awsling.smartcode.judge.codesandbox.impl.ThirdPartyCodeSandbox;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeRequest;
import com.awsling.smartcode.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱工厂（根据字符串参数创建指定类型的代码沙箱）
 */
public class CodeSandboxFactory {

    /**
     * 创建代码沙箱实例
     * @param type 代码沙箱类型
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
