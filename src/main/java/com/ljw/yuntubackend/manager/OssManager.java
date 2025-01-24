package com.ljw.yuntubackend.manager;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.ljw.yuntubackend.config.OssClientConfig;
import com.ljw.yuntubackend.modal.enums.UploadFileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private String objectName = "";

    public String upload(File file, String uploadPath, UploadFileTypeEnum uploadType) {
        String str = uploadType.getType()+"/"+uploadPath;
//        setObjectName(file,uploadPath,uploadType);
        return upload(file,str);
    }

    public String upload(File file,String uploadPath){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucketName(), uploadPath, fileInputStream);

            // 创建PutObject请求。
            oss.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("文件上传失败："+e.getMessage());
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(ossClientConfig.getBucketName())
                .append(".")
                .append(ossClientConfig.getEndpoint().split("//")[1])
                .append("/")
                .append(uploadPath);

        log.info("文件上传到:{}", stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * 下载文件
     * @param pathName 文件保存位置
     * @param fileName 文件名
     * @return return
     */
    public boolean download(String pathName,String fileName) {
        // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
        // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
        try {
            oss.getObject(new GetObjectRequest(ossClientConfig.getBucketName(), fileName), new File(pathName));
        } catch (OSSException | ClientException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
