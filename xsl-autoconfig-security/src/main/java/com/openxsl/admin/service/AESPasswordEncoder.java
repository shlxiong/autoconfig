package com.openxsl.admin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
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
	
	public void encodeFile(String sourceFile, String targetFile) {
		File file = new File(targetFile);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream fos = new FileOutputStream(targetFile);){
	        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
	        //读取要加密的文件流
	        CipherInputStream cis = new CipherInputStream(new FileInputStream(sourceFile), cipher);
	        byte[] b = new byte[1024];
	        int len = 0;
	        while((len = cis.read(b)) != -1) {
	        	fos.write(b, 0, len);
	        	fos.flush();
	        }
	        cis.close();
		} catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void decodeFile(String sourceFile, String targetFile) {
		try (FileInputStream fis = new FileInputStream(sourceFile);) {
			this.decodeFile(fis, targetFile);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void decodeFile(InputStream is, String targetFile) {
		File file = new File(targetFile);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, keySpec);
	        CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(targetFile), cipher);
	        byte[] buffer = new byte[1024];
	        int len;
	        while ((len = is.read(buffer)) >= 0) {
	        	cos.write(buffer, 0, len);
	        	cos.flush();
	        }  
	        cos.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		AESPasswordEncoder encoder = new AESPasswordEncoder();
		encoder.setSalt("md5aesrsa");
		String plainText = "加密串：54321_Test";
		String secretText = encoder.encode(plainText);
		System.out.println(secretText);
		System.out.println(encoder.decode(secretText));
		String sourceFile = "D:/hwm_conf.log";
		String encodeFile = "D:/hwm_conf.log1";
		String decodeFile = "D:/hwm_conf.log2";
		encoder.encodeFile(sourceFile, encodeFile);
		encoder.decodeFile(encodeFile, decodeFile);
		encoder.decodeFile(new FileInputStream(encodeFile), decodeFile);
	}

}
