package com.awsling.codesandbox.utils;

import com.awsling.codesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * 进程工具类
 */
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {

        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程正常的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder outputStringBuilder = new StringBuilder();
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputStringBuilder.append(outputLine);
                }
                executeMessage.setMessage(outputStringBuilder.toString());
                bufferedReader.close();
            } else {
                System.out.println(opName + "失败，错误码:" + exitValue);
                // 分批获取进程正常的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder outputStringBuilder = new StringBuilder();
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputStringBuilder.append(outputLine);
                }
                executeMessage.setMessage(outputStringBuilder.toString());

                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorOutputStringBuilder = new StringBuilder();
                String errorOutputLine;
                while ((errorOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStringBuilder.append(errorOutputLine);
                }
                executeMessage.setErrMessage(errorOutputStringBuilder.toString());
                errorBufferedReader.close();
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            // 资源销毁
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName, String args) {

        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 向控制台输入
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

            // 需要加上回车
            outputStreamWriter.write(args + "\n");
            outputStreamWriter.flush();

            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            System.out.println(opName + "成功");
            // 分批获取进程正常的输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder outputStringBuilder = new StringBuilder();
            String outputLine;
            while ((outputLine = bufferedReader.readLine()) != null) {
                outputStringBuilder.append(outputLine);
            }
            executeMessage.setMessage(outputStringBuilder.toString());

            // 释放资源
            bufferedReader.close();
            outputStreamWriter.close();
            outputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }
}
