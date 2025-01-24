package com.ljw.yuntubackend.modal.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 刘佳伟
 * @date 2025/1/24 14:16
 * @Description 修改图片请求类（用户）
 */
@Data
public class PictureEditRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;
  
    private static final long serialVersionUID = 1L;  
}
