package com.ljw.yuntubackend.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ljw.yuntubackend.annotation.AuthCheck;
import com.ljw.yuntubackend.common.BaseResponse;
import com.ljw.yuntubackend.common.ResultUtils;
import com.ljw.yuntubackend.constant.UserConstant;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.manager.TosManager;
import com.ljw.yuntubackend.modal.dto.picture.*;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.PictureTagCategory;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.vo.PictureVO;
import com.ljw.yuntubackend.service.IPictureService;
import com.ljw.yuntubackend.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 图片 前端控制器
 * </p>
 *
 * @author liujiawei
 * @since 2025-01-24
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private IUserService userService;
    @Resource
    private IPictureService pictureService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Cache<String,String> caffeineCache;
    @Resource
    private TosManager tosManager;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 上传图片（通过URL上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureUrl(PictureUploadRequest pictureUploadRequest,
                                                    HttpServletRequest request){
        User loginUser = userService.getCurrentUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片(仅本人或者管理员可删除)
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deletePicture(@PathVariable Long id,HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        boolean result = pictureService.deletePicture(id,currentUser);
        return ResultUtils.success(result);
    }

    /**
     * 修改图片（管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request) {
        if (pictureUpdateRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        Picture old = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(old == null,ErrorCode.NOT_FOUND_ERROR);

        Picture picture = BeanUtil.copyProperties(pictureUpdateRequest, Picture.class);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);

        User loginUser = userService.getCurrentUser(request);
        pictureService.fillReviewParams(picture,loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片（管理员）
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPicture(@PathVariable Long id) {
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片（封装类）
     */
    @GetMapping("/vo/{id}")
    public BaseResponse<PictureVO> getPictureVO(@PathVariable Long id) {
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureService.getPictureVO(picture,null));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        picturePage.getRecords().forEach(picture -> {
            String preSignatureUrl = tosManager.getPreSignatureUrl(picture.getUploadPath());
            picture.setUrl(preSignatureUrl);
        });
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        User currentUser = userService.getCurrentUser(request);
        pictureQueryRequest.setUserId(currentUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 封装key
        String hashKey = DigestUtils.md5DigestAsHex(JSONUtil.toJsonStr(currentUser).getBytes());
        String key = "picture:listPictureVOByPage:"+hashKey;

        // 1.查询本地缓存
        String cache = caffeineCache.getIfPresent(key);
        if (cache != null) {
            Page<PictureVO> cachePage = JSONUtil.toBean(cache, Page.class);
            return ResultUtils.success(cachePage);
        }
        // 2.查询Redis
        cache = stringRedisTemplate.opsForValue().get(key);
        if (cache != null) {
            caffeineCache.put(key, cache);
            Page<PictureVO> cachePage = JSONUtil.toBean(cache, Page.class);
            return ResultUtils.success(cachePage);
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest).in("review_status",Arrays.asList(0,1)));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 存入缓存
        int seconds = 259200 + RandomUtil.randomInt(0,300);
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(pictureVOPage),seconds, TimeUnit.SECONDS);
        caffeineCache.put(key, JSONUtil.toJsonStr(pictureVOPage));

        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(LocalDateTime.now());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getCurrentUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        pictureService.fillReviewParams(picture,loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员审核图片
     * @return
     */
    @PostMapping("/preview")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> previewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        if (pictureReviewRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = pictureService.doReviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 批量加载图片（仅管理员）
     * @return 加载图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchUploadPicture(@RequestBody PictureBatchUploadRequest pictureBatchUploadRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureBatchUploadRequest.getSearchText() == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getCurrentUser(request);
        Integer count = pictureService.batchUploadPicture(pictureBatchUploadRequest, loginUser);
        return ResultUtils.success(count);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }
}
