package com.openxsl.config.webmvc;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.openxsl.config.util.StringUtils;

/**
 * spring-mvc Response
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class Response implements Serializable {
	@NotNull
	private String code = "0";
	/** 提示信息 */
	private String message;
	/** 异常堆栈信息 */
	private String exception;

	/** 返回结果 */
	private Object data;
	
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
            result.setCode(FAILURE.code);
            result.setMessage(FAILURE.message);
            result.setException(StringUtils.getStackTrace(e));
        }
        return result;
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
