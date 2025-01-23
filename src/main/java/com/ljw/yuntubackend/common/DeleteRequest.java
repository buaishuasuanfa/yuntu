package com.ljw.yuntubackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 自定义删除包装类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
