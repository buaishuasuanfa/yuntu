package com.ljw.yuntubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.yuntubackend.modal.dto.picture.PictureQueryRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceAddRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceQueryRequest;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.Space;
import com.ljw.yuntubackend.modal.entity.User;

/**
* @author 17404
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-01-26 10:30:27
*/
public interface SpaceService extends IService<Space> {

    /**
     * 校验数据
     * @param space
     * @param add
     */
    void validSpace(Space space,boolean add);

    /**
     * 构造查询条件
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据等级自动填充最大数与大小
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 新增空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

}
