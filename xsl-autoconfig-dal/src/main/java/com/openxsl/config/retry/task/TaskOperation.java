package com.openxsl.config.retry.task;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 操作步骤
 * @author xiongsl
 */
@Entity
@Table(name="notify_task_operation", indexes= {
		@Index(name="IDX_TASK_OPERATION", columnList="taskId")
})
public class TaskOperation {
	@Id
	@Column(name="id")
	private long id;
    /** 任务ID */
	@Column(name="task_id", length=32)
    private long taskId;
    /** 步骤ID */
	@Column(name="step_id", length=8)
    private String stepId;
    /** 操作时间 */
	@Column(name="operate_date")
    private Date operateDate;
    /** 参数 */
	@Column(name="parameter", length=TaskContext.MAX_LEN_PARAMETER)
    private String parameter;
    /** 结果 */
	@Column(name="result", length=TaskContext.MAX_LEN_RESULT)
    private String result;
    /** 机器地址 */
	@Column(name="source_host", length=127)
    private String sourceHost;
    /** 对方IP */
	@Column(name="target_host", length=127)
    private String targetHost;
    /** 状态：是否成功 */
	@Column
    private short status;
	@Column
	private short retries;
	
//	private List<String> details;
	
	public TaskOperation() {}
	
	public TaskOperation(long taskId, String stepId){
		this.taskId = taskId;
		this.stepId = stepId;
	}

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public Date getOperateDate() {
        return operateDate;
    }

    public void setOperateDate(Date operateDate) {
        this.operateDate = operateDate;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    public String getSourceHost() {
		return sourceHost;
	}

	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}

	public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

	public short getRetries() {
		return retries;
	}

	public void setRetries(short retries) {
		this.retries = retries;
	}

//	public List<String> getDetails() {
//		return details;
//	}
//
//	public void setDetails(List<String> details) {
//		this.details = details;
//	}

}
