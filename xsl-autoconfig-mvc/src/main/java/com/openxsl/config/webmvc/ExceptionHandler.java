package com.openxsl.config.webmvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.openxsl.config.util.StringUtils;

/**
 * 全局异常处理类
 * @author xiongsl
 */
@Controller
@ControllerAdvice
public class ExceptionHandler {
	protected final Logger logger = Logger.getLogger(getClass());
	
	@org.springframework.web.bind.annotation.ExceptionHandler
	public void handle(HttpServletRequest req, HttpServletResponse resp,
					   Throwable exception) throws ServletException, IOException{
		logger.error("Controller Exception: ", exception);
		final Class<?> clazz = exception.getClass();
		String message=null, detail=null;
		if (exception instanceof BindException){
			BindingResult errors = ((BindException)exception).getBindingResult();
			StringBuilder buffer = new StringBuilder("传参错误");
			for (FieldError error : errors.getFieldErrors()){
				String field = error.getObjectName()+"."+error.getField();
				buffer.append(String.format("\n　　　　[%s]: %s", 
									field, error.getDefaultMessage()));
			}
			message = buffer.toString();
			detail = errors.toString();
		}else if (clazz == MissingServletRequestParameterException.class
				|| clazz == TypeMismatchException.class
				|| clazz == HttpMessageNotReadableException.class){  //HttpStatus.BAD_REQUEST
			message = "缺少参数或参数类型错误";
		}else if (HttpMediaTypeException.class.isAssignableFrom(clazz)){
			message = "头信息不对:"+req.getContentType();
		}else if (exception instanceof NoHandlerFoundException){   //HttpStatus.NOT_FOUND
//				|| exception instanceof NoSuchRequestHandlingMethodException){
			message = "请求地址错误";
		}else if (exception instanceof HttpRequestMethodNotSupportedException){
			message = "HTTP_METHOD不正确";
		}else{  //RuntimeException
			detail = StringUtils.getStackTrace(exception);
			resp.setStatus(500);
		}
		
		req.setAttribute("errorMsg", message);
		req.setAttribute("errorDetail", detail);
		req.getRequestDispatcher("/errorPage").forward(req, resp);
	}
	
}
