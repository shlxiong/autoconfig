package com.openxsl.config.rpcmodel;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 签名对象
 * @author xiongsl
 */
@SuppressWarnings("serial")
public class SignatureBean implements Serializable{
	/** 签名信息 */
	@JSONField(serialize=false)
	private String finger;
	/** 加密数据 */
	@JSONField(serialize=false)
	private String encoded;
	
	public String getFinger() {
		return finger;
	}
	public void setFinger(String finger) {
		this.finger = finger;
	}
	public String getEncoded() {
		return encoded;
	}
	public void setEncoded(String encoded) {
		this.encoded = encoded;
	}
}
