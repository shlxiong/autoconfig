package com.openxsl.config.dal.jdbc;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.BeanUtils;

/**
 * 增删改全部使用 Selective操作 
 * 		<Mapper, Entity, PK>
 * 
 * @author shuilin.xiong
 */
public class BaseService<M extends Mapper<T>, T extends Entity<PK>, PK extends Serializable> {
    @Autowired
    protected M mapper;
    @Autowired
    private SqlSession sqlSession;
    
    private final Class<?> entityClass;
    private final Class<?> mapperClass;
    
    public BaseService() {
    	ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
    	entityClass = (Class<?>)pt.getActualTypeArguments()[1];
    	mapperClass = (Class<?>)pt.getActualTypeArguments()[0];
    }

    public int insert(T entity) {
    	return this.mapper.insertSelective(entity);
    }

    public int insertBatch(List<T> list) {
    	int count = 0;
        for (T obj : list) {
            count += this.mapper.insertSelective(obj);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
	public int delete(T entity) {
        return this.delete(entity.getId());
    }
    
    @SuppressWarnings("unchecked")
	public int delete(PK... ids) {
    	int count = 0;
    	for (PK id : ids) {
    		T entity = (T)BeanUtils.instantiate(entityClass);
    		entity.setId(id);
    		entity.setDeleted(Entity.TRUE);
    		count += this.mapper.updateByPrimaryKeySelective(entity);
    	}
    	return count;
    }

//    public void deleteTrue(PK id) {
//        this.mapper.deleteByPrimaryKey(id);   //物理删除
//    }

    public int update(T entity) {
    	return this.mapper.updateByPrimaryKeySelective(entity);
    }
    public int update(T entity, String whereFields) {
    	Assert.isTrue(whereFields!=null && whereFields.length()>0, "查询条件不能为空");
    	Example example = new Example(entityClass);
    	Criteria criteria = example.createCriteria();
    	for (String field : whereFields.split(",")) {
    		criteria.andEqualTo(field, BeanUtils.getValue(entity, field));
    	}
    	return mapper.updateByExampleSelective(entity, example);
    }
    public int updateBatch(List<T> entitys) {
    	int count = 0;
        for (T obj : entitys) {
            count += this.mapper.updateByPrimaryKeySelective(obj);
        }
        return count;
    }

    public T find(T entity) {
    	entity.setDeleted(Entity.FALSE);
        return (T) this.mapper.selectOne(entity);
    }

    public T get(Serializable id) {
        return (T) this.mapper.selectByPrimaryKey(id);
    }

    public List<T> list(T entity) {
    	entity.setDeleted(Entity.FALSE);
        return this.mapper.select(entity);
    }

    public Long selectCount(T entity) {
    	entity.setDeleted(Entity.FALSE);
        return new Long(this.mapper.selectCount(entity));
    }

    public List<T> selectAll() {
//        return this.mapper.selectAll();
    	return this.selectByExample(new Example(entityClass));
    }

    public List<T> selectByExample(Example example) {
        example.and(example.createCriteria().andEqualTo(Entity.DELETED_FIELD, Entity.FALSE));
        return this.mapper.selectByExample(example);
    }

    public int selectCountByExample(Example example) {
        example.and(example.createCriteria().andEqualTo(Entity.DELETED_FIELD, Entity.FALSE));
        return this.mapper.selectCountByExample(example);
    }

//    /**
//     * github.pagehelper的分页，太傻了
//     * @param example
//     * @param paginate
//     */
//    @Deprecated
//    public Page<T> page(Example example, Pagination paginate) {
//    	example.and(example.createCriteria().andEqualTo(Entity.DELETED_FIELD, Entity.FALSE));
//        if (paginate.getOrderBy() != null) {
//            PageHelper.orderBy(paginate.getOrderBy().toString());
//        }
//        PageHelper.startPage(paginate.getPageNo(), paginate.getPageSize());
//        List<T> list = this.mapper.selectByExample(example);
//        PageInfo<T> pageInfo = new PageInfo<T>(list);
//    	return new Page<T>(paginate, (int)pageInfo.getTotal())
//    				.setData(pageInfo.getList());
//    }

    /**
     * 我们自己的分页组件，直接操作sqlsession
     * @param sqlId
     * @param parameter
     * @param page
     */
    public Page<T> queryForPage(String sqlId, QueryMap<Object> parameter, Pagination page) {
    	parameter.put(Entity.DELETED_FIELD, Entity.FALSE);
    	parameter.put("page", page);
    	sqlId = mapperClass.getName() + "." + sqlId;
    	List<T> pagedData = sqlSession.selectList(sqlId, parameter);
    	return new Page<T>(page, pagedData);
    }
    
	public Page<T> queryForPage(QueryMap<Object> parameter, Pagination page) {
    	if (!BaseMapper.class.isAssignableFrom(mapperClass)) {
    		throw new IllegalStateException("mapperClass must be BaseMapper");
    	}
    	parameter.put(Entity.DELETED_FIELD, Entity.FALSE);
    	parameter.put("page", page);
    	return new Page<T>(page, ((BaseMapper<T>)mapper).queryForPage(parameter));
    }
//
//    protected String buildStatNameById(String id) {
//        String namespace = this.getClass().getName().replace("biz.", "dao.").replace("Biz", "Mapper");
//        return namespace + "." + id;
//    }
	
	/**
	 * mapper.getKeyValue: List<`key`,`value`>
	 * @param params 查询条件
	 */
	public Map<String,String> getKeyValues(QueryMap<Object> params){
		Map<String, String> mapData = new HashMap<String, String>();
    	try {
    		params.put(Entity.DELETED_FIELD, Entity.FALSE);
    		((BaseMapper<T>)mapper).getKeyValue(params).stream().forEach(e -> {
	    		mapData.put(e.get("key"), e.get("value"));
	    	});
    	} catch (NullPointerException npe) {}
    	return mapData;
	}

    protected String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}
