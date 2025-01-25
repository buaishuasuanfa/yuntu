package com.ljw.yuntubackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.ljw.yuntubackend.exception.BusinessException;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.manager.TosManager;
import com.ljw.yuntubackend.modal.dto.file.UploadPictureResult;
import com.ljw.yuntubackend.modal.entity.ImageInfo;
import com.ljw.yuntubackend.utils.ImgUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author 刘佳伟
 * @date 2025/1/24 17:34
 * @Description
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private TosManager tosManager;

    /**
     * 上传图片模板方法
     *
     * @param object    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object object, String uploadPathPrefix) {
        // 校验图片
        validPicture(object);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginalFilename(object);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                "wbmp");
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath,null);
            // 处理文件来源
            processFile(object,file);
            // 压缩图片
            ImgUtil.toWebpFile(file);
            // 上传图片
            tosManager.upload(file, uploadPath);
            ImageInfo imageInfo = tosManager.getImageInfo(uploadPath);
            // 封装返回结果
            return getUploadPictureResult(imageInfo,originFilename,file,uploadPath);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    // 处理文件来源
    protected abstract void processFile(Object object, File file) throws IOException;

    // 获取原始文件名
    protected abstract String getOriginalFilename(Object object);

    // 图片校验
    protected abstract void validPicture(Object object);

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    private UploadPictureResult getUploadPictureResult(ImageInfo imageInfo,String originFilename,File file,String uploadPath) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        String format = imageInfo.getFormat();
        double picScale = NumberUtil.round(picWidth*1.0/picHeight,2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUploadPath(uploadPath);
        return uploadPictureResult;
    }

    /**
     * 删除图片
     */
    public void deleteImage(String url) {
        String baseUrl = "https://ljw-bucket.tos-cn-beijing.volces.com/";
        // 先找到 baseUrl 在原 URL 中的结束位置
        int startIndex = url.indexOf(baseUrl);
        if (startIndex != -1) {
            startIndex += baseUrl.length();

            // 再找到第一个问号的位置
            int endIndex = url.indexOf('?');
            if (endIndex != -1) {
                // 提取目标内容
                String result = url.substring(startIndex, endIndex);
                result = result.replace("%2F", "/");
                tosManager.delete(result);
            }
        }
    }

}
