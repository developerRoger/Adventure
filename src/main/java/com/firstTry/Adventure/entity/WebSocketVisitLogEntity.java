package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * websocket访问记录表
 * 
 * @author Roger
 */
@Data
@TableName("websocket_visit_log")
public class WebSocketVisitLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 主键 */
	private Long id;
	/** websocket连接标识 */
	private String sid;
	/** 客户端IP */
	private String ip;
	/** 省份（IP归属地解析，离线ip2region库，省/市精度） */
	private String province;
	/** 城市 */
	private String city;
	/** 区/县（IP定位一般只能到市精度，暂不填充） */
	private String district;
	/** 加入的房间名 */
	private String roomName;
	/** 进入websocket时间 */
	private Date enterTime;
}
