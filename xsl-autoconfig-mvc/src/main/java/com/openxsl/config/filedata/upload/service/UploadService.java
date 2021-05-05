package com.openxsl.config.filedata.upload.service;

import java.util.List;
import java.util.Map;

import com.openxsl.config.filedata.upload.model.FileItem;

public interface UploadService {
	
	public void upload(String name, String storePath, Map<String,?> attributes);
	
	public void download(String file);
	
	public List<FileItem> queryImages(String serviceId,String beginDate,String endDate);

}
