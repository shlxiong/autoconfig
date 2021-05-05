package com.openxsl.config.filedata.upload.model;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.Index;

@SuppressWarnings("serial")
@Table(name = "upload_file_info")
public class FileItem extends BaseEntity<Integer> {
	@Column
	private String itemName;
	@Column
	private String filePath;
	@Column
	private long fileSize;
	@Index
	@Column
	private String serviceId;
	@Index
	@Column
	private String dataId;
	@Column
	private int downCount;
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getDataId() {
		return dataId;
	}
	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public int getDownCount() {
		return downCount;
	}
	public void setDownCount(int downCount) {
		this.downCount = downCount;
	}

}
