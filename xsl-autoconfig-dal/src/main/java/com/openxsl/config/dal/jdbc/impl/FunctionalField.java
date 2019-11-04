package com.openxsl.config.dal.jdbc.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.springframework.util.Assert;

public class FunctionalField {
	@Target({METHOD, FIELD})
	@Retention(RUNTIME)
	public @interface FunctColumn{
		String sql();
		String alias() default "";
	}
	
	public FunctionalField(Field field){
		FunctColumn anno = field.getAnnotation(FunctColumn.class);
		Assert.notNull(anno, field.getName()+"须使用注解 @FunctColumn");
		this.setSql(anno.sql());
		this.setAlias( "".equals(anno.alias())?field.getName():anno.alias() );
	}
	
	private String sql;
	private String alias;
	
	public String toSql(){
		return new StringBuilder(sql).append(" as ").append(alias)
				.toString();
	}
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}

}
