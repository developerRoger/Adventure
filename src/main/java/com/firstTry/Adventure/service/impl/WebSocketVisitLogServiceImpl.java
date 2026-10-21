package com.firstTry.Adventure.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.firstTry.Adventure.entity.WebSocketVisitLogEntity;
import com.firstTry.Adventure.mapper.WebSocketVisitLogMapper;
import com.firstTry.Adventure.service.WebSocketVisitLogService;

/**
 * websocket访问记录
 * 
 * @author Roger
 */
@Service("webSocketVisitLogService")
public class WebSocketVisitLogServiceImpl implements WebSocketVisitLogService {

	@Autowired
	private WebSocketVisitLogMapper webSocketVisitLogDao;

	@Override
	public void save(WebSocketVisitLogEntity visitLog) {
		// BaseMapper提供insert方法
		webSocketVisitLogDao.insert(visitLog);
	}

	@Override
	public void updateRoomName(String sid, String roomName) {
		webSocketVisitLogDao.updateRoomName(sid, roomName);
	}

	@Override
	public List<WebSocketVisitLogEntity> queryList() {
		return webSocketVisitLogDao.queryList();
	}

}
