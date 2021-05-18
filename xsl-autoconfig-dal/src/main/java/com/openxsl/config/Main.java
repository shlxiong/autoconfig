package com.openxsl.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Assert;

import com.openxsl.config.util.HexEncoder;

/**
 * 修改包名
 * @author xiongsl
 */
public class Main {
	static final AESPasswordEncoder encoder = new AESPasswordEncoder();
	
	/**
	 * Zip压缩文件
	 * @param sourceDir 目录
	 * @param zipName zip名称
	 * @throws IOException
	 */
	static void makeZip(String sourceDir, String zipName) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName));
		new File("demo").mkdirs();  //tempDir
		List<File> files = new ArrayList<File>();
		files.add(new File(sourceDir+"/pom.xml"));
		String javaDir = sourceDir + "/src/main/java/" + Main.class.getPackage().getName().replace('.', '/');
		files.addAll(listJavas(new File(javaDir)));
		String xmlDir = sourceDir + "/src/main/resources";
		files.addAll(listXmls(new File(xmlDir)));
		boolean flag = true;
		for (File file : files) {
			String parentPath = file.getName().endsWith(".java") ? javaDir : xmlDir;
			String pathName = flag ? "pom.xml"
					: file.getCanonicalPath().substring(parentPath.length()+1);
			File targetFile = new File("demo/", pathName);
			encoder.encodeFile(file.getAbsolutePath(), targetFile.getAbsolutePath());
			
			if (flag) {
				zos.putNextEntry(new ZipEntry(encoder.encode("pom.xml")));
				flag = false;
			} else {
				String path = encryPath(file, parentPath);
				zos.putNextEntry(new ZipEntry(path));
			}
			FileInputStream fis = new FileInputStream(targetFile);  //file
			byte[] buf = new byte[5120];
			int len = 0;
            while ((len = fis.read(buf)) != -1) {
                zos.write(buf, 0, len);
                zos.flush();
            }
            fis.close();
            zos.closeEntry();
		}
		zos.close();
	}
	private static String encryPath(File file, String baseDir) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String path = file.getCanonicalPath().substring(baseDir.length()+1)
							.replace(File.separator, "/");
		for (String p : path.split("/")) {
			buffer.append(encoder.encode(p)).append("/");
		}
		return buffer.substring(0, buffer.length()-1);
	}
	
	/**
	 * 解压缩zip文件
	 * @param zipFile zip文件
	 * @param targetDir 目录
	 * @throws IOException
	 */
	static void unzip(String zipFile, String targetDir) throws IOException {
        File file = new File(targetDir);
        if (!file.exists() || file.isDirectory()) {
        	file.mkdirs();
        }
        
        ZipFile zips = new ZipFile(zipFile);
        Enumeration<?> emu = zips.entries();
        while (emu.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) emu.nextElement();
            file = new File(targetDir + decryPath(entry.getName()));
            if (!file.exists()) {
            	file.getParentFile().mkdirs();
            }
            encoder.decodeFile(zips.getInputStream(entry), file.getAbsolutePath());
        }
        zips.close();
	}
	private static String decryPath(String entryPath) throws IOException {
		StringBuilder buffer = new StringBuilder();
		for (String p : entryPath.split("/")) {
			buffer.append(encoder.decode(p)).append("/");
		}
		return buffer.substring(0, buffer.length()-1);
	}
	
	static void replacePcks(String sourceDir, final String sourcePck, final String targetPck) throws IOException{
		StringBuilder content = new StringBuilder();
		BufferedReader reader;
		String line;
		for (File file : listJavas(new File(sourceDir))) {
			boolean flag = false;
			reader = new BufferedReader(new FileReader(file));
			while ((line=reader.readLine()) != null) {
				if (line.contains(sourcePck)) {
					line = line.replace(sourcePck, targetPck);
					flag = true;
				}
				content.append(line).append("\n");
			}
			reader.close();
			if (flag) {
				System.out.println("replace " + file);
				FileWriter writer = new FileWriter(file);
				writer.write(content.toString());
				writer.close();
			}
			content.delete(0, content.length());
		}
	}
	
	private static List<File> listJavas(File file) {
		List<File> targets = new ArrayList<File>();
		if (file.isFile()) {
			String name = file.getName();
			if (!name.equals("Main.java") && name.endsWith(".java")) {
				targets.add(file);
			}
		} else {
			for (String name : file.list()) {
				targets.addAll(listJavas(new File(file, name)));
			}
		}
		return targets;
	}
	private static List<File> listXmls(File file) {
		List<File> targets = new ArrayList<File>();
		if (file.isFile()) {
			String name = file.getName();
			if (name.endsWith(".xml")) {
				targets.add(file);
			}
		} else {
			try {
				for (String name : file.list()) {
					targets.addAll(listXmls(new File(file, name)));
				}
			} catch (NullPointerException npe) {
				//
			}
		}
		return targets;
	}
	
	public static void main(String[] args) throws IOException {
		encoder.setSalt("com.openxsl.vmp");
		String projDir = System.getProperty("user.dir");
//		makeZip(projDir, "test.zip");
		unzip("/users/xiongsl/Applications/test-move/test-adm.zip", projDir+"/demo/admin/");
		
//		String sourcePck = "cn.openxsl.config";
//		String targetPck = "com.openxsl.config";
//		String sourceDir = projDir + "/src/main/java/"
//				+ Main.class.getPackage().getName().replace('.', File.separatorChar);
//		System.out.println(sourceDir);
//		replacePcks(sourceDir, sourcePck, targetPck);
	}
	
	static class AESPasswordEncoder {
		static final String KEY_ALGORITHM = "AES";
		static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
		static SecretKeySpec keySpec;
		
//		private String salt = "xxxxxyyyyyzzzzz1";
		public void setSalt(String salt) {
			Assert.notNull(salt, "");
			while (salt.length() < 16) {
				salt += salt;
			}
			salt = salt.substring(0, 16);
			keySpec = new SecretKeySpec(salt.getBytes(), KEY_ALGORITHM);
		}

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
	}

}
