package com.firstTry.Adventure.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.firstTry.Adventure.config.IdProcessor;
import com.firstTry.Adventure.utils.QiniuCloudUtil;
import com.firstTry.Adventure.utils.R;

/**
 * @data 2018/8/20
 * @author Roger
 * CDN七牛云控制层
 */
@RestController
@RequestMapping("/adventure/qiniu")
public class QiniuController {
	
	@Autowired
	private IdProcessor<Long> idProcessor;
	
	/**
	 * 七牛上传图片
	 * @param image
	 * @param request
	 * @return
	 */
    @SuppressWarnings("static-access")
	@RequestMapping(value="/uploadImg", method=RequestMethod.POST)
    public R uploadImg(@RequestParam MultipartFile image, HttpServletRequest request) {
        R r = new R();
        if (image.isEmpty()) {
            r.error(500,"文件为空，请重新上传");
            return r;
        }

        try {
            byte[] bytes = image.getBytes();
            String imageName = idProcessor.nextId().toString();
            QiniuCloudUtil qiniuUtil = new QiniuCloudUtil();
            try {
                //使用base64方式上传到七牛云
                String url = qiniuUtil.put64image(bytes, imageName);
               r.put("url", url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return r;
        } catch (IOException e) {
        	r.error(500,"文件上传发生异常！");
            return r;
        }
    }
}
