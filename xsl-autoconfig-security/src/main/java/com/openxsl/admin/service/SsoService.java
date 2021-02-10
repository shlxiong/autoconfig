package com.openxsl.admin.service;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.config.redis.GenericRedisHelper;
import com.openxsl.config.verifycode.VerifyCodeImage;

@Service
public class SsoService {
	private final SecureRandom random = new SecureRandom();
	private final long EXPIRES = 900;
	private final String PREFIX = VerifyCodeImage.ATTR_CODE;
	@javax.annotation.Resource(name="AESEncoder")
	private IPasswordEncoder passwordEncoder;
	
	private GenericRedisHelper<String> redisHelper;
	@Autowired
	public void setRedisHelper(GenericRedisHelper<String> redisHelper) {
		this.redisHelper = redisHelper;
		this.redisHelper.setEntityClass(String.class);
	}
	
	public String getSecurityCode(String userName) {
		StringBuilder code = new StringBuilder();
		while (code.length() < 16) {
			code.append(random.nextInt(100));
		}
		
		String secureCode = code.substring(0, 16);
		redisHelper.save(PREFIX+userName, secureCode, EXPIRES);
		return passwordEncoder.encode(secureCode);
	}
	
	public boolean validate(String userName, String code) {
		try {
			String originCode = passwordEncoder.decode(code);
			String realCode = redisHelper.get(PREFIX+userName);
			if (originCode.equals(realCode)) {
				redisHelper.delete(PREFIX+userName);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

}
