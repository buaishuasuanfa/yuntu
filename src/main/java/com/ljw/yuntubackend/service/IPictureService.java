package com.ljw.yuntubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ljw.yuntubackend.modal.dto.picture.PictureUploadRequest;
import com.ljw.yuntubackend.modal.entity.Picture;
import com.ljw.yuntubackend.modal.entity.User;
import com.ljw.yuntubackend.modal.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

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


}
