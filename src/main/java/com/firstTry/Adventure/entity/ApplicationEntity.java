package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-07-31 20:42:21
 */
public class ApplicationEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//应用编码
	private String code;
	//应用名称
	private String name;
	//应用状态
	private String status;
	//应用描述
	private String description;
	//显示顺序
	private Integer showOrder;
	//创建人
	private String createBy;
	//创建时间
	private Date createTime;
	//更新人
	private String updateBy;
	//更新时间
	private Date updateTime;
	//菜单
	List<MenuEntity> listMenu;
	
	public List<MenuEntity> getListMenu() {
		return listMenu;
	}
	public void setListMenu(List<MenuEntity> listMenu) {
		this.listMenu = listMenu;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getShowOrder() {
		return showOrder;
	}
	public void setShowOrder(Integer showOrder) {
		this.showOrder = showOrder;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "ApplicationEntity [code=" + code + ", name=" + name + ", status=" + status + ", description="
				+ description + ", showOrder=" + showOrder + ", createBy=" + createBy + ", createTime=" + createTime
				+ ", updateBy=" + updateBy + ", updateTime=" + updateTime + ", listMenu=" + listMenu + "]";
	}
}
