package com.openxsl.config.dal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import com.openxsl.config.autodetect.PrefixProps;
import com.openxsl.config.autodetect.PrefixPropsRegistrar;
import com.openxsl.config.util.StringUtils;

/**
 * @author xiongsl
 */
@SuppressWarnings("unchecked")
@Import({PrefixPropsRegistrar.class})
public class FtpClientInvoker {
	public static final String FTP_HOST = "ftp.host";
	public static final String FTP_PORT = "ftp.port";
	public static final String FTP_USER = "ftp.username";
	public static final String FTP_PASS = "ftp.password";
	public static final String FTP_PRIV_KEY = "ftp.privatekey";
	public static final String FTP_ROOT = "ftp.rootpath";
	public static final String FTP_LOCAL_ROOT = "ftp.local.root";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ChannelSftp sftp = null;
	private Properties props;
	private String rootPath;
	private String localRootPath;
	private boolean setted = false;
	
	@Resource
	@PrefixProps(prefix="${spring.application.name}", regexp="(.*).ftp.(.*)")
	private Properties ftpProps;
	
	public FtpClientInvoker(){}
	public FtpClientInvoker(Properties props){
		this.setFtpProps(props);
	}
	public void setFtpProps(Properties props){
		this.props = props;
		logger.info("Ftp Connection Properties: {}", props);
		rootPath = props.getProperty(FTP_ROOT);
		localRootPath = props.getProperty(FTP_LOCAL_ROOT);
		setted = true;
	}
	
	public static void main(String[] args) throws IOException{
		Properties props = new Properties();
		props.setProperty(FTP_HOST, "10.50.8.24");
		props.setProperty(FTP_PORT, "22");
		props.setProperty(FTP_ROOT, "/upload");
		props.setProperty(FTP_USER, "zjtg");
		props.setProperty(FTP_PASS, "zjtg123456");
		props.setProperty(FTP_LOCAL_ROOT, "/local/download");
		FtpClientInvoker test = new FtpClientInvoker();
		test.setFtpProps(props);
		test.listFiles("XHH");
		test.upload("D:/keys", "XHH");
	}
	
	/**
	 * 下载文件到指定的目录下
	 * @param directory  FTP目录
	 * @param fileName  原文件名
	 * @param saveFile  下载后的文件名
	 * @throws IOException
	 */
	public void download(String directory, String fileName, String saveFile) throws IOException {
		connect();
		
		try {
			String path = this.canonicalPath(directory);
            sftp.cd(path);
            File file = new File(saveFile);
            sftp.get(fileName, new FileOutputStream(file));
            logger.debug("download {} success", file.getName());
            sftp.cd(rootPath);
        }catch(SftpException ex){
			throw new IOException("下载文件失败", ex);
		}
    }
	
	public InputStream read(String directory, String fileName) throws IOException{
		connect();
		
		try{
			String path = this.canonicalPath(directory);
            sftp.cd(path);
	        return sftp.get(fileName);
		}catch(SftpException ex){
			throw new IOException("读文件失败", ex);
		}
	}
	
	/**
	 * 上传文件
	 * @param localPath  本地文件夹
	 * @param remotePath  FTP服务器的目录
	 * @throws IOException
	 */
	public void upload(String localPath, String remotePath) throws IOException{
		File parentFile = new File(localPath);
		File[] children;
		if (parentFile.isFile()){
			children = new File[]{parentFile};
		}else{  //not exists or directory
			children = parentFile.listFiles();
		}
        if (children == null) {
        	return;
        }
        try {
        	connect();
        	if (remotePath!=null && !remotePath.equals("")){
        		sftp.cd(remotePath);
        	}
            for (File file : children) {
                if (file.isFile()){
                    sftp.put(new FileInputStream(file), file.getName());
                    logger.debug("upload {} success!", file.getCanonicalPath());
                }
            }
            sftp.cd(rootPath);
        } catch (SftpException e) {
        	throw new IOException("上传文件失败", e);
        }
	}
	
	public void write(String directory, String fileName, String bakFileName,
					String content) throws IOException{
		connect();
		
		try{
			String path = this.canonicalPath(directory);
			sftp.cd(path);
		    if (!StringUtils.isEmpty(bakFileName) && exists(path, fileName)){
		    	if (exists(path,bakFileName)){
		    		sftp.rm(bakFileName);
		    	}
		    	sftp.rename(fileName, bakFileName);  //重命名可能失败
		    }
		    InputStream is = new ByteArrayInputStream(content.getBytes());
		    sftp.put(is, fileName);
		}catch(SftpException ex){
			throw new IOException("写文件失败", ex);
		}
	}
	
	public List<FileInfo> listFiles(String directory, String...fileExt)throws IOException{
		connect();
		
		try{
			String path = this.canonicalPath(directory);
            sftp.cd(path);
            Vector<LsEntry> list = sftp.ls(path);
            Collections.sort(list, new Comparator<LsEntry>(){
				@Override
				public int compare(LsEntry entry1, LsEntry entry2) { //降序
					return entry2.getAttrs().getATime() - entry1.getAttrs().getATime();
				}
			});
            
            String extName = null;
            if (fileExt.length > 0){
            	extName = fileExt[0];
            	if (extName.charAt(0) != '.') {
            		extName = "." + extName;
            	}
            }
            List<FileInfo> lstFiles = new ArrayList<FileInfo>(list.size());
            String fileName;
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (LsEntry entry : list){
            	fileName = entry.getFilename();
            	if (fileName.charAt(0) == '.') {
            		continue;  // .和..
            	}
            	if (extName==null || fileName.endsWith(extName)){
            		SftpATTRS attrs = entry.getAttrs();
	            	FileInfo file = new FileInfo(fileName, attrs.isDir());
	            	file.setSize(attrs.getSize());
	            	file.setPermission(entry.getAttrs().getPermissionsString());
	            	file.setCreateDate(
	            			sdfrmt.format(new Date(attrs.getATime()*1000L)) );
	            	file.setLastModified(
	            			sdfrmt.format(new Date(attrs.getMTime()*1000L)) );
	            	lstFiles.add(file);
            	}
            }
            list.clear();
            sftp.cd(rootPath);
            return lstFiles;
		}catch(SftpException ex){
			throw new IOException("读取目录失败", ex);
		}
	}
	public List<FileInfo> listExceptFiles(String directory, String excludes,
					String...fileExt)throws IOException{
		List<FileInfo> files = this.listFiles(directory, fileExt);
		List<String> exts = StringUtils.split2(excludes, ",");
		for (int i=files.size()-1; i>=0; i--){
			if (files.get(i).isDir()) {
				continue;
			}
			String extName = files.get(i).getFileName();
			if (extName.contains(".")){
				extName = extName.substring(extName.lastIndexOf("."));
				if (exts.contains(extName)){
					files.remove(i);
				}
			}
		}
		return files;
	}
	
	/**
	 * 删除过期文件
	 * @param directory  FTP服务器目录
	 * @param days  过期天数
	 * @return
	 * @throws IOException
	 */
	public int deleteRecentFiles(String directory, int days)throws IOException{
		connect();
		
		int count = 0;
		try{
			String path = this.canonicalPath(directory);
            sftp.cd(path);
            Vector<LsEntry> list = sftp.ls(path);
            final long expires = System.currentTimeMillis() / 1000 - 24*3600L*days;
            for (LsEntry entry : list){
            	if (entry.getFilename().charAt(0) == '.') {
            		continue;  // .和..
            	}
            	if (entry.getAttrs().isDir()){  //删除子目录
            		String child = directory+"/"+entry.getFilename();
            		count += deleteRecentFiles(child, days);
            	}else if (entry.getAttrs().getMTime() < expires){
            		sftp.rm(entry.getFilename());
            		count ++;
            	}
            }
            sftp.cd(rootPath);
            return count;
		}catch(SftpException ex){
			throw new IOException("删除文件失败", ex);
		}
	}
	
	public void disconnect() {
	    if (this.sftp != null){
	        if(this.sftp.isConnected()){
	            this.sftp.disconnect();  //sftp.quit(); sftp.exit();
	        }else if(this.sftp.isClosed()){
	        	logger.debug("sftp is closed already");
	        }
	    }
	}
	
	public String getRootPath() {
		return rootPath;
	}
	public String getLocalRootPath() {
		return localRootPath;
	}
	private final String canonicalPath(String path){
		if (path.charAt(0) != '/'){
			path = '/' + path;
		}
		if (!path.startsWith(rootPath)){
			path = rootPath + path;
		}
		return path;
	}
	
	private void connect() throws IOException{
		if (!setted){
			throw new IllegalStateException("未初始化Ftp连接，请先调用setProperties()");
		}
		if (sftp!=null && sftp.isConnected()){
			return;
		}
		
		Assert.notEmpty(props, "ftp setting is null");
		JSch jsch = new JSch();
		int port = 22;
		try{
			port = Integer.parseInt(props.getProperty(FTP_PORT));
		}catch(Exception e){}
		String host = props.getProperty(FTP_HOST);
		String username = props.getProperty(FTP_USER);
		String password = props.getProperty(FTP_PASS);
		try{
//			jsch.addIdentity("privateKey");  //
			Session sshSession = jsch.getSession(username, host, port);
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			logger.info("Session connected.");
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
		}catch (JSchException ex){
			throw new IOException("无法连接Ftp服务器", ex);
		}
	}
	private boolean exists(String path, final String fileName)throws IOException{
		try{
			final List<LsEntry> result = new ArrayList<LsEntry>();
		    LsEntrySelector selector = new LsEntrySelector(){
		    	@Override
				public int select(LsEntry entry) {
					if (entry.getFilename().equals(fileName)){
						result.add(entry);
						return ChannelSftp.LsEntrySelector.BREAK;
					}else{
						return ChannelSftp.LsEntrySelector.CONTINUE;
					}
				}
		    };
			sftp.ls(path, selector);
			
			int index = result.size() - 1;
			return index>=0 && (result.get(index).getFilename().equals(fileName));
		}catch(SftpException ex){
			throw new IOException("exists失败", ex);
		}
	}
	
	public class FileInfo{
		private String fileName;
		private boolean dir;
		private String createDate;
		private String lastModified;
		private long size;
		private String permission;
		
		public FileInfo(String fileName){
			this.setFileName(fileName);
		}
		public FileInfo(String fileName, boolean isDir){
			this.setFileName(fileName);
			this.setDir(isDir);
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getLastModified() {
			return lastModified;
		}
		public void setLastModified(String lastModified) {
			this.lastModified = lastModified;
		}
		public long getSize() {
			return size;
		}
		public void setSize(long size) {
			this.size = size;
		}
		public String getPermission() {
			return permission;
		}
		public void setPermission(String permission) {
			this.permission = permission;
		}
		public boolean isDir() {
			return dir;
		}
		public void setDir(boolean dir) {
			this.dir = dir;
		}
		public String getCreateDate() {
			return createDate;
		}
		public void setCreateDate(String created) {
			this.createDate = created;
		}
	}
}
