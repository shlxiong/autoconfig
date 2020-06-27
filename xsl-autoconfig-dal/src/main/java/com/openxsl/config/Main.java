package com.openxsl.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 修改包名
 * @author xiongsl
 */
public class Main {
	
	static List<File> listFiles(File file) {
		List<File> targets = new ArrayList<File>();
		if (file.isFile()) {
			String name = file.getName();
			if (!name.equals("Main.java") && name.endsWith(".java")) {
				targets.add(file);
			}
		} else {
			for (String name : file.list()) {
				targets.addAll(listFiles(new File(file, name)));
			}
		}
		return targets;
	}
	
	public static void main(String[] args) throws IOException {
		String sourcePck = "cn.openxsl.config";
		String targetPck = "com.openxsl.config";
		String projDir = System.getProperty("user.dir");
		String sourceDir = projDir + "/src/main/java/"
				+ Main.class.getPackage().getName().replace('.', File.separatorChar);
		System.out.println(sourceDir);
		
		StringBuilder content = new StringBuilder();
		BufferedReader reader;
		String line;
		for (File file : listFiles(new File(sourceDir))) {
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

}
