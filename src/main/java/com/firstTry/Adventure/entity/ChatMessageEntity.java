package com.firstTry.Adventure.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 隐藏房间聊天记录表
 *
 * @author Roger
 */
@Data
@TableName("chat_message")
public class ChatMessageEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 主键（数据库自增） */
	@TableId(type = IdType.AUTO)
	private Long id;
	/** 房间key */
	private String roomKey;
	/** 发送者连接标识sid */
	private String sender;
	/** 消息类型：text文本 / image图片 / video视频 */
	private String msgType;
	/** 文本内容，或媒体消息的标记文本（如 [图片] xxx.jpg、[视频] xxx.mp4） */
	private String content;
	/** 媒体文件名 */
	private String mediaName;
	/** 媒体MIME类型 */
	private String mediaMime;
	/** 图片base64内容（仅图片类型落真实内容） */
	private String mediaData;
	/** 创建时间 */
	private Date createdAt;
}