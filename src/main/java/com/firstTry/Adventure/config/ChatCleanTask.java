package com.firstTry.Adventure.config;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.firstTry.Adventure.middleware.WebSocketServer;
import com.firstTry.Adventure.service.ChatMessageService;

/**
 * 隐藏房间聊天记录定时清理：每天凌晨3点清除超过10天的数据
 *
 * @author Roger
 */
@Component
public class ChatCleanTask {

	private static final Log log = LogFactory.getLog(ChatCleanTask.class);
	// 聊天记录保留天数
	private static final long KEEP_DAYS = 10L;

	@Autowired
	private ChatMessageService chatMessageService;

	/**
	 * 定时清理过期聊天记录（每天 03:00 执行）
	 */
	@Scheduled(cron = "0 0 3 * * ?")
	public void cleanExpiredChat() {
		Date cutoff = new Date(System.currentTimeMillis() - KEEP_DAYS * 24 * 60 * 60 * 1000);
		int deleted = chatMessageService.cleanExpired(WebSocketServer.HIDDEN_ROOM_KEY, cutoff);
		log.info("清理隐藏房间过期聊天记录完成，已删除 " + deleted + " 条");
	}
}