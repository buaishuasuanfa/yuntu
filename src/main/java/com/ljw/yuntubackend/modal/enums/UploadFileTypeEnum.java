package com.ljw.yuntubackend.modal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 刘佳伟
 * @date 2025/1/23 21:34
 * @Description
 */
@Getter
@AllArgsConstructor
public enum UploadFileTypeEnum {

    IMAGE("image",0);

    private final String type;
    private final Integer code;

}