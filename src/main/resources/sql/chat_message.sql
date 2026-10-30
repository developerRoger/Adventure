-- 隐藏房间聊天记录表（应用启动时会自动创建，如启动失败可手动执行本脚本）
CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  room_key VARCHAR(16) NOT NULL COMMENT '房间key',
  sender VARCHAR(64) DEFAULT NULL COMMENT '发送者连接标识sid',
  msg_type VARCHAR(16) NOT NULL COMMENT '消息类型：text文本/image图片/video视频',
  content TEXT COMMENT '文本内容或媒体标记文本',
  media_name VARCHAR(255) DEFAULT NULL COMMENT '媒体文件名',
  media_mime VARCHAR(64) DEFAULT NULL COMMENT '媒体MIME类型',
  media_data LONGTEXT COMMENT '图片base64内容（仅图片类型）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_room_created (room_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐藏房间聊天记录';