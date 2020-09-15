package com.example.demo.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILURE = 500;

    public static final String MESSAGE_SUCCESS = "成功";
    public static final String MESSAGE_FAILURE = "失败";
    /**
     * 编码：0表示成功，其他值表示失败
     */
    private int code ;
    /**
     * 消息内容
     */
    private String msg ;
    /**
     * 响应数据
     */
    private T data;

    public Result(T data) {
        this.data = data;
    }
    public Result() {
    }
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, MESSAGE_SUCCESS, data);
    }
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }
    public static <T> Result<T> failure(T data) {
        return new Result<>(CODE_FAILURE, MESSAGE_FAILURE, data);
    }
    public Result<T> error(String msg) {
        this.code = 500;
        this.msg = msg;
        return this;
    }
    public Result<T> error(int code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }
    public Result<T> error(int code) {
        this.code = code;
        this.msg = MESSAGE_FAILURE;
        return this;
    }
    public static <T> Result<T> failure(String message, T data) {
        return new Result<>(CODE_FAILURE, message, data);
    }
    public static <T> Result<T> response(int status, String message, T data) {
        return new Result<>(status, message, data);
    }
}
