package com.ljw.yuntubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljw.yuntubackend.modal.dto.UserQueryRequest;
import com.ljw.yuntubackend.modal.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.yuntubackend.modal.vo.LoginUserVO;
import com.ljw.yuntubackend.modal.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author liujiawei
 * @since 2025-01-23
 */
public interface IUserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword,HttpServletRequest request);

    /**
     * 获取当前用户
     * @param request request
     * @return 当前登录用户
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户登出
     * @param request request
     */
    void logout(HttpServletRequest request);

    /**
     * 信息脱敏
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 信息脱敏
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构造查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 新增用户
     * @param user
     * @return
     */
    boolean addUser(User user);
}
