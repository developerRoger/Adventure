# AGENTS.md

本文件为 AI 编码代理在本仓库工作时提供必要的上下文与约定。请先阅读本文件，再进行代码修改。

## 项目概览

- 项目名：Adventure（`com.firstTry:Adventure:1.0`），Maven 项目，打包方式 `jar`
- Java 版本：1.8
- Spring Boot：`2.0.5.RELEASE`
- 主要技术：Spring Boot、MyBatis-Plus（`BaseMapper` + Wrapper，无 XML）、MySQL、Redis、Spring Security + JWT、Thymeleaf、WebSocket
- 项目定位：**仅保留聊天室功能**（WebSocket 房间制多人聊天 + 图片消息 + 访问记录），已移除 Swagger2、Netty、七牛云、Velocity 代码生成、Nacos、后台管理及测试用例

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
  - `config/` 配置类（Spring Security、JWT、Redis、默认视图），`middleware/` WebSocket 中间件
  - `entity/` 实体，`mapper/` 数据访问层（MyBatis-Plus，无 XML 文件），`service/`（含 `impl/`）业务层
  - `exception/` 异常处理，`utils/` 工具类
- 资源目录：`src/main/resources/`（application*.yml、logback-spring.xml、静态资源、templates/html/colourEgg.html 聊天室页面）

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
- WebSocket 图片消息分片转发**禁止在服务端拼接组装**，分片组装完全交给前端处理，以降低 JVM 内存压力。
- `AdventureApplication` 中 `@MapperScan("com.firstTry.adventure.mapper")` 与实际包名 `com.firstTry.Adventure.mapper` 存在大小写差异，修改 Mapper 扫描或包名时需保持一致。
- 入口拦截保留 Spring Security 白名单配置（`config/WebSecurityConfig.java`），非聊天室入口匿名访问返回 403。
- `target/` 已被 `.gitignore` 忽略，不要提交编译产物。
- 提交前请运行 `mvn clean package` 确认编译通过。
