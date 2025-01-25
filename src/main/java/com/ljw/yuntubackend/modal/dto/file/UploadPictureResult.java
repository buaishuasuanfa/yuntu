package com.ljw.yuntubackend.modal.dto.file;

import lombok.Data;

/**
 * @author 刘佳伟
 * @date 2025/1/23 22:29
 * @Description
 */
@Data
public class UploadPictureResult {  
  
    /**  
     * 图片地址  
     */  
    private String url;

    /**
     * 图片 Key
     */
    private String uploadPath;

    /**  
     * 图片名称  
     */  
    private String picName;  
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private int picWidth;  
  
    /**  
     * 图片高度  
     */  
    private int picHeight;  
  
    /**  
     * 图片宽高比  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;  
  
}
