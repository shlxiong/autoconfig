package com.openxsl.config.filedata.upload.dao;

import java.util.List;
import java.util.Map;

import com.openxsl.config.filedata.upload.model.FileItem;
import com.openxsl.config.filedata.upload.model.UploadConfig;

public interface UploadDao {
	
	public List<UploadConfig> getAllUploadConfigs();
	
	public int insertFileItem(FileItem fileItem);
	
	public int deleteFileItem(FileItem fileItem);
	
	public List<FileItem> queryFileItems(Map<String,String> params);
	
}
