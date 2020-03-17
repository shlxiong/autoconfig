package com.openxsl.config.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MavenDeployFile {
	static String DEPLOY_CMD = "mvn -e deploy:deploy-file \"-DgroupId=%s\" \"-DartifactId=%s\""
			+ " \"-Dversion=%s\" \"-Dpackaging=jar\" \"-Dfile=%s\""
			+ " \"-Durl=http://192.168.8.240:8081/nexus/content/repositories/%s/\""
			+ " \"-DrepositoryId=Openxsl-%s\"";
	
	public static void main(String[] args) throws Exception {
		String sources = "E:\\m2\\repository\\com\\openxsl\\framework\\openxsl-autoconfig-base\\1.2";
		List<String> files = listJarFiles(sources);
		String[] dependency;
		for (String file : files) {
			dependency = getDependency(file);
			int len = dependency.length;
			if (len > 0) {
				String[] args2 = new String[len + 2];
				System.arraycopy(dependency, 0, args2, 0, len);
				args2[len] = "snapshot";
				args2[len+1] = "snapshot";
				String command = String.format(DEPLOY_CMD, args2);
				executeCommand(command);
			}
		}
	}
	
	private static String[] getDependency(String file) {
		String[] segments = file.replace(File.separatorChar, '/').split("/");
		// group/artifact/version/*.jar
		int len = segments.length;
		String groupId = segments[len-4];
		String artifact = segments[len-3];
		String version = segments[len-2];
		String fileName = segments[len-1];
		if (fileName.equals(artifact+"-"+version+".jar")) {
			return new String[] {
					groupId, artifact, version, fileName
			};
		} else {
			return new String[0];
		}
	}
	
	static List<String> listJarFiles(String path) {
		List<String> fileNames = new ArrayList<String>();
		File[] subDirs = new File(path).listFiles();
		for (File child : subDirs) {
			if (child.isDirectory()) {
				fileNames.addAll(listJarFiles(child.getAbsolutePath()));
			} else if (child.getName().endsWith(".jar")) {
				fileNames.add(child.getAbsolutePath());
			}
		}
		return fileNames;
	}
	
 	static void executeCommand(String command) throws IOException {
		System.out.println("执行脚本：" + command);
		String os = System.getProperty("os.name");
		Process proc;
		if (os.toLowerCase().startsWith("windows")) {
			proc = Runtime.getRuntime().exec("cmd /c "+command);
		}else {
			String[] commands = new String[]{"/bin/sh", "-c", command};
			proc = Runtime.getRuntime().exec(commands);
		}
		int value = 0;
		try {
			value = proc.waitFor();
		} catch (InterruptedException e) {
		}
		InputStream is = (value==0) ? proc.getInputStream() : proc.getErrorStream();
		String charset = System.getProperty("sun.jnu.encoding", "GBK");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
		StringBuilder sb = new StringBuilder();
		sb.append(value).append("\n\t");
		String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        System.out.println("运行结果："+sb.toString());
        reader.close();
	}

}
