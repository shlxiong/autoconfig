package com.openxsl.config.dal.jdbc;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.KvPair;

/**
 * @author xiongsl
 * @lastModified 2016-06-06 增加方法find(id), exists(id), count(...)
 * @lastModified 2018-07-05 为了insert()返回值适应子类，增加泛化参数“K”-代表主键
 */
public class BasicDaoFacade<K extends Serializable, T> {
	protected DaoTemplate<T> template;
	
	/**
	 * 分页 dao模板
	 */
	protected PagedDaoTemplate<T> pagedTemplate;
	
	@Autowired
	@SuppressWarnings({"unchecked","rawtypes"})
	public void setTemplate(DaoTemplate<T> template){
		this.template = template;
		ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
		Class<T> entityClass = (Class)pt.getActualTypeArguments()[1];
		template.setGenericParameterType(entityClass);
		if (this.pagedTemplate==null && template instanceof PagedDaoTemplate) {
			this.pagedTemplate = (PagedDaoTemplate)template;
		}
	}
	
	public DaoTemplate<T> getTemplate(){
		return this.template;
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public void setPagedTemplate(PagedDaoTemplate<T> pagedDao) {
		this.pagedTemplate = pagedDao;
		ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
		Class<T> entityClass = (Class)pt.getActualTypeArguments()[1];
		pagedTemplate.setGenericParameterType(entityClass);
		
		if (template == null){
			template = pagedDao;
		}
	}
	public PagedDaoTemplate<T> getPagedTemplate() {
		return pagedTemplate;
	}
	
	

	public T find(K id){
		return template.find(id);
	}
	public boolean exists(K id){
		return template.find(id) != null;
	}
	public long count(String sql, Object... args){
		return template.getTotal(sql, args);
	}
	
	public T find(KvPair[] pairs, String orders, String... fields){
		QueryMap<Object> wheres = new QueryMap<Object>();
		for (KvPair pair : pairs){
			wheres.put(pair.getName(), pair.getValue());
		}
		try{
			return template.find(wheres, orders, fields);
		}finally{
			wheres.clear();
			wheres = null;
		}
	}
	
	public List<T> query(KvPair[] pairs, String... fields){
		QueryMap<Object> wheres = new QueryMap<Object>();
		for (KvPair pair : pairs){
			wheres.put(pair.getName(), pair.getValue());
		}
		try{
			return template.query(wheres, fields);
		}finally{
			wheres.clear();
			wheres = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public K insert(T entity){
		return (K)template.insert(entity);
	}
	public int delete(K id){
		return template.deleteById(id);
	}
	@SuppressWarnings("unchecked")
	public int update(T entity) {
		String pk = template.getSqlParser().getIdField();
		Assert.notNull(pk, "Entity Class has not @Id field");
		K id = (K)template.getSqlParser().getId(entity);
		Assert.notNull(id, String.format("Entity's %s(id) is null", pk));
		return template.update(entity, new QueryMap<K>(pk, id));
	}
	
	public int update(K id, KvPair... pairs){
		Assert.notEmpty(pairs, "'UPDATE-SET' 为空");
		QueryMap<Object> values = new QueryMap<Object>();
		for (KvPair pair : pairs){
			values.put(pair.getName(), pair.getValue());
		}
		return template.update(values, id);
	}
	
}
