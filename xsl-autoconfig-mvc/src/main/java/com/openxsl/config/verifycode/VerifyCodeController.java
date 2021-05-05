package com.openxsl.config.verifycode;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class VerifyCodeController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private VerifyCodeService service;
	
	@RequestMapping(value = "/verifycode.png", method = RequestMethod.GET)
	public void generImgVerifyCode(int width, int height, boolean simple) {
	    try {
	    	VerifyCodeImage verify = new VerifyCodeImage(width, height, simple);
	        BufferedImage verifyImg = verify.getVerificationCode();
	        service.saveCode(request, verify.getResult());
	        
	        response.setContentType("image/png"); // 必须设置响应内容类型为图片，否则前台不识别
	        OutputStream os = response.getOutputStream(); // 获取文件输出流
	        ImageIO.write(verifyImg, "png", os); // 输出图片流

	        os.flush();
	        os.close();
	    } catch (Exception e) {
	    	logger.error("", e);
	    }
	}

}
