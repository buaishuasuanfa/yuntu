package com.ljw.yuntubackend.common;

import com.ljw.yuntubackend.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/15 18:19
 * @Description 全局响应封装类
 */
@Data
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {

    private int code;
    private T data;
    private String message;


    public BaseResponse(T data, int code) {
        this(code,data,"");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
