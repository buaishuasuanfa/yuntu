package com.ljw.yuntubackend.controller;


import com.ljw.yuntubackend.annotation.AuthCheck;
import com.ljw.yuntubackend.common.BaseResponse;
import com.ljw.yuntubackend.common.ResultUtils;
import com.ljw.yuntubackend.constant.UserConstant;
import com.ljw.yuntubackend.modal.dto.picture.PictureUploadRequest;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.vo.PictureVO;
import com.ljw.yuntubackend.service.IPictureService;
import com.ljw.yuntubackend.service.IUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


}
