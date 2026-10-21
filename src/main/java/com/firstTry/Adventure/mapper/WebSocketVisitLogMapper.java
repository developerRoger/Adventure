package com.firstTry.Adventure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.firstTry.Adventure.entity.WebSocketVisitLogEntity;

/**
 * websocket访问记录
 * 
 * 使用MyBatis-Plus的BaseMapper + Wrapper链式写法，无需XML
 * 
 * @author Roger
 */
@Mapper
public interface WebSocketVisitLogMapper extends BaseMapper<WebSocketVisitLogEntity> {

	/**
	 * 根据连接标识更新加入的房间名（LambdaUpdateWrapper链式更新）
	 * 
	 * @param sid
	 * @param roomName
	 */
	default void updateRoomName(String sid, String roomName) {
		// 构建更新条件：sid匹配，set设置room_name字段
		update(null, Wrappers.<WebSocketVisitLogEntity>lambdaUpdate()
				.eq(WebSocketVisitLogEntity::getSid, sid)
				.set(WebSocketVisitLogEntity::getRoomName, roomName));
	}

	/**
	 * 查询所有访问记录（按进入时间倒序，LambdaQueryWrapper链式查询）
	 * 
	 * @return
	 */
	default List<WebSocketVisitLogEntity> queryList() {
		return selectList(Wrappers.<WebSocketVisitLogEntity>lambdaQuery()
				.orderByDesc(WebSocketVisitLogEntity::getEnterTime)
				.orderByDesc(WebSocketVisitLogEntity::getId));
	}
}
