package com.awsling.codesandbox.model;

import lombok.Data;

@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private String errMessage;

    private Long time;

    private Long memory;

}
