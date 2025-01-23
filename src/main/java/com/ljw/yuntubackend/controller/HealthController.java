package com.ljw.yuntubackend.controller;

import com.ljw.yuntubackend.common.BaseResponse;
import com.ljw.yuntubackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 刘佳伟
 * @date 2025/1/15 21:45
 * @Description
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public BaseResponse<String> health(){
        return ResultUtils.success("OK");
    }

}
