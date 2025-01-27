package com.ljw.yuntubackend.modal.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/25 18:20
 * @Description
 */
@Data
public class PictureDeleteRequest implements Serializable {

    private String id;
    private String uploadPath;
    private Long userId;

}
