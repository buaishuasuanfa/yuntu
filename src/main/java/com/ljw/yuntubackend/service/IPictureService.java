package com.ljw.yuntubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.yuntubackend.modal.dto.picture.PictureQueryRequest;
import com.ljw.yuntubackend.modal.dto.picture.PictureUploadRequest;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 图片 服务类
 * </p>
 *
 * @author liujiawei
 * @since 2025-01-24
 */
public interface IPictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 删除图片（管理员）
     * @param id
     * @return
     */
    boolean deletePicture(Long id,User loginUser);


    /**
     * 获取图片返回封装类
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片分页封装类
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片数据校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 构造查询条件
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

}
