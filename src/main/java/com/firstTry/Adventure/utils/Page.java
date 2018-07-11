package com.firstTry.Adventure.utils;

import java.io.Serializable;
import java.util.List;

/***
 * 
 * 分页支持
 */
public class Page<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	// 前端传入-每页显示多少
	private int pageSize;
	// 前端传入-页码
	private int pageNumber;
	// 总数
	private int total;
	// 业务查询条件
	private T queryObj;
	// 数据列表
	private List<T> rows;

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

	public T getQueryObj() {
		return queryObj;
	}

	public void setQueryObj(T queryObj) {
		this.queryObj = queryObj;
	}
}
