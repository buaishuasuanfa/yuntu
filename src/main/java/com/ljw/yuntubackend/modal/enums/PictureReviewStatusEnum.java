package com.ljw.yuntubackend.modal.enums;

import lombok.Data;
import lombok.Getter;

/**
 * @author 刘佳伟
 * @date 2025/1/24 16:10
 * @Description
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEW("待审核",0),
    PASS("通过",1),
    REJECT("拒绝",2);

    private final String text;
    private final int code;
    PictureReviewStatusEnum(String text, int code) {
        this.text = text;
        this.code = code;
    }
    /**
     * 根据 code 获取枚举
     */
    public static PictureReviewStatusEnum getEnumByCode(int code) {
        PictureReviewStatusEnum[] values = PictureReviewStatusEnum.values();
        for (PictureReviewStatusEnum value : values) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }

}
