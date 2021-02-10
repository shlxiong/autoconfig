package com.openxsl.config.webmvc;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.openxsl.config.util.StringUtils;

/**
 * spring-mvc Response
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class Response implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);
	@NotNull
	private String code = "0";
	private int status = 200;   //前端检查状态
	/** 提示信息 */
	private String message;
	/** 异常堆栈信息 */
	private String exception;

	/** 返回结果 */
	private Object data = new JSONObject();
	
	public static final Response SUCCESS = new ImmutableResponse("操作成功");
	public static final Response FAILURE = new ImmutableResponse("-1", "操作失败");
	
	public Response() {}
	public Response(String message) {
		this.setMessage(message);
	}
	public Response(String code, String message) {
		this.setCode(code);
		this.setMessage(message);
	}
	
	public interface Command<D> {   //lamda function
        D execute();
    }
	public static <D> Response build(Command<D> cmd) {
        Response result = new Response();
        try {
            D data = cmd.execute();
            result.setMessage(SUCCESS.message);
            result.setCode(SUCCESS.code);
            result.setData(data);
        } catch (Exception e) {
        	LOGGER.error("", e);
            result.setCode(FAILURE.code);
            result.setMessage(FAILURE.message);
            result.setException(StringUtils.getStackTrace(e));
        }
        return result;
    }
	
	public interface Excutable {
		void execute();
	}
	public static Response build(Excutable cmd) {
        try {
            cmd.execute();
            return SUCCESS;
        } catch (Exception e) {
        	LOGGER.error("", e);
        	Response result = new Response(FAILURE.code, FAILURE.message);
            result.setException(StringUtils.getStackTrace(e));
            return result;
        }
    }
	
	public boolean successful(){
		return exception == null;
	}
	public boolean isSuccess() {
		return exception == null;
	}
	
	//============================== JavaBean Methods =========================//
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	static class ImmutableResponse extends Response{
		public ImmutableResponse(String message) {
			this.setMessage(message);
		}
		public ImmutableResponse(String code, String message) {
			this.setCode(code);
			this.setMessage(message);
		}

		public void setData(Object data) {
			throw new UnsupportedOperationException();
		}
	}
	
}
