package com.openxsl.config.retry.task;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * 任务表（关联明细表）
 * @author xiongsl
 */
@Entity
@Table(name="notify_task_info")
@NamedQueries({
	@NamedQuery(name="queryByStatusAndStep",
			query="SELECT t.* FROM notify_task_info t left join notify_task_operation o"
				 + " on t.id=o.task_id and t.step_id=o.step_id and t.status=? "
				 + "WHERE t.status=o.status and t.step_id=?")
})
public class Task {
	@Id
	@Column(name="id")
	private long id;
    /** 服务ID */
	@Column(name="service_id", length=32)
    private String serviceId;
	/** 业务系统ID  */
	@Column(name="biz_sys", length=32)
	private String bizSys;
	/** 业务主键  */
	@Column(name="data_id", length=64)
    private String dataId;
    /** 当前步骤  */
	@Column(name="step_id", length=8)
    private String stepId;
    /** 支付系统的参数  */
	@Column(length=TaskContext.MAX_LEN_PARAMETER)
    private String parameter;
    /** 银行返回结果 */
	@Column(length=255)
    private String response;
	/** 对方查询主键 */
	@Column(length=36)
	private String callbackId;
    /** 银行即时返回的异步响应 */
	@Column(name="async_resp", length=TaskContext.MAX_LEN_RESULT)
    private String asyncResp;
    /** 失败重试次数  */
	@Column(name="retries")
    private short retries;
    /** 状态  */
	@Column(name="status")
    private short status;
    
    /** 日期 */
	@Column(name="create_date")
    private Date createDate;
	@Column(name="last_modified")
    private Date lastModified;
	
	/**
	 * 短信渠道（特有）
	 */
	@Column
	private String channel;
	/**
	 * 接收手机号（特有）
	 */
	@Column
	private String receiver;
	
//	/** parameters中的detail */
//	private List<String> details;
    
    public Task() {}
    
    public Task(String serviceId, String stepId){
    	this.setServiceId(serviceId);
    	this.setStepId(stepId);
    }

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public short getRetries() {
        return retries;
    }

    public void setRetries(short retries) {
        this.retries = retries;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

	public String getBizSys() {
		return bizSys;
	}

	public void setBizSys(String bizSys) {
		this.bizSys = bizSys;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getAsyncResp() {
		return asyncResp;
	}

	public void setAsyncResp(String asyncResp) {
		this.asyncResp = asyncResp;
	}

//	public List<String> getDetails() {
//		return details;
//	}
//
//	public void setDetails(List<String> details) {
//		this.details = details;
//	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getCallbackId() {
		return callbackId;
	}

	public void setCallbackId(String callbackId) {
		this.callbackId = callbackId;
	}

}
