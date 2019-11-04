package com.openxsl.config.retry.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 明细数据
 * @author xiongsl
 */
@Entity
@Table(name="notify_task_detail", indexes= {
		@Index(name="IDX_TASK_DETAIL", columnList="taskId")
})
public class TaskDetail {
	@Column(name="task_id")
	private long taskId;
	@Column(name="order_no", length=36)
	private String orderNo;
	@Column(name="request_str", length=255)
	private String requestStr;
	@Column(name="resp_str", length=255)
	private String respStr;
	@Column(length=8)
	private String status;
	
	public TaskDetail(){}
	public TaskDetail(long taskId){
		this.setTaskId(taskId);
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRequestStr() {
		return requestStr;
	}
	public void setRequestStr(String requestStr) {
		this.requestStr = requestStr;
	}
	public String getRespStr() {
		return respStr;
	}
	public void setRespStr(String respStr) {
		this.respStr = respStr;
	}
	
}
