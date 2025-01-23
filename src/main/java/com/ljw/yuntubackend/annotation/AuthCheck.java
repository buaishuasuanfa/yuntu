package com.ljw.yuntubackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 刘佳伟
 * @date 2025/1/23 20:10
 * @Description
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须拥有某个角色
     */
    String mustRole() default "";

}
