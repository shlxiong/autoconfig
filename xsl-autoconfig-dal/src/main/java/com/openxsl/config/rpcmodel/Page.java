package com.openxsl.config.rpcmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 分页数据，页码从0开始
 * @author 001327-xiongsl
 * @param <T>
 */
@SuppressWarnings("serial")
public class Page<T> implements Serializable{
	private int pageNo;         //当前页码
	private int pageSize;       //分页大小
	private int total;          //总记录数
	private int start;          //当前页开始位置
	private int pages;          //总页数
	@JSONField(name="rows")     //bootstrap-table结果名
	private List<T> results;    //数据
	
	public Page(){}
	public Page(int pageNo, int pageSize, int total) {
		setPageSize(pageSize);
		setPageNo(pageNo);
		setTotal(total);
	}
	
	public Page(int pageNo, int pageSize, List<T> allData) {
		Assert.notNull(allData, "结果为空");
		setPageSize(pageSize);
		setPageNo(pageNo);
		setTotal(allData.size());
		setResults(allData);
	}
	
	public void add(T data){
		if (results == null){
			results = new ArrayList<T>(pageSize);
		}
		results.add(data);
	}

	public int getStart() {
		return start;
	}
	public int getEnd() {
		int end = start + pageSize;
		return Math.min(end, total) - 1;
	}

	public int getPageSize() {
		return pageSize;
	}
	
	public int getPageNo() {
		return pageNo;
	}
	
	public int getPages() {
		return pages;
	}

	public long getTotal() {
		return total;
	}
	
	public boolean hasNext(){
		return total > start + pageSize;
	}
	public boolean hasPrevious(){
		return pageNo > 0;
	}
	
	private void setPageSize(int count) {
		this.pageSize = count;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
		this.start = pageNo * pageSize;
	}
	
	public void setTotal(int total) {
		this.total = total;
		this.pages = pageSize == 0 ? 1 : 
			((total%pageSize==0) ? total/pageSize : (total/pageSize + 1));
	}


	public List<T> getResults() {
		return results;
	}
	public Page<T> setResults(List<T> pageData) {
		this.results = pageData;
		return this;
	}
	
}
