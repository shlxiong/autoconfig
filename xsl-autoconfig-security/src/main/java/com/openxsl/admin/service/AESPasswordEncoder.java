package com.openxsl.admin.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.config.util.HexEncoder;

@Service("AESEncoder")
public class AESPasswordEncoder implements IPasswordEncoder {
	static final String KEY_ALGORITHM = "AES";
	static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
	static SecretKeySpec keySpec;
	
//	private String salt = "xxxxxyyyyyzzzzz1";
	@Value("${security.password-init}")
	private String initPswd;
	
	@Value("${security.password-salt}")
	public void setSalt(String salt) {
		Assert.notNull(salt, "");
		while (salt.length() < 16) {
			salt += salt;
		}
		salt = salt.substring(0, 16);
		keySpec = new SecretKeySpec(salt.getBytes(), KEY_ALGORITHM);
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return this.encode(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return this.encode(rawPassword.toString()).equals(encodedPassword);
	}

	@Override
	public String encode(String password) {
		try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encoded = cipher.doFinal(password.getBytes());
            return HexEncoder.encode(encoded);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public String getInitialPswd() {
		String plainText = HexEncoder.decode(initPswd);
		return this.encode(plainText);
	}

	@Override
	public String decode(String encoded) {
		try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] original = cipher.doFinal(HexEncoder.decodeBytes(encoded));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
	}
	
	public static void main(String[] args) {
		AESPasswordEncoder encoder = new AESPasswordEncoder();
		encoder.setSalt("md5aesrsa");
		String plainText = "加密串：54321_Test";
		String secretText = encoder.encode(plainText);
		System.out.println(secretText);
		System.out.println(encoder.decode(secretText));
	}

}
