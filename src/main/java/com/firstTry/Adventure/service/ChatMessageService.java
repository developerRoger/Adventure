package com.firstTry.Adventure.service;

import java.util.Date;
import java.util.List;

import com.firstTry.Adventure.entity.ChatMessageEntity;

/**
 * 隐藏房间聊天记录
 *
 * @author Roger
 */
public interface ChatMessageService {

	/**
	 * 保存一条聊天记录
	 *
	 * @param message
	 */
	void save(ChatMessageEntity message);

	/**
	 * 清理指定房间、指定时间之前的过期聊天记录
	 *
	 * @param roomKey 房间key
	 * @param cutoff  截止时间
	 * @return 删除条数
	 */
	int cleanExpired(String roomKey, Date cutoff);

	/**
	 * 查询房间聊天历史（按id倒序，最新的在前），beforeId 用于向上翻页
	 *
	 * @param roomKey 房间key
	 * @param beforeId 翻页游标（取id更小的更早记录），首次加载传null
	 * @param size 每页条数
	 * @return 记录列表
	 */
	List<ChatMessageEntity> queryHistory(String roomKey, Long beforeId, int size);
}