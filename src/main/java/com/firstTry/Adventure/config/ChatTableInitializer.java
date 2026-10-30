package com.firstTry.Adventure.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 隐藏房间聊天记录表初始化：应用启动时自动建表（表不存在则创建），
 * 失败时仅记录日志不中断启动，可手动执行 src/main/resources/sql/chat_message.sql
 *
 * @author Roger
 */
@Component
public class ChatTableInitializer {

	private static final Log log = LogFactory.getLog(ChatTableInitializer.class);

	private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS chat_message ("
			+ "  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',"
			+ "  room_key VARCHAR(16) NOT NULL COMMENT '房间key',"
			+ "  sender VARCHAR(64) DEFAULT NULL COMMENT '发送者连接标识sid',"
			+ "  msg_type VARCHAR(16) NOT NULL COMMENT '消息类型：text/image/video',"
			+ "  content TEXT COMMENT '文本内容或媒体标记文本',"
			+ "  media_name VARCHAR(255) DEFAULT NULL COMMENT '媒体文件名',"
			+ "  media_mime VARCHAR(64) DEFAULT NULL COMMENT '媒体MIME类型',"
			+ "  media_data LONGTEXT COMMENT '图片base64内容（仅图片）',"
			+ "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
			+ "  PRIMARY KEY (id),"
			+ "  KEY idx_room_created (room_key, created_at)"
			+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐藏房间聊天记录'";

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	public void initTable() {
		try {
			new JdbcTemplate(dataSource).execute(CREATE_SQL);
			log.info("隐藏房间聊天记录表 chat_message 已就绪");
		} catch (Exception e) {
			log.error("初始化 chat_message 表失败，请手动执行 src/main/resources/sql/chat_message.sql", e);
		}
	}
}