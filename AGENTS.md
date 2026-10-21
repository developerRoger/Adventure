# AGENTS.md

本文件为 AI 编码代理在本仓库工作时提供必要的上下文与约定。请先阅读本文件，再进行代码修改。

## 项目概览

- 项目名：Adventure（`com.firstTry:Adventure:1.0`），Maven 项目，打包方式 `jar`
- Java 版本：1.8
- Spring Boot：`2.0.5.RELEASE`
- 主要技术：MyBatis、MySQL、Redis、Spring Security + JWT、Swagger2、Thymeleaf、Netty、WebSocket、七牛云、Velocity 代码生成

## 常用命令

```bash
# 编译并打包
mvn clean package

# 运行测试
mvn test

# 启动（默认 dev profile）
mvn spring-boot:run

# 指定 profile 启动
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 目录结构

- 源码根包：`src/main/java/com/firstTry/Adventure/`
  - `controller/` 控制层，`service/`（含 `impl/`）业务层，`mapper/`（含 XML）数据访问层
  - `entity/` 实体，`vo/` 视图对象，`config/` 配置类，`utils/` 工具类
  - `exception/` 异常处理，`middleware/` WebSocket 中间件，`tasks/` 定时任务，`Netty/` Netty 通信
- 示例代码：`src/main/java/com/example/`（观察者模式演示）
- 资源目录：`src/main/resources/`（配置、静态前端资源）

## 开发约定

- 代码遵循标准分层：Controller → Service（接口 + `impl` 实现）→ Mapper（接口 + XML）。
- 统一响应使用 `utils/R.java`（`R.ok()` / `R.error(...)`），分页使用 `PageUtils` / `Query`。
- 实体类使用 Lombok，Java 版本为 1.8。
- 新增 Mapper XML 放在与接口同目录（`mapper/`）下，注意与 `@MapperScan` 包名保持一致。
- 能用最新的lambad语法就使用lambad，但是要写复杂语法注释

## 注意事项

- `target/` 已被 `.gitignore` 忽略，不要提交编译产物。
- 多环境配置：`application.yml` 通过 `spring.profiles.active` 切换 `dev` / `prod`。
- `AdventureApplication` 中 `@MapperScan("com.firstTry.adventure.mapper")` 与实际包名 `com.firstTry.Adventure.mapper` 存在大小写差异，修改 Mapper 扫描或包名时需保持一致。
- 提交前请运行 `mvn clean package` 确认编译通过。

