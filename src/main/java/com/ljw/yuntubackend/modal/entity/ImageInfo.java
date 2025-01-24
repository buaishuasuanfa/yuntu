package com.ljw.yuntubackend.modal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 刘佳伟
 * @date 2025/1/24 10:50
 * @Description
 */
@Data
@AllArgsConstructor
public class ImageInfo {

    private int height;
    private int width;
    private double size;
    private String format;

}
