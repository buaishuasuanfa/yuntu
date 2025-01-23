package com.ljw.yuntubackend.manager;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.ljw.yuntubackend.config.OssClientConfig;
import com.ljw.yuntubackend.exception.ErrorCode;
import com.ljw.yuntubackend.exception.ThrowUtils;
import com.ljw.yuntubackend.modal.enums.UploadFileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author 刘佳伟
 * @date 2025/1/23 21:32
 * @Description
 */
@Slf4j
@Component
public class OssManager {

    @Resource
    private OssClientConfig ossClientConfig;
    @Resource
    private OSS oss;


    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".png");

    private String objectName = "";

    public String upload(MultipartFile file, String prefix, UploadFileTypeEnum uploadType) {
        String str = uploadType.getType()+"/"+prefix;
        setObjectName(file,prefix,uploadType);
        return upload(file,str);
    }

    public String upload(MultipartFile file,String prefix){
        // 检查文件大小
        checkFileSize(file);
        // 检查文件类型
        checkFileType(file);
        try {
            if (Objects.equals(objectName, "")){
                setObjectName(file,prefix,null);
            }
            byte[] content = file.getBytes();

            ThrowUtils.throwIf(StrUtil.isBlank(objectName), ErrorCode.SYSTEM_ERROR,"文件名获取失败");
            objectName = prefix+"/"+objectName;
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucketName(), objectName, new ByteArrayInputStream(content));

            // 创建PutObject请求。
            oss.putObject(putObjectRequest);
        } catch (IOException e) {
            log.error("文件上传失败："+e.getMessage());
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(ossClientConfig.getBucketName())
                .append(".")
                .append(ossClientConfig.getEndpoint().split("//")[1])
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder);

        return stringBuilder.toString();
    }

    private void setObjectName(MultipartFile file, String prefix, UploadFileTypeEnum uploadType) {
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取原始文件名后缀 global.jpg
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (uploadType != null){
            switch (uploadType) {
                case IMAGE: this.objectName = prefix+extension;
            }
        } else{

            //构造新文件名称
            this.objectName = UUID.randomUUID()+ extension;
        }

//        return UUID.randomUUID()+ extension;
    }

    private void checkFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过 1MB");
        }
    }

    private void checkFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型，仅支持 " + ALLOWED_EXTENSIONS);
        }
    }


}
