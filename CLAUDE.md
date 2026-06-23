# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

云课堂 (YunH) — 基于 Spring Cloud 的在线教育微服务项目。采用 Spring Boot 2.2.5 + Spring Cloud Hoxton.SR3 + Spring Cloud Alibaba 2.2.1，JDK 1.8。

## 常用命令

```bash
# 构建全部模块
mvn clean package

# 构建跳过测试
mvn clean package -DskipTests

# 构建单个模块及其依赖
mvn clean package -pl yunh-service/yunh-service-user -am

# 启动单个服务
mvn spring-boot:run -pl yunh-service/yunh-service-user

# 初始化数据库（首次运行）
mysql -u root -p < database/init.sql
# 测试数据
mysql -u root -p < database/insert_more_data.sql
# 视频测试数据（公开 MP4 链接 + 课程4/5）
mysql -u root -p < database/insert_test_video_data.sql

# 一键启动所有基础设施（Nacos + Redis + RocketMQ + Seata + Sentinel Dashboard）
config\start-all-services.bat

# 批量发布 Nacos 配置（PowerShell）
powershell -File config\publish-nacos-configs.ps1

# Sentinel Dashboard（端口 8098）
java --add-opens java.base/java.lang=ALL-UNNAMED -jar config/sentinel-dashboard-1.7.1.jar

# 手动启动基础设施
# Nacos（必须先启动，内置版本）
cd D:/vis/yunh/config/nacos-server-3.1.1/nacos/bin && startup.cmd -m standalone
# Seata Server
cd D:/vis/yunh/config/seata-server-1.4.2/seata/seata-server-1.4.2/bin && seata-server.bat
# RocketMQ（两个终端）
cd D:/vis/yunh/config/rocketmq-all-4.9.4-bin-release && mqnamesrv.cmd
cd D:/vis/yunh/config/rocketmq-all-4.9.4-bin-release && mqbroker.cmd -n 127.0.0.1:9876

# 前端（yunh-admin，端口 5173，代理 /api → localhost:9000）
cd yunh-admin && npm run dev
cd yunh-admin && npm run build
```

## 架构

### 模块依赖关系（自底向上）

```
yunh-basic-common          ← 基础工具（Result、异常处理、RedisService、MqConstant）
  ↑
yunh-basic-dependency      ← 聚合依赖（Web + MyBatis-Plus + MySQL + Druid + Nacos Config + Knife4j）
  ↑
yunh-service-common        ← 纯依赖聚合（无 Java 源码），添加 Nacos Discovery + Sentinel
  ↑
yunh-pojo-* (8个)          ← 实体类模块，其中 pojo-common 存放跨服务共享 DTO（CourseOrderMessage、SelectCourseDTO）
yunh-api-* (4个)           ← Feign Client 接口 + Fallback 降级类
yunh-service-* (8个)       ← 微服务实现，依赖 service-common + 对应 pojo + api
yunh-auth                  ← 独立鉴权服务（JWT 签发/刷新/验证码）
yunh-support/gateway       ← API 网关（路由、鉴权过滤、限流、CORS）
```

每个微服务内部结构一致：`controller/` → `service/` + `service/impl/` → `mapper/`，`config/` 存放 FeignConfig、SwaggerConfig、Properties 等配置类。

### 服务端口映射

| 服务 | 端口 | 数据库 |
|------|------|--------|
| Gateway | 9000 | - |
| Auth | 9001 | Redis（验证码/Token黑名单） |
| Course | 9002 | yunh_course |
| Order | 9004 | yunh_order |
| Pay | 9005 | yunh_pay |
| Video | 9006 | yunh_video |
| Interaction | 9007 | yunh_interaction |
| Search | 9008 | yunh_course（ES） |
| Statistics | 9009 | yunh_statistics |
| User | 9010 | yunh_user |

### Gateway 路由表

Gateway 将外部 `/api/**` 请求路由到内部服务，全部使用 `StripPrefix=1` 去掉 `/api` 前缀：

| 外部路径 | 目标服务 | 备注 |
|----------|----------|------|
| `/api/auth/**` | yunh-auth | 白名单，免鉴权 |
| `/api/user/**` | yunh-service-user | Retry on BAD_GATEWAY x3；`/api/user/internal/**` 禁止外部访问 |
| `/api/course/**` | yunh-service-course | 仅 GET 免鉴权：`/list`、`/health`、`/{id}`、`/{id}/detail` |
| `/api/order/**`, `/api/cart/**` | yunh-service-order | |
| `/api/pay/**` | yunh-service-pay | |
| `/api/video/**`, `/api/chapter/**` | yunh-service-video | |
| `/api/comment/**`, `/api/question/**`, `/api/answer/**`, `/api/note/**` | yunh-service-interaction | |
| `/api/search/**` | yunh-service-search | 白名单，免鉴权 |
| `/api/statistics/**` | yunh-service-statistics | 白名单，免鉴权 |

### 服务间调用关系

- Auth → User（Feign：登录验证）
- User → Course（Feign：选课减库存）
- Course → User（Feign：获取教师信息）
- Order → User / Course（Feign：跨服务调用）

### 服务基础设施依赖矩阵

| 服务 | Seata | RocketMQ | 备注 |
|------|-------|----------|------|
| User | ✓ | ✓ | 选课/取消订单的分布式事务入口 |
| Course | ✓ | | 库存扣减/恢复参与分布式事务 |
| Order | ✓ | ✓ | 订单状态变更 |
| Pay | ✓ | ✓ | 支付流程 |
| Video | | | |
| Interaction | | | |
| Search | | | 共用 yunh_course 数据库 |
| Statistics | | | |
| Auth | | | Redis 存验证码/Token黑名单 |

### API 文档

所有 8 个业务服务均配置了 **Knife4j**（Swagger 增强），直接访问各服务的 `http://localhost:{port}/doc.html` 即可查看接口文档。

### 配置架构

**双文件模式**：每个服务有 `bootstrap.yml`（Nacos 连接）和 `application.yml`（仅端口和日志路径）。所有业务配置（数据库密码、Feign 超时、Seata 等）存储在 Nacos Config，Data ID 为 `{spring.application.name}.yml`。

Nacos 本地配置备份：`config/nacos-server-3.1.1/nacos/data/tenant-config-data/public/DEFAULT_GROUP/`

**服务启动顺序**：Nacos → MySQL → Redis → Seata（如需） → RocketMQ（如需） → 各微服务

**Nacos 配置管理**：Data ID 规则为 `{spring.application.name}.yml`（如 `yunh-service-user.yml`）。可通过 Nacos 控制台（http://127.0.0.1:8848/nacos，账号 nacos/123456）或 `config/publish-nacos-configs.ps1` 脚本批量发布。

**基础设施工具版本注意**：`config/` 下的启动脚本（start-seata.bat、start-namesrv.bat、start-broker.bat）硬编码了 `JAVA_HOME=C:\Program Files\Java\jdk-20`，与项目要求的 JDK 1.8 不同。这些是中间件工具使用的 JDK，不影响应用代码编译。

## 核心模式

### 统一响应
`Result<T>` 包装所有接口返回，使用 `ResultCode` 枚举定义状态码。`GlobalExceptionHandler` 统一捕获 `BusinessException` 和通用异常。均在 `yunh-basic-common` 中（包 `com.yunh.common`）。

### Feign 调用
Feign Client 定义在 `yunh-api` 模块，含 Fallback 降级。`FeignConfig` 拦截器传递 `Authorization`、`X-Request-ID`、Seata XID。Feign 超时由 Nacos 配置管理（connect 5s，read 10-15s）。

### JWT 鉴权流程
1. Auth 服务签发 access token（2h）+ refresh token（7d），存 Redis
2. Gateway `AuthGlobalFilter`（order=-100）校验 JWT，将 userId/username/role 写入请求头（`X-User-Id`、`X-Username`、`X-Role`）
3. 白名单路径跳过校验：`/api/auth/**`、`/api/user/login`、`/api/user/register`、`/api/user/health`、`/api/search/**`、`/api/statistics/**`、`/actuator/**`、`/favicon.ico`
4. 课程 API 部分免鉴权：仅 GET 请求的 `/api/course/list`、`/api/course/health`、`/api/course/{id}`、`/api/course/{id}/detail` 放行，POST/PUT/DELETE 需鉴权
5. 内部接口保护：`/api/user/internal/**` 禁止外部访问（返回 403）

### 分布式事务
选课流程使用 `@GlobalTransactional`（Seata AT 模式），事务组 `yunh_tx_group`。Gateway 有 `SeataGatewayFilter`（order=-250）透传 XID，Feign 拦截器在服务间传播 XID。Seata 服务端使用 DB 存储模式（`seata` 数据库），通过 Nacos 注册。

涉及分布式事务的方法：
- `UserCourseService.selectCourse()` — 本地插入 + 远程减库存 + MQ 消息
- `UserCourseService.cancelTimeoutOrder()` — 本地更新状态 + 远程加库存

### 消息队列
RocketMQ Topic/Tag 常量集中在 `MqConstant`。`MqMessageService` 封装了同步/异步/单向/延迟/事务五种消息发送模式。选课超时取消通过延迟消息实现（30分钟）。RocketMQ 事务消息监听器在 `CourseOrderTransactionListener`。

### 库存并发控制
Course 服务的库存操作使用**乐观锁**：Mapper XML 中 `UPDATE t_course SET stock = stock - #{count} WHERE id = #{id} AND stock >= #{count}`，防止超卖。

### Gateway 过滤器链

| 过滤器 | Order | 职责 |
|--------|-------|------|
| SeataGatewayFilter | -250 | 透传分布式事务 XID |
| LogGlobalFilter | -200 | 请求日志（requestId、耗时、客户端 IP） |
| AuthGlobalFilter | -100 | JWT 校验、白名单放行、注入用户信息头 |

### Gateway 限流
`SentinelGatewayConfig` 配置了 Sentinel 网关限流：用户和课程 API 限 2 req/sec，自定义 429 响应。

### 前端
- **yunh-admin**：Vue 3 + Element Plus + Pinia + ECharts，Vite 构建，Axios 拦截器自动刷新 Token（401 → refresh → 重试）。路由守卫检查 localStorage token。端口 5173，代理 `/api → localhost:9000`
- **yunh-mini**：微信小程序（appid: `wx02da9208e46ca6a0`），`wx.request` 封装含 JWT 和自动刷新。自定义 tabBar 支持学生/教师双角色切换（8个 tab）。分包结构：`packageCourse`（课程详情/章节/播放）、`packageOrder`（下单/结果）、`packageProfile`（我的课程/订单）、`packageTeacher`（教师端）。多环境配置在 `miniprogram/env/`（dev → `127.0.0.1:9000`）

## 约定

- 数据库表名 `t_` 前缀，字段下划线命名，MyBatis-Plus 自动驼峰映射
- Redis Key 前缀：`yunh:{domain}:{id}`
- 日志路径：应用日志 `D:/vis/yunh/log/app/{service-name}.log`，Sentinel 日志 `log/sentinel/`
- 每个服务独立数据库（8 个库），Init 脚本：`database/init.sql`
- 根 pom.xml 管理 6 个顶层模块：`yunh-basic`、`yunh-pojo`、`yunh-api`、`yunh-service`、`yunh-support`、`yunh-auth`
- 注意：`yunh-support` 下有一个空的 pay 服务脚手架，实际 Pay 服务在 `yunh-service/yunh-service-pay/`
- 测试用户密码统一为 `123456`（BCrypt 加密）
- **项目无自动化测试**：所有 `src/test/` 目录为空，无 JUnit/TestNG 测试
- Sentinel Dashboard 地址：`http://localhost:8098`
- `config/` 目录包含本地基础设施二进制：Nacos（`nacos-server-3.1.1/`）、Redis（`Redis-x64-5.0.14.1/`）、RocketMQ、Seata、Sentinel Dashboard JAR。`start-all-services.bat` 一键启动全部基础设施（推荐）
- `database/` 包含 3 个 SQL 文件：`init.sql`（建表）、`insert_more_data.sql`（测试数据）、`insert_test_video_data.sql`（视频和课程4/5数据）
- `ResultCode` 枚举：200(成功)、400(参数错误)、401(未授权)、403(禁止)、404(未找到)、500(系统错误)、600(业务错误)
