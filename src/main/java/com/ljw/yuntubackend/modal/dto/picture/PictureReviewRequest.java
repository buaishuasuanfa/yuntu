package com.ljw.yuntubackend.modal.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 刘佳伟
 * @date 2025/1/24  16:14
 * @Description 管理员审核请求封装类
 */
@Data
public class PictureReviewRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 状态：0-待审核, 1-通过, 2-拒绝  
     */  
    private Integer reviewStatus;  
  
    /**  
     * 审核信息  
     */  
    private String reviewMessage;  
  
  
    private static final long serialVersionUID = 1L;  
}
