# Adventure

<h3>@author: Roger</h3>

基于 Spring Boot 的 **WebSocket 多人聊天室**项目。

## 项目简介

- 核心功能：WebSocket 房间制多人聊天（指定 6 位房间码加入或随机匹配）、图片/GIF/视频消息、访问记录、系统内置隐藏房间（密码进入、聊天数据落库）
- 仅保留聊天室功能 + MySQL + MyBatis-Plus，已移除 Swagger2、Netty、七牛云、Velocity 代码生成、Nacos、Redis、Docker 等无关组件与配置
- 启动后访问 `http://localhost:8001/go` 即可进入聊天室

## 技术栈

- Spring Boot 2.0.5、Java 1.8
- MyBatis-Plus 3.3.2（BaseMapper + Wrapper，无 XML）
- MySQL
- Spring Security + JWT（入口拦截保留，非聊天室入口匿名返回 403）
- Thymeleaf、WebSocket
- 离线 ip2region（IP 归属地解析，省/市精度）
- Logback 日志（仅 INFO/ERROR）

## 主要功能

- 房间制多人聊天（单房间最多 10 人）
- 6 位短房间码（去除易混淆字符，大小写不敏感）
- 媒体消息（图片/GIF/视频）：64KB 分片二进制帧 + 序号传输，服务端仅透传不组装，前端 ChaCha20 逐片独立密钥加密、前端组装，支持丢包重传
- 视频：≤10MB 原样发送、11\~20MB 压缩为 640×360\@400\~600kbps、>20MB 拦截提示；GIF ≤3MB 原样展示动画、>3MB 拦截提示
- 敏感词脱敏（前后端双重拦截）
- 房间列表浮窗（实时房间名 + 人数，可最小化）
- 实时在线人数：右上角悬浮展示，进入房间后自动隐藏
- 加入房间后「加入房间」按钮切换为「离开」并隐藏「随机匹配」，离开需二次确认
- WebSocket 访问记录（IP、省/市归属地、进入时间、房间名）
- 消息气泡自适应内容宽度，本人消息在右、他人消息在左
- 消息 500 字符上限、聊天记录上限 300 条（自动释放媒体对象 URL）、一键清屏、自动滚动到底部、移动端适配
- 系统内置隐藏房间（key=`CSYY`）：在房间名输入框输入 `csyy` 即弹框输入密码 `csyy`，不出现在公开房间列表；该房间聊天数据落库（文字、图片真实内容、视频文件名标记），保留 10 天，每天凌晨定时清理
- 隐藏房间历史记录：进入后默认加载最近 20 条，向上滑动加载更早记录，支持一键回到最下面

## 快速启动

```bash
mvn clean package        # 编译打包
mvn spring-boot:run      # 默认 dev profile 启动（端口 8001）
```

启动后访问 `http://localhost:8001/go`。
