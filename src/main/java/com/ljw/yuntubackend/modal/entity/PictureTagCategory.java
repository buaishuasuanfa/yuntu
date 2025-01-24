package com.ljw.yuntubackend.modal.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 刘佳伟
 * @date 2025/1/24 15:44
 * @Description
 */
@Data
public class PictureTagCategory implements Serializable {

    private List<String> tagList;
    private List<String> categoryList;

}
