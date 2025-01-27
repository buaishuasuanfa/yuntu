package com.ljw.yuntubackend.modal.dto.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/26 11:04
 * @Description 空间删除类
 */
@Data
public class SpaceDeleteRequest implements Serializable {

    private Long id;
    private Long spaceId;
    /**
     * 管理员删除用户空间必须输入原因
     */
    private String reason;

    @Serial
    private static final long serialVersionUID = 1L;

}
