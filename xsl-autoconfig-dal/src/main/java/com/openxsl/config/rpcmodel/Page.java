package com.openxsl.config.rpcmodel;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 分页数据，页码从0开始
 * @author 001327-xiongsl
 * @param <T>
 */
@SuppressWarnings("serial")
public class Page<T> extends Pagination{
	@JSONField(name="rows")     //bootstrap-table结果名
	private List<T> data;       //数据
	
	public Page(){
		super();
	}
	public Page(int pageNo, int pageSize, int total) {
		super(pageNo, pageSize);
		this.setTotal(total);
	}
	public Page(Pagination pagination, int totalNum) {
		this(pagination.getPageNo(), pagination.getPageSize(), totalNum);
	}
	
	public Page(int pageNo, int pageSize, List<T> allData) {
		this(pageNo, pageSize, allData==null?0:allData.size());
		this.setData(allData);
	}
	
	/**
	 * 通过Pagination和List构造，用于BaseService.queryForPage()
	 * @param pagination 分页对象
	 * @param data 分页过后的数据
	 */
	public Page(Pagination pagination, List<T> data) {
		this(pagination.getPageNo(), pagination.getPageSize(), data);
		this.setTotal(pagination.getTotal());
	}
	
//	public void add(T data){
//		if (results == null){
//			results = new ArrayList<T>(pageSize);
//		}
//		results.add(data);
//	}
	
	public int getStart(int... pageNo) {
		int pn = pageNo.length>0 ? pageNo[0] : this.getPageNo();
		return pn * this.getPageSize();
	}
	
	public int getEnd(int... pageNo) {
		int pn = pageNo.length>0 ? pageNo[0] : this.getPageNo();
		return Math.min(this.getTotal(), (pn+1) * this.getPageSize());
	}
	
	public boolean hasNextPage() {
		return this.getPageNo() < this.getPages()-1;
	}
	public boolean hasPrevPage() {
		return this.getPageNo() > 0;
	}
	
	public List<T> getPageData(){
		if (data == null) {
			return Collections.emptyList();
		}
		return (this.getTotal() > data.size()) ? data
				: data.subList(this.getStart(), this.getEnd());
	}
	//bootstrap-table结果名
	public List<T> getRows() {
		return getPageData();
	}
	
	public List<T> getData() {
		return data;
	}
	public Page<T> setData(List<T> allData) {
		this.data = allData;
		return this;
	}
	
}
