-- IP 黑名单表：动态添加需要限制加入房间的IP，WebSocket加入/随机匹配前实时校验
CREATE TABLE IF NOT EXISTS ip_blacklist (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  ip VARCHAR(64) NOT NULL COMMENT '被限制的IP',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP黑名单';

-- 示例：运行中动态添加/移除IP（插入即生效，无需重启）
-- INSERT INTO ip_blacklist (ip, remark) VALUES ('192.168.1.100', '测试拦截');
-- DELETE FROM ip_blacklist WHERE ip = '192.168.1.100';