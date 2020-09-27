package com.openxsl.config.dal.jdbc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.dal.jdbc.anno.Index;
import com.openxsl.config.dal.jdbc.anno.RefEntity;
import com.openxsl.config.util.StringUtils;

//@JsonIgnoreProperties({"createBy", "createTime", "modifyBy", "modifyTime"})
public abstract class BaseEntity<PK extends Serializable> extends Entity<PK> {
    private static final long serialVersionUID = -3011550426322591805L;
    public static final String CREATE_BY_FIELD = "createBy";
    public static final String CREATE_TIME_FIELD = "createTime";
    public static final String MODIFY_BY_FIELD = "modifyBy";
    public static final String MODIFY_TIME_FIELD = "modifyTime";

    @Column
    private String createBy;
    @Column
    private Date createTime;
    @Column
    private String modifyBy;
    @Column
    private Date modifyTime;

    public BaseEntity() {
    }
    
    public String toString() {
		return JSON.toJSONString(this);
	}
    
    public String generDDLSql() {
    	Class<?> clazz = getClass(),
    			parentClazz = clazz.getSuperclass();
    	if (!clazz.isAnnotationPresent(Table.class)) {
    		return null;
    	}
    	String tableName = clazz.getAnnotation(Table.class).name();
    	StringBuilder buffer = new StringBuilder("CREATE TABLE `");
    	buffer.append(tableName).append("`(\n");
    	buffer.append("    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',\n");
    	List<Field> fields = new ArrayList<Field>();
    	while (parentClazz != Object.class && parentClazz != BaseEntity.class) {
    		fields.addAll(0, Arrays.asList(parentClazz.getDeclaredFields()));
    		parentClazz = parentClazz.getSuperclass();
    	}
    	fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    	List<TableIndex> indexes = new ArrayList<TableIndex>();
    	for (Field field : fields) {
    		if (field.isAnnotationPresent(Column.class)) {
    			buffer.append(this.generFieldDDL(field, indexes));
    		} else if (field.isAnnotationPresent(RefEntity.class)) {
    			for (Field refered : field.getType().getDeclaredFields()) {
    				buffer.append(this.generFieldDDL(refered, indexes));
    			}
    		}
    	}
    	buffer.append("    `create_by` VARCHAR(16), create_time DATETIME,\n")
    			.append("    `modify_by` VARCHAR(16), modify_time DATETIME,\n")
    			.append("    `deleted` char(1) default 'F',\n");
    	buffer.append("    PRIMARY KEY (`id`) \n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    	if (clazz.isAnnotationPresent(ApiModel.class)) {
    		String comment = clazz.getAnnotation(ApiModel.class).value();
    		buffer.append(" COMMENT='").append(comment).append("'");
    	}
    	buffer.append(";\n");
    	
    	if (clazz.isAnnotationPresent(Index.class)) {
    		indexes.add(new TableIndex(clazz.getAnnotation(Index.class)));
    	}
    	//CREATE UNIQUE INDEX `name` ON `table`(`column`);
    	for (TableIndex index : indexes) {
    		buffer.append("CREATE");
    		if (index.isUnique()) {
    			buffer.append(" UNIQUE ");
    		}
    		buffer.append(" INDEX `").append(index.getIndexName()).append("` ON `")
    			.append(tableName).append("`(`").append(index.getColumnName()).append("`); \n");
    	}
    	return buffer.toString();
    }
    
    private String generFieldDDL(Field field, List<TableIndex> indexes) {
    	StringBuilder buffer = new StringBuilder();
    	if (field.isAnnotationPresent(Column.class)) {
			Column column = field.getAnnotation(Column.class);
			String name = !"".equals(column.name()) ? column.name()
					: StringUtils.camelToSplitName(field.getName(), "_");
			String jdbcType = "";
			Class<?> ftype = field.getType();
			if (ftype == Integer.class || ftype == int.class || ftype == long.class || ftype == Long.class) {
				jdbcType = "INT";   //11 bit
			} else if (ftype == Boolean.class || ftype == boolean.class) {
				jdbcType = "CHAR(1)";
			} else if (ftype == Date.class) {
				jdbcType = "DATETIME";
			} else if (ftype == Float.class || ftype == float.class || ftype == double.class || ftype == Double.class) {
				jdbcType = "NUMERIC(16, 2)";
			} else if (ftype == BigDecimal.class) {
				jdbcType = "NUMERIC(32, 2)";
			} else if (ftype == java.sql.Date.class) {
				jdbcType = "DATE";
			} else if (ftype == Short.class || ftype == short.class) {
				jdbcType = "SMALLINT";
			} else {
				int len = column.length();
				jdbcType = (len > 255) ? "TEXT" : "VARCHAR("+(len==255?32:len)+')';
			}
			buffer.append("    `").append(name).append("` ").append(jdbcType);
			if (field.isAnnotationPresent(ApiModelProperty.class)) {
				String comment = field.getAnnotation(ApiModelProperty.class).value();
				buffer.append(" COMMENT '").append(comment+"'");
			}
			buffer.append(",\n");
			
			if (field.isAnnotationPresent(Index.class)) {
				indexes.add(new TableIndex(field.getAnnotation(Index.class),name));
			}
		}
    	return buffer.toString();
    }
    
    public String getCreateBy() {
        return this.createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getModifyBy() {
        return this.modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Date getModifyTime() {
        return this.modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
    
    class TableIndex {
    	private String indexName;
    	private String columnName;
    	private boolean unique;
    	
    	TableIndex(Index index, String column){
    		this.columnName = column==null ? index.column() : column;
    		Assert.isTrue(!"".equals(columnName), "column of index can not be empty");
    		this.indexName = index.value().equals("") ? ("IDX_"+this.columnName) : index.value();
    		this.unique = index.unique();
    	}
    	TableIndex(Index index){
    		this(index, null);
    	}
    	
		public String getIndexName() {
			return indexName;
		}
		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public boolean isUnique() {
			return unique;
		}
		public void setUnique(boolean unique) {
			this.unique = unique;
		}
    	
    }
}
