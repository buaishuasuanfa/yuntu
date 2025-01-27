package com.ljw.yuntubackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.mapper.UserMapper;
import com.ljw.yuntubackend.modal.dto.user.UserQueryRequest;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.enums.UserRoleEnum;
import com.ljw.yuntubackend.modal.vo.UserVO;
import com.ljw.yuntubackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ljw.yuntubackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author liujiawei
 * @since 2025-01-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final String DEFAULT_AVATAR = "https://ljw-bucket.oss-cn-hangzhou.aliyuncs.com/avatar/1881171093897687041/1881171093897687041.png";
    private final String NAME_PREFIX = "USER_";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserAvatar(DEFAULT_AVATAR);
        user.setUserName(NAME_PREFIX +userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "yun_tu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public User userLogin(String userAccount, String userPassword,HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        ThrowUtils.throwIf(userAccount.length()<4, ErrorCode.PARAMS_ERROR,"账号错误");
        ThrowUtils.throwIf(userPassword.length()<8, ErrorCode.PARAMS_ERROR,"密码错误");

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount);
        User user = this.getOne(queryWrapper);

        ThrowUtils.throwIf(user == null,ErrorCode.PARAMS_ERROR,"用户不存在");
        ThrowUtils.throwIf(!user.getUserPassword().equals(getEncryptPassword(userPassword)),ErrorCode.PARAMS_ERROR,"密码错误");

        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        return user;
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() ==null,ErrorCode.NOT_LOGIN_ERROR);

        return loginUser;
    }

    @Override
    public void logout(HttpServletRequest request) {
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() ==null,ErrorCode.NOT_LOGIN_ERROR);

        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "user_account", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "user_name", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean addUser(User user) {
        if (user.getUserName() == null){
            user.setUserName(NAME_PREFIX + user.getUserAccount());
        }
        if (user.getUserAvatar() == null){
            user.setUserAvatar(DEFAULT_AVATAR);
        }
        return this.saveOrUpdate(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}
