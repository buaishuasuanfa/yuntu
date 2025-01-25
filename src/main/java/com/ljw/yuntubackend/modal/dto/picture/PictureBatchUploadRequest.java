package com.ljw.yuntubackend.modal.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/24 19:34
 * @Description 批量获取图片
 */
@Data
public class PictureBatchUploadRequest implements Serializable {

    private String searchText;
    private int size = 10;

}
