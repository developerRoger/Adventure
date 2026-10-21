-- --------------------------------------------
-- websocket访问记录表
-- 记录哪个IP进入websocket、进入时间、加入的房间名
-- --------------------------------------------
DROP TABLE IF EXISTS `websocket_visit_log`;
CREATE TABLE `websocket_visit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sid` varchar(64) NOT NULL COMMENT 'websocket连接标识',
  `ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `room_name` varchar(32) DEFAULT NULL COMMENT '加入的房间名',
  `enter_time` datetime DEFAULT NULL COMMENT '进入websocket时间',
  PRIMARY KEY (`id`),
  KEY `idx_sid` (`sid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='websocket访问记录表';
