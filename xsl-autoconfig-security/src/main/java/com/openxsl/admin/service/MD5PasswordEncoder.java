package com.openxsl.admin.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.config.util.HexEncoder;
import com.openxsl.config.util.MessageDigester;

@Service("MD5Encoder")
public class MD5PasswordEncoder implements IPasswordEncoder {
	@Value("${security.password-salt}")
	private String salt;
	@Value("${security.password-init}")
	private String initPswd;
	
	@PostConstruct
	public void initiate(){
		this.salt = HexEncoder.decode(salt);
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return this.encode(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return this.encode(rawPassword).equals(encodedPassword);
	}

	@Override
	public String encode(String password) {
		try {
			return MessageDigester.md5(password, salt);
		} catch (Exception e) {
			return password.toString();
		}
	}

	@Override
	public String getInitialPswd() {
		String plainText = HexEncoder.decode(initPswd);
		return this.encode(plainText);
	}

	@Override
	public String decode(String encoded) {
		throw new UnsupportedOperationException();
	}
	
}
