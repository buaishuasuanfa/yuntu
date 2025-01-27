package com.ljw.yuntubackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljw.yuntubackend.annotation.AuthCheck;
import com.ljw.yuntubackend.common.BaseResponse;
import com.ljw.yuntubackend.common.ResultUtils;
import com.ljw.yuntubackend.constant.UserConstant;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.modal.dto.space.SpaceAddRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceEditRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceQueryRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceUpdateRequest;
import com.ljw.yuntubackend.modal.entity.Space;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.enums.SpaceLevelEnum;
import com.ljw.yuntubackend.modal.vo.SpaceLevel;
import com.ljw.yuntubackend.modal.vo.SpaceVO;
import com.ljw.yuntubackend.service.SpaceService;
import com.ljw.yuntubackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author 刘佳伟
 * @date 2025/1/26 10:33
 * @Description 空间
 */
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    /**
     * 添加空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {

        User loginUser = userService.getCurrentUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest,loginUser);

        return ResultUtils.success(spaceId);
    }

    /**
     * 修改空间（当前仅允许修改空间名称）
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if ( spaceEditRequest.getSpaceId() <= 0 || spaceEditRequest.getSpaceName() == null || spaceEditRequest.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(loginUser.getId() != spaceEditRequest.getUserId(),ErrorCode.NO_AUTH_ERROR);

        Space space = BeanUtil.copyProperties(spaceEditRequest, Space.class);
        spaceService.validSpace(space,false);
        space.setEditTime(LocalDateTime.now());
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"修改空间失败");
        return ResultUtils.success(true);
    }

    /**
     * 修改空间信息（管理员）
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        if ( spaceUpdateRequest.getId() <= 0 || spaceUpdateRequest.getUserId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        Space space = BeanUtil.copyProperties(spaceUpdateRequest, Space.class);
        spaceService.validSpace(space,false);
        spaceService.fillSpaceBySpaceLevel(space);

        if (Objects.equals(space.getId(), loginUser.getId())){
            space.setEditTime(LocalDateTime.now());
        }
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"修改空间失败");
        return ResultUtils.success(true);
    }

    /**
     * 查询所有空间信息（管理员）
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> getSpaceList(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();

        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }


    /**
     * 根据id查询空间信息
     */
    @GetMapping
    public BaseResponse<SpaceVO> getSpaceById(HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        Space space = spaceService.getById(loginUser.getId());
        if (Objects.isNull(space)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该用户无任何空间");
        }
        SpaceVO spaceVO = BeanUtil.copyProperties(space, SpaceVO.class);
        return ResultUtils.success(spaceVO);
    }


    /**
     * 查询空间级别
     */
    @PostMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevelList() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).toList();
        return ResultUtils.success(spaceLevelList);
    }
}
