package com.firstTry.Adventure.service;

import java.util.List;

import com.firstTry.Adventure.entity.WebSocketVisitLogEntity;

/**
 * websocket访问记录
 * 
 * @author Roger
 */
public interface WebSocketVisitLogService {

	/**
	 * 保存一条访问记录（连接建立时）
	 * 
	 * @param visitLog
	 */
	void save(WebSocketVisitLogEntity visitLog);

	/**
	 * 根据连接标识更新加入的房间名
	 * 
	 * @param sid
	 * @param roomName
	 */
	void updateRoomName(String sid, String roomName);

	/**
	 * 查询所有访问记录（按进入时间倒序）
	 * 
	 * @return
	 */
	List<WebSocketVisitLogEntity> queryList();
}
