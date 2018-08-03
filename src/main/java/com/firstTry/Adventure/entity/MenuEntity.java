package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-08-02 16:33:50
 */
public class MenuEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//菜单Id
	private Long id;
	//应用编码
	private String appCode;
	//菜单层级
	private Integer level;
	//父id
	private Long parentId;
	//菜单名称
	private String name;
	//菜单Url
	private String url;
	//
	private String httpUrl;
	//展示名称
	private String showName;
	//展示顺序
	private Integer showOrder;
	//创建人
	private String createBy;
	//创建时间
	private Date createTime;
	//更新人
	private String updateBy;
	//更新时间
	private Date updateTime;

	/**
	 * 设置：菜单Id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 获取：菜单Id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置：应用编码
	 */
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	/**
	 * 获取：应用编码
	 */
	public String getAppCode() {
		return appCode;
	}
	/**
	 * 设置：菜单层级
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}
	/**
	 * 获取：菜单层级
	 */
	public Integer getLevel() {
		return level;
	}
	/**
	 * 设置：父id
	 */
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	/**
	 * 获取：父id
	 */
	public Long getParentId() {
		return parentId;
	}
	/**
	 * 设置：菜单名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取：菜单名称
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置：菜单Url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 获取：菜单Url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 设置：
	 */
	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}
	/**
	 * 获取：
	 */
	public String getHttpUrl() {
		return httpUrl;
	}
	/**
	 * 设置：展示名称
	 */
	public void setShowName(String showName) {
		this.showName = showName;
	}
	/**
	 * 获取：展示名称
	 */
	public String getShowName() {
		return showName;
	}
	/**
	 * 设置：展示顺序
	 */
	public void setShowOrder(Integer showOrder) {
		this.showOrder = showOrder;
	}
	/**
	 * 获取：展示顺序
	 */
	public Integer getShowOrder() {
		return showOrder;
	}
	/**
	 * 设置：创建人
	 */
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	/**
	 * 获取：创建人
	 */
	public String getCreateBy() {
		return createBy;
	}
	/**
	 * 设置：创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 获取：创建时间
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * 设置：更新人
	 */
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	/**
	 * 获取：更新人
	 */
	public String getUpdateBy() {
		return updateBy;
	}
	/**
	 * 设置：更新时间
	 */
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	/**
	 * 获取：更新时间
	 */
	public Date getUpdateTime() {
		return updateTime;
	}
}
