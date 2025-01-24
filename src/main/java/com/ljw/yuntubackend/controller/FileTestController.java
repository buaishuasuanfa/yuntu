package com.ljw.yuntubackend.controller;

import com.ljw.yuntubackend.common.BaseResponse;
import com.ljw.yuntubackend.common.ResultUtils;
import com.ljw.yuntubackend.manager.OssManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author 刘佳伟
 * @date 2025/1/24 9:21
 * @Description
 */
@RestController
public class FileTestController {

    @Resource
    private OssManager ossManager;

    @GetMapping
    public BaseResponse<Boolean> test() {
        String pathName = "D:\\desktop";
        String objectName = "avatar/1881171093897687041/1881171093897687041.png";
        boolean download = ossManager.download(pathName, objectName);
        return ResultUtils.success(download);
    }

    @PostMapping("/file/upload")
    public BaseResponse<Boolean> upload(@RequestParam("file") MultipartFile file) {
//        String upload = ossManager.upload(file, "test");
        return ResultUtils.success(true);
    }

}
