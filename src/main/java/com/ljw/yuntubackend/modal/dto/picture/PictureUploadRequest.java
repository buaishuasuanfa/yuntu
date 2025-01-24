package com.ljw.yuntubackend.modal.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/23 22:26
 * @Description
 */
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

