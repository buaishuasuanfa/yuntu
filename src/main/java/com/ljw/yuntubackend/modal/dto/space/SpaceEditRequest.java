package com.ljw.yuntubackend.modal.dto.space;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/26 10:43
 * @Description
 */
@Data
public class SpaceEditRequest implements Serializable {

    private Long spaceId;
    private String spaceName;
    private Long userId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}


