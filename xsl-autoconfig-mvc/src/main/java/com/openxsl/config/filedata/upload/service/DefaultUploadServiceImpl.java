package com.openxsl.config.filedata.upload.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.filedata.upload.dao.UploadDao;
import com.openxsl.config.filedata.upload.model.FileItem;

@Service
public class DefaultUploadServiceImpl implements UploadService {
	@Autowired
	private UploadDao dao;
	
	@Override
	public void upload(String name, String storePath, Map<String,?> attributes) {
		//serviceId, dataId, fileSize
		FileItem fileItem = JSON.parseObject(JSON.toJSONString(attributes), FileItem.class);
		fileItem.setItemName(name);
		fileItem.setFilePath(storePath);
		this.save(fileItem);
	}

	@Override
	public void download(String file) {
		// TODO Auto-generated method stub
	}
	
	public int save(FileItem fileItem) {
		dao.deleteFileItem(fileItem);
		return dao.insertFileItem(fileItem);
	}

	@Override
	public List<FileItem> queryImages(String serviceId,String beginDate,String endDate){
		Map<String,String> params = new HashMap<>();
		params.put("serviceId",serviceId);
		params.put("beginDate",beginDate);
		params.put("endDate",endDate);
		return dao.queryFileItems(params);
	}

}
