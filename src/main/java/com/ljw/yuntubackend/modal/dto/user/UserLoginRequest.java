package com.ljw.yuntubackend.modal.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * 用户登录
 * @author 刘佳伟
 * @date 2025/1/23 19:34
 * @Description
 */
@Data
public class UserLoginRequest implements Serializable {
    @ApiModelProperty(value = "账号")
    private String userAccount;
    @ApiModelProperty(value = "密码")
    private String userPassword;
}
