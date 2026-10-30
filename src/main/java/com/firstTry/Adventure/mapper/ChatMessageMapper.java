package com.firstTry.Adventure.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.firstTry.Adventure.entity.ChatMessageEntity;

/**
 * 隐藏房间聊天记录
 *
 * 使用MyBatis-Plus的BaseMapper + Wrapper链式写法，无需XML
 *
 * @author Roger
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {

	/**
	 * 删除指定房间、指定时间之前的过期聊天记录（LambdaQueryWrapper链式删除）
	 *
	 * @param roomKey 房间key
	 * @param cutoff  截止时间（早于该时间的记录删除）
	 * @return 删除条数
	 */
	default int deleteExpired(String roomKey, Date cutoff) {
		return delete(Wrappers.<ChatMessageEntity>lambdaQuery()
				.eq(ChatMessageEntity::getRoomKey, roomKey)
				.lt(ChatMessageEntity::getCreatedAt, cutoff));
	}

	/**
	 * 查询房间聊天历史（按id倒序，最新的在前）
	 *
	 * @param roomKey 房间key
	 * @param beforeId 翻页游标：只取id小于该值的更早记录（首次加载传null）
	 * @param size 每页条数
	 * @return 按id倒序的记录列表
	 */
	default List<ChatMessageEntity> queryHistory(String roomKey, Long beforeId, int size) {
		return selectList(Wrappers.<ChatMessageEntity>lambdaQuery()
				.eq(ChatMessageEntity::getRoomKey, roomKey)
				.lt(beforeId != null, ChatMessageEntity::getId, beforeId)
				.orderByDesc(ChatMessageEntity::getId)
				.last("LIMIT " + size));
	}
}