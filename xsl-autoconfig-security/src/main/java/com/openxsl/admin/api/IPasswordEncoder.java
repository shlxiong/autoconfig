package com.openxsl.admin.api;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器
 * @author shuilin.xiong
 */
public interface IPasswordEncoder extends PasswordEncoder {
	
	public String encode(String password);
	
	public String getInitialPswd();
	
//	String encode(CharSequence rawPassword);
//	
//	boolean matches(CharSequence rawPassword, String encodedPassword);

}
