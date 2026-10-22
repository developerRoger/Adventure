# Adventure

<h3>@author: Roger</h3>

基于 Spring Boot 的 **WebSocket 多人聊天室**项目。

## 项目简介

- 核心功能：WebSocket 房间制多人聊天（指定 6 位房间码加入或随机匹配）、图片消息、访问记录
- 仅保留聊天室功能 + Redis + MySQL + MyBatis-Plus，已移除 Swagger2、Netty、七牛云、Velocity 代码生成、Nacos 等无关组件
- 启动后访问 `http://localhost:8001/go` 即可进入聊天室

## 技术栈

- Spring Boot 2.0.5、Java 1.8
- MyBatis-Plus 3.3.2（BaseMapper + Wrapper，无 XML）
- MySQL、Redis
- Spring Security + JWT（入口拦截保留，非聊天室入口匿名返回 403）
- Thymeleaf、WebSocket
- Logback 日志（仅 INFO/ERROR）

## 主要功能

- 房间制多人聊天（单房间最多 50 人）
- 6 位短房间码（去除易混淆字符，大小写不敏感）
- 图片消息：canvas 压缩 ≤1MB、64KB 分片二进制帧传输、前端组装
- 敏感词脱敏（前后端双重拦截）
- 房间列表浮窗（实时房间名 + 人数，可最小化）
- WebSocket 访问记录（IP、进入时间、房间名）
- 消息 500 字符上限、一键清屏、自动滚动到底部、移动端适配

## 快速启动

```bash
mvn clean package        # 编译打包
mvn spring-boot:run      # 默认 dev profile 启动（端口 8001）
```

启动后访问 `http://localhost:8001/go`。
