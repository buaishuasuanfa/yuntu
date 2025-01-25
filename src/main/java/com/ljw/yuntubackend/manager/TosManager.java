package com.ljw.yuntubackend.manager;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ljw.yuntubackend.config.TosClientConfig;
import com.ljw.yuntubackend.modal.entity.ImageInfo;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.comm.TosHeader;
import com.volcengine.tos.internal.util.TosUtils;
import com.volcengine.tos.model.object.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class TosManager {

    @Resource
    private TosClientConfig.TosClient tosClient;
    @Resource
    private TosClientConfig tosClientConfig;

    // 上传文件
    public void upload(File file, String uploadPath) throws TosClientException {
        TOSV2 tos = tosClient.getTosv2();
        // TOSV2 提供的所有接口均会抛出 TosException 异常，需要使用 try-catch 进行捕获并处理。
        try {
            // 待上传的数据，以下代码以上传一个 ByteArrayInputStream 作为示例
            FileInputStream fileInputStream = new FileInputStream(file);
            // 设置上传的桶名和对象名
            PutObjectInput putObjectInput = new PutObjectInput().setBucket(tosClient.getBucketName())
                    .setKey(uploadPath)
                    .setContent(fileInputStream);
            // 上传对象
            tos.putObject(putObjectInput);
        } catch (TosClientException e) {
            // 操作失败，捕获客户端异常，一般情况是请求参数错误，此时请求并未发送
            System.out.println("putObject failed");
            System.out.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        } catch (TosServerException e) {
            // 操作失败，捕获服务端异常，可以获取到从服务端返回的详细错误信息
            System.out.println("putObject failed");
            System.out.println("StatusCode: " + e.getStatusCode());
            System.out.println("Code: " + e.getCode());
            System.out.println("Message: " + e.getMessage());
            System.out.println("RequestID: " + e.getRequestID());
        } catch (Throwable t) {
            // 作为兜底捕获其他异常，一般不会执行到这里
            System.out.println("putObject failed");
            System.out.println("unexpected exception, message: " + t.getMessage());
        }
        // https://ljw-bucket.tos-cn-beijing.volces.com/example_dir/example_object.txt
    }

    // 获取文件信息
    public ImageInfo getImageInfo(String uploadPath) {
        // 获取图片信息
        String style = "image/info";
        TOSV2 tos = tosClient.getTosv2();
        try {
            GetObjectV2Input input = new GetObjectV2Input().setBucket(tosClientConfig.getBucketName()).setKey(uploadPath).setProcess(style);
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
                 GetObjectV2Output output = tos.getObject(input)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = output.getContent().read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
                String respBody = stream.toString("UTF-8");
                JsonNode jsonNode = TosUtils.getJsonMapper().readTree(respBody);
                int height = jsonNode.get("ImageHeight").get("value").asInt();
                int width = jsonNode.get("ImageWidth").get("value").asInt();
                double fileSize = jsonNode.get("FileSize").get("value").asDouble();
                String format = jsonNode.get("Format").get("value").asText();
                return new ImageInfo(height, width, fileSize, format);
            } catch (JacksonException e) {
                System.out.println("parse response data failed");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("read response data failed");
                e.printStackTrace();
            }
        } catch (TosClientException e) {
            // 操作失败，捕获客户端异常，一般情况是请求参数错误，此时请求并未发送。
            System.out.println("get image info failed");
            System.out.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        } catch (TosServerException e) {
            // 操作失败，捕获服务端异常，可以获取到从服务端返回的详细错误信息。
            System.out.println("get image info failed");
            System.out.println("StatusCode: " + e.getStatusCode());
            System.out.println("Code: " + e.getCode());
            System.out.println("Message: " + e.getMessage());
            System.out.println("RequestID: " + e.getRequestID());
        } catch (Throwable t) {
            // 作为兜底捕获其他异常，一般不会执行到这里。
            System.out.println("get image info failed");
            System.out.println("unexpected exception, message: " + t.getMessage());
        }
        return null;
    }

    // 删除文件
    public void delete(String uploadPath) {
        TOSV2 tos = tosClient.getTosv2();
        DeleteObjectInput input = new DeleteObjectInput().setBucket(tosClientConfig.getBucketName()).setKey(uploadPath);
        tos.deleteObject(input);
    }

    // 获取图片预签名
    public String getPreSignatureUrl(String uploadPath){
        return getPreSignatureUrl(uploadPath,false);
    }

    // 获取图片缩略图预签名
    public String getPreSignatureUrl(String uploadPath,boolean flag) {
        TOSV2 tos = tosClient.getTosv2();

        PreSignedURLInput input = new PreSignedURLInput()
                .setBucket(tosClient.getBucketName())
                .setKey(uploadPath)
                .setHttpMethod(HttpMethod.GET);

        if (!flag) {
            return tos.preSignedURL(input).getSignedUrl();
        }

        // 获取缩略图预签名
        String style = "image/resize,h_128/format,jpg";
        Map<String, String> query = new HashMap<>();
        query.put(TosHeader.QUERY_DATA_PROCESS, style);

        input.setQuery(query);
        return tos.preSignedURL(input).getSignedUrl();
    }
}
