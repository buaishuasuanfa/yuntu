package com.ljw.yuntubackend.modal.dto;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户注册
 *
 * @author liujiawei
 * @since 2025-01-23
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账号")
    private String userAccount;

    @ApiModelProperty(value = "密码")
    private String userPassword;

    @ApiModelProperty(value = "确认密码")
    private String checkUserPassword;
}
