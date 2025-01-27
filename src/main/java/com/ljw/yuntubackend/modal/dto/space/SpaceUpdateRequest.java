package com.ljw.yuntubackend.modal.dto.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/26 10:51
 * @Description 管理员修改空间类
 */
@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间原用户
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    @Serial
    private static final long serialVersionUID = 1L;
}

