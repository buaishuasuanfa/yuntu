package com.ljw.yuntubackend.modal.dto.space;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/26 10:36
 * @Description 新增空间请求类
 */
@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
