# AGENTS.md

本文件为 AI 编码代理在本仓库工作时提供必要的上下文与约定。请先阅读本文件，再进行代码修改。

## 项目概览

- 项目名：Adventure（`com.firstTry:Adventure:1.0`），Maven 项目，打包方式 `jar`
- Java 版本：1.8
- Spring Boot：`2.0.5.RELEASE`
- 主要技术：Spring Boot、MyBatis-Plus（`BaseMapper` + Wrapper，无 XML）、MySQL、Spring Security + JWT、Thymeleaf、WebSocket、离线 ip2region（IP 归属地解析）、媒体加密（纯 JS ChaCha20，图片/GIF/视频共用）、隐藏房间聊天记录落库（保留 10 天 + 定时清理）
- 项目定位：**仅保留聊天室功能**（WebSocket 房间制多人聊天 + 图片/GIF/视频消息 + 访问记录 + 系统内置隐藏房间），已移除 Swagger2、Netty、七牛云、Velocity 代码生成、Nacos、Redis（`RedisConfig`/`RedisObjectSerializer`/yml redis 配置/`spring-boot-starter-redis` 依赖）、Docker（`Dockerfile`/docker-maven-plugin）、后台管理及测试用例

## 常用命令

```bash
# 编译并打包
mvn clean package

# 启动（默认 dev profile，端口 8001）
mvn spring-boot:run

# 指定 profile 启动
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 注意：项目已删除测试用例，mvn test 无测试可运行
```

## 目录结构

- 源码根包：`src/main/java/com/firstTry/Adventure/`
  - `config/` 配置类（Spring Security、JWT、默认视图、隐藏房间定时清理 + 自动建表），`middleware/` WebSocket 中间件
  - `entity/` 实体，`mapper/` 数据访问层（MyBatis-Plus，无 XML 文件），`service/`（含 `impl/`）业务层
  - `exception/` 异常处理，`utils/` 工具类
- 资源目录：`src/main/resources/`（application*.yml、logback-spring.xml、ip2region.xdb、静态资源、templates/html/colourEgg.html 聊天室页面、sql/chat_message.sql 隐藏房间聊天记录建表脚本）

## 开发约定

- 代码遵循标准分层：Controller/视图 → Service（接口 + `impl` 实现）→ Mapper（`BaseMapper` + LambdaWrapper）。
- 新增简单增删改查优先使用 MyBatis-Plus `BaseMapper` + `Wrappers.lambdaQuery()` / `lambdaUpdate()` 链式写法，无需新建 XML。
- 实体类使用 Lombok，需加 `@TableName` 注解指定表名，Java 版本为 1.8。
- 统一响应使用 `utils/R.java`（`R.ok()` / `R.error(...)`）。
- 能用最新的 lambda 语法就使用 lambda，但是要写复杂语法注释。

## 注意事项

- 日志框架必须使用 logback，禁止使用 LOG4J；日志输出仅保留 error 和 info 级别。
- 日志输出目录生产环境固定为 `/home/apps/logs/`（通过 `log.path` 配置，本地 dev 配置为 `./logs`），单文件最大 5MB，保留 5 天。
- 项目不使用 Nacos，所有 nacos 相关配置均已移除。
- 项目不使用 Redis 与 Docker：`RedisConfig`/`RedisObjectSerializer`、yml 中的 redis 配置、`spring-boot-starter-redis` 依赖以及 `Dockerfile`/docker-maven-plugin 均已移除，勿再引入。
- WebSocket 媒体消息(图片/GIF/视频)分片转发**禁止在服务端拼接组装**，分片组装完全交给前端处理，以降低 JVM 内存压力；丢包通过「片序号 + 重传」由前端自行处理，服务端仅按 msgId 路由重传请求（`media_end`/`retrans`）。
- 隐藏房间（key=`CSYY`，密码 `csyy`）是"服务端不组装分片"规则的**唯一例外**：该房间图片落库需由服务端重组分片并用 `ChaCha20` 解密还原，普通房间媒体分片仍保持不组装。
- 隐藏房间聊天数据落库：文字存脱敏后内容、图片存 base64 真实内容、视频仅存文件名标记（`[视频] xxx`）；保留 10 天，`config/ChatCleanTask` 每天 03:00 定时清理（`@Scheduled`），`config/ChatTableInitializer` 启动时自动建表 `chat_message`（脚本见 `resources/sql/chat_message.sql`）。
- `AdventureApplication` 中 `@MapperScan("com.firstTry.adventure.mapper")` 与实际包名 `com.firstTry.Adventure.mapper` 存在大小写差异，修改 Mapper 扫描或包名时需保持一致。
- 入口拦截保留 Spring Security 白名单配置（`config/WebSecurityConfig.java`），非聊天室入口匿名访问返回 403。
- 聊天历史查询接口 `GET /chatHistory`（参数 `room`、可选 `beforeId` 游标、`size` 默认 20）：DB 按 id 倒序取 top N 后 `Collections.reverse` 反转成正序返回（旧在前）；`GET /online` 返回当前在线连接数（`WebSocketServer.getOnlineCount()`）。二者均在 `PASS_URL` 白名单内放行。
- WebSocket 消息类型：`join`/`match`/`chat`/`image_meta`/`media_end`/`retrans`/`heartbeat` 之外，新增 `leave`（客户端主动离开，服务端执行 `leaveRoom` 后回 `left`）。
- 前端按钮状态联动：不在房间时显示「加入房间」+「随机匹配」并在右上角悬浮展示在线人数；加入房间后「加入房间」变「离开」、「随机匹配」与在线人数均隐藏；离开房间先弹确认框再发送 `leave`。隐藏房间无独立进入按钮，通过在房间名输入框输入 `CSYY` 触发密码弹框进入，进入后默认加载最近 20 条历史，向上滑动经 `/chatHistory?beforeId=` 翻页。
- `target/` 已被 `.gitignore` 忽略，不要提交编译产物。
- 提交前请运行 `mvn clean package` 确认编译通过。
