package com.ljw.yuntubackend.modal.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 刘佳伟
 * @date 2025/1/23 19:43
 * @Description
 */
@Data
public class LoginUserVO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户昵称")
    private String userName;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    @ApiModelProperty(value = "用户角色：user/admin/vip")
    private String userRole;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "会员过期时间")
    private LocalDateTime vipExpireTime;

    @ApiModelProperty(value = "会员编号")
    private Long vipNumber;

}
