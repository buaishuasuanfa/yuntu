package com.ljw.yuntubackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.modal.dto.space.SpaceAddRequest;
import com.ljw.yuntubackend.modal.dto.space.SpaceQueryRequest;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.Space;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.enums.SpaceLevelEnum;
import com.ljw.yuntubackend.service.SpaceService;
import com.ljw.yuntubackend.mapper.SpaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author 17404
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-01-26 10:30:27
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private TransactionTemplate transactionTemplate;

    Map<Long ,Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();

        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(id),"id", id);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(userId),"user_id", userId);
        spaceQueryWrapper.eq(StrUtil.isNotBlank(spaceName),"space_name", spaceName);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel),"space_level", spaceLevel);
        // 排序
        spaceQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return spaceQueryWrapper;
    }


    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 新增空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        String SPACE_NAME_PREFIX = "SPACE_";
        Space space = BeanUtil.copyProperties(spaceAddRequest, Space.class);
        // 默认值
        if (space.getSpaceName().isBlank()){
            space.setSpaceName(SPACE_NAME_PREFIX + UUID.randomUUID());
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        validSpace(space,true);
        fillSpaceBySpaceLevel(space);

        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 加本地锁防止用户重复创建空间
        Object object = lockMap.computeIfAbsent(userId, key -> new Object());
        synchronized (object) {
            try{
                Long spaceId = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                    if (exists) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户创建空间");
                    }
                    boolean save = this.save(space);
                    ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
                    return space.getId();
                });
                return Optional.ofNullable(spaceId).orElse(-1L);
            }finally {
                lockMap.remove(userId);
            }

        }
    }
}




