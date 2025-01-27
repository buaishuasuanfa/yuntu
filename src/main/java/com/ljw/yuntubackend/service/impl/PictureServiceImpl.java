package com.ljw.yuntubackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.manager.TosManager;
import com.ljw.yuntubackend.manager.upload.FilePictureUpload;
import com.ljw.yuntubackend.manager.upload.PictureUploadTemplate;
import com.ljw.yuntubackend.manager.upload.UrlPictureUpload;
import com.ljw.yuntubackend.mapper.PictureMapper;
import com.ljw.yuntubackend.modal.dto.file.UploadPictureResult;
import com.ljw.yuntubackend.modal.dto.picture.*;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.Space;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.enums.PictureReviewStatusEnum;
import com.ljw.yuntubackend.modal.vo.PictureVO;
import com.ljw.yuntubackend.modal.vo.UserVO;
import com.ljw.yuntubackend.service.PictureService;
import com.ljw.yuntubackend.service.SpaceService;
import com.ljw.yuntubackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 图片 服务实现类
 * </p>
 *
 * @author liujiawei
 * @since 2025-01-24
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private UserService userService;
    @Resource
    private TosManager tosManager;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private Cache<String,String> caffeineCache;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public PictureVO uploadPicture(Object inoutSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return uploadPicture(inoutSource, pictureUploadRequest, loginUser, "");
    }

    @Override
    public PictureVO uploadPicture(Object inoutSource, PictureUploadRequest pictureUploadRequest, User loginUser, String category) {
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断上传空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"目标空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"您不是该空间管理员");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR,"存储空间不足");
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(),ErrorCode.OPERATION_ERROR,String.format("最多存储%s张照片",space.getMaxCount()));
        }
        // 用于判断是新增还是更新图片
        Long pictureId = pictureUploadRequest.getId();
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null){
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            Picture picture = this.getById(pictureId);
            Long userId = picture.getUserId();
            if (!userId.equals(loginUser.getId()) && userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            pictureUploadTemplate.deleteImage(picture.getUploadPath());

            if (spaceId == null){
                if ( picture.getSpaceId()!=null){
                    spaceId = picture.getSpaceId();
                }
            }else {
                if (!spaceId.equals(picture.getSpaceId())){
                    throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间错误");
                }
            }
        }
        if (inoutSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }


        // 上传图片，得到信息
        // 按照用户 id,分类，空间 id 划分目录
        String uploadPathPrefix = "";
        if (!category.isEmpty() && spaceId != null) {
            uploadPathPrefix = String.format("space/%s/%s",spaceId, category);
        } else if (!category.isEmpty()){
            uploadPathPrefix = String.format("public/%s/%s", loginUser.getId(),category);
        }else {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }

        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inoutSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUploadPath(uploadPictureResult.getUploadPath());
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);

        fillReviewParams(picture, loginUser);
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(LocalDateTimeUtil.now());
        }
        if (!category.isEmpty()) {
            picture.setCategory(category);
        }

        Long finalSpaceId = spaceId;
        return transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);

            // 判断空间是否存在
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("total_size = total_size +" + picture.getPicSize())
                        .setSql("total_count = total_count +" + 1)
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            // 删除缓存
            if (result) {
                String hashKey = DigestUtils.md5DigestAsHex(JSONUtil.toJsonStr(loginUser).getBytes());
                String key = "picture:listPictureVOByPage:" + hashKey;

                caffeineCache.invalidate(key);
                stringRedisTemplate.delete(key);
            }
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            return PictureVO.objToVo(picture, tosManager);
        });
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture,tosManager);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public boolean doReviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 获取信息
        Long pictureId = pictureReviewRequest.getId();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByCode(pictureReviewRequest.getReviewStatus());
        if (pictureId == null || pictureReviewStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        if (Objects.equals(oldPicture.getReviewStatus(), pictureReviewRequest.getReviewStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请要重复设置状态");
        }

        Picture updatePicture = BeanUtil.copyProperties(oldPicture, Picture.class);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewStatus(pictureReviewStatusEnum.getCode());
        updatePicture.setReviewMessage(pictureReviewRequest.getReviewMessage());
        updatePicture.setReviewTime(LocalDateTimeUtil.now());
        boolean update = this.updateById(updatePicture);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public Integer batchUploadPicture(PictureBatchUploadRequest pictureBatchUploadRequest, User loginUser) {
        String searchText = pictureBatchUploadRequest.getSearchText();
        // 格式化数量
        int count = pictureBatchUploadRequest.getSize();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser, searchText);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Override
    public void deletePicture(Picture picture, User loginUser) {
        if (!Objects.equals(picture.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // todo 若是管理员删除用户上传的公共图片，必须通知用户

        Picture oldPicture = this.getById(picture.getId());
        checkPictureAuth(oldPicture, loginUser);
        Long count = this.lambdaQuery()
                .eq(Picture::getUploadPath, picture.getUploadPath())
                .count();
        ThrowUtils.throwIf(count > 1, ErrorCode.OPERATION_ERROR,"多条消息关联");

        // 事务执行删除图片表与修改空间表
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(picture.getId());
            ThrowUtils.throwIf(result, ErrorCode.OPERATION_ERROR);
            if (picture.getSpaceId()!=null){
                Space space = spaceService.getById(picture.getSpaceId());
                ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"所属空间不存在");
                ThrowUtils.throwIf(space.getTotalCount() == 0 || space.getTotalSize()<picture.getPicSize(), ErrorCode.OPERATION_ERROR);
                space.setTotalCount(space.getTotalCount()-1);
                space.setTotalSize(space.getTotalSize()-picture.getPicSize());
                boolean result1 = spaceService.updateById(space);
                ThrowUtils.throwIf(result1, ErrorCode.OPERATION_ERROR);
            }
            // 删除 Tos 对象存储图片
            PictureUploadTemplate pictureUploadTemplate = new FilePictureUpload();
            pictureUploadTemplate.deleteImage(picture.getUploadPath());
            return true;
        });

    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(LocalDateTime.now());
        // 数据校验
        validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        checkPictureAuth(oldPicture, loginUser);

        fillReviewParams(picture,loginUser);
        // 操作数据库
        boolean result = updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }


    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewTime(LocalDateTimeUtil.now());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEW.getCode());
        }
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = new ArrayList<>();
        for (Picture picture : pictureList) {
            PictureVO pictureVO = PictureVO.objToVo(picture,tosManager);
            pictureVOList.add(pictureVO);
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "pic_format", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "pic_width", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "pic_weight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_size", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "pic_scale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "review_status", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "review_message", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewer_id", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "space_id", spaceId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "edit_time", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "edit_time", endEditTime);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void checkPictureAuth(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        if ( spaceId == null){
            // 公共图库
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }else {
            // 私有图库
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }


}
