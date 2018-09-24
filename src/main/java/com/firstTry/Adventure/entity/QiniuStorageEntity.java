package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-09-22 15:57:37
 */
public class QiniuStorageEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	//默认未删除
	private final static int  UNDELETED=0;
	//已删除
	private final static int  DELETED=1;
	
	public  QiniuStorageEntity() {
		createTime=new Date();
		updateTime=new Date();
		deleteCode=UNDELETED;
	}

	
	//
	private Long id;
	//
	private String name;
	//七牛云地址
	private String url;
	//备注
	private String remark;
	//(0，为未删除，1，为已删除)
	private Integer deleteCode;
	//创建人
	private String createBy;
	//创建时间
	private Date createTime;
	//更新人
	private String updateBy;
	//更新时间
	private Date updateTime;

	/**
	 * 设置：
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置：
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取：
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置：七牛云地址
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 获取：七牛云地址
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 设置：备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * 获取：备注
	 */
	public String getRemark() {
		return remark;
	}
	/**
	 * 设置：(0，为未删除，1，为已删除)
	 */
	public void setDeleteCode(Integer deleteCode) {
		this.deleteCode = deleteCode;
	}
	/**
	 * 获取：(0，为未删除，1，为已删除)
	 */
	public Integer getDeleteCode() {
		return deleteCode;
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
	@Override
	public String toString() {
	
				return "QiniuStorageEntity [id="+id+""+
					",name="+name+""+
					",url="+url+""+
					",remark="+remark+""+
					",deleteCode="+deleteCode+""+
					",createBy="+createBy+""+
					",createTime="+createTime+""+
					",updateBy="+updateBy+""+
					",updateTime="+updateTime+""+
		      "]";}
}
