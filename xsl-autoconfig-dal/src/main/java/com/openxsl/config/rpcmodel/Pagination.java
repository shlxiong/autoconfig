package com.openxsl.config.rpcmodel;

import java.io.Serializable;

import com.openxsl.config.rpcmodel.QueryMap.Orderby;

/**
 * 分页参数，第一页pageNo=0
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
public class Pagination implements Serializable{
	private int pageNo;         //当前页码
	private int pageSize;       //分页大小
	private int total;          //总记录数
	private int pages;          //总页数
	private Orderby orderBy;

	public Pagination(){
		this(0, 10);
	}

	public Pagination(int pageNo, int pageSize) {
		this.setPageNo(pageNo);
		this.setPageSize(pageSize);
	}
	
	public int getStart(int pageNo) {
		return pageNo * pageSize;
	}
	public int getEnd(int pageNo) {
		return Math.min((pageNo+1)*pageSize, total);
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		this.pages = (total%pageSize == 0) ? total/pageSize 
				: (total/pageSize + 1);
	}
	
	public int getPages() {
		return pages;
	}

	public Orderby getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(Orderby orderBy) {
		this.orderBy = orderBy;
	}
	

}
