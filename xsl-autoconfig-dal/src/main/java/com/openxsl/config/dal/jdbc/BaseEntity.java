package com.openxsl.config.dal.jdbc;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.springframework.util.Assert;

import com.openxsl.config.dal.jdbc.anno.Index;
import com.openxsl.config.util.StringUtils;

//@JsonIgnoreProperties({"createBy", "createTime", "modifyBy", "modifyTime"})
public abstract class BaseEntity<PK extends Serializable> extends Entity<PK> {
    private static final long serialVersionUID = -3011550426322591805L;
    public static final String CREATE_BY_FIELD = "createBy";
    public static final String CREATE_TIME_FIELD = "createTime";
    public static final String MODIFY_BY_FIELD = "modifyBy";
    public static final String MODIFY_TIME_FIELD = "modifyTime";

    @Column
    private transient String createBy;
    @Column
    private transient Date createTime;
    @Column
    private transient String modifyBy;
    @Column
    private transient Date modifyTime;

    public BaseEntity() {
    }
    
    public String generDDLSql() {
    	StringBuilder buffer = new StringBuilder("CREATE TABLE `");
    	Class<?> clazz = getClass();
    	String tableName = clazz.getAnnotation(Table.class).name();
    	buffer.append(tableName).append("`(\n");
    	buffer.append("    id INT NOT NULL AUTO_INCREMENT COMMENT '主键',\n");
    	List<TableIndex> indexes = new ArrayList<TableIndex>();
    	for (Field field : clazz.getDeclaredFields()) {
    		if (field.isAnnotationPresent(Column.class)) {
    			Column column = field.getAnnotation(Column.class);
    			String name = !"".equals(column.name()) ? column.name()
    					: StringUtils.camelToSplitName(field.getName(), "_");
    			String jdbcType = "";
    			Class<?> ftype = field.getType();
    			if (ftype == Integer.class || ftype == int.class || ftype == long.class || ftype == Long.class) {
    				jdbcType = "INT";   //11 bit
    			} else if (ftype == String.class || ftype == char[].class) {
    				jdbcType = "VARCHAR(32)";
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
    			}
    			buffer.append("    `").append(name).append("` ").append(jdbcType).append(",\n");
    			
    			if (field.isAnnotationPresent(Index.class)) {
    				indexes.add(new TableIndex(field.getAnnotation(Index.class),name));
    			}
    		}
    	}
    	buffer.append("    `create_by` VARCHAR(16), create_time DATETIME,\n")
    			.append("    `modify_by` VARCHAR(16), modify_time DATETIME,\n")
    			.append("    `deleted` char(1) default 'F',");
    	buffer.append(" PRIMARY KEY (`id`) \n) ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
    	
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
