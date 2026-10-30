package com.firstTry.Adventure.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.firstTry.Adventure.entity.ChatMessageEntity;
import com.firstTry.Adventure.mapper.ChatMessageMapper;
import com.firstTry.Adventure.service.ChatMessageService;

/**
 * 隐藏房间聊天记录
 *
 * @author Roger
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

	@Autowired
	private ChatMessageMapper chatMessageMapper;

	@Override
	public void save(ChatMessageEntity message) {
		chatMessageMapper.insert(message);
	}

	@Override
	public int cleanExpired(String roomKey, Date cutoff) {
		return chatMessageMapper.deleteExpired(roomKey, cutoff);
	}

	@Override
	public List<ChatMessageEntity> queryHistory(String roomKey, Long beforeId, int size) {
		return chatMessageMapper.queryHistory(roomKey, beforeId, size);
	}
}