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
	/** 加入的房间名 */
	private String roomName;
	/** 进入websocket时间 */
	private Date enterTime;
}
