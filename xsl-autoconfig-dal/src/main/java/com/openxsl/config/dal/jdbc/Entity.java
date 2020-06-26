package com.openxsl.config.dal.jdbc;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 数据库实体对象，逻辑删除(deleted)和Id字段
 * @author xiongsl
 */
@SuppressWarnings("serial")
public class Entity<PK extends Serializable> implements Serializable {
	public static final String DELETED_FIELD = "deleted";
	static final String TRUE = "T";
	static final String FALSE = "F";
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private PK id;
    private transient String deleted = FALSE;

    public PK getId() {
        return this.id;
    }

    public void setId(PK id) {
        this.id = id;
    }

    public String getDeleted() {
        return this.deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

}
