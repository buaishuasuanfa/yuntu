package com.ljw.yuntubackend.aop;

import com.ljw.yuntubackend.annotation.AuthCheck;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.enums.UserRoleEnum;
import com.ljw.yuntubackend.service.IUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;

import static com.ljw.yuntubackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 刘佳伟
 * @date 2025/1/23 20:21
 * @Description
 */
@Aspect
@Component
public class AuthAOP {

    @Resource
    private IUserService userService;

    @Around("@annotation(authCheck)")
    public Object authCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前用户
        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null|| user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        // 获取需要的权限
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限
        if (enumByValue == null) {
            return joinPoint.proceed();
        }
        // 需要权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        ThrowUtils.throwIf(userRoleEnum == null,ErrorCode.NO_AUTH_ERROR);

        // 无管理员权限
        if (UserRoleEnum.ADMIN.equals(enumByValue) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 无VIP权限
        if (UserRoleEnum.VIP.equals(enumByValue) && !UserRoleEnum.VIP.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }

}
