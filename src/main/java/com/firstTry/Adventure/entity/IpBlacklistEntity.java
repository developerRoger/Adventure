package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * IP黑名单表
 *
 * @author Roger
 */
@Data
@TableName("ip_blacklist")
public class IpBlacklistEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 主键（数据库自增） */
	@TableId(type = IdType.AUTO)
	private Long id;
	/** 被限制的IP */
	private String ip;
	/** 备注（可选，记录限制原因） */
	private String remark;
	/** 创建时间 */
	private Date createTime;
}