# 云课堂 (YunH) - Spring Cloud 微服务项目

## 项目简介

基于 Spring Cloud 的在线教育微服务项目。采用 Spring Boot 2.2.5 + Spring Cloud Hoxton.SR3 + Spring Cloud Alibaba 2.2.1，JDK 1.8。

## 快速启动

### 1. 启动基础设施

```bash
# 一键启动所有基础设施（Nacos + Redis + RocketMQ + Seata + Sentinel Dashboard）
config\start-all-services.bat

# 批量发布 Nacos 配置（PowerShell）
powershell -File config\publish-nacos-configs.ps1
```

### 2. 初始化数据库

```bash
mysql -u root -p < database/init.sql
mysql -u root -p < database/insert_more_data.sql
mysql -u root -p < database/insert_test_video_data.sql
```

### 3. 启动服务

**启动顺序**：Gateway → 各业务服务（启动前确保 Nacos、MySQL、Redis 已运行）

各服务通过 IDE 运行对应的 `*Application.java`，或使用 Maven：

```bash
mvn spring-boot:run -pl yunh-support/yunh-service-gateway
mvn spring-boot:run -pl yunh-service/yunh-service-user
# ... 其他服务
```

### 4. 启动前端

```bash
# 管理端（端口 5173，代理 /api → localhost:9000）
cd yunh-admin && npm run dev
```

## 架构概览

### 服务端口映射

| 服务 | 端口 | 数据库 |
|------|------|--------|
| Gateway | 9000 | - |
| Auth | 9001 | Redis |
| Course | 9002 | yunh_course |
| Order | 9004 | yunh_order |
| Pay | 9005 | yunh_pay |
| Video | 9006 | yunh_video |
| Interaction | 9007 | yunh_interaction |
| Search | 9008 | yunh_course（ES） |
| Statistics | 9009 | yunh_statistics |
| User | 9010 | yunh_user |

### 模块依赖

```
yunh-basic-common（Result、异常处理、RedisService、MqConstant）
  ↑
yunh-basic-dependency（Web + MyBatis-Plus + MySQL + Druid + Nacos Config + Knife4j）
  ↑
yunh-service-common（Nacos Discovery + Sentinel）
  ↑
yunh-pojo-*（实体类）  yunh-api-*（Feign Client + Fallback）
  ↑                       ↑
yunh-service-*（微服务实现）
yunh-auth（独立鉴权服务）
yunh-support/gateway（API 网关）
```

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.2.5.RELEASE | 基础框架 |
| Spring Cloud | Hoxton.SR3 | 微服务框架 |
| Spring Cloud Alibaba | 2.2.1 | Nacos/Sentinel/Seata |
| MyBatis-Plus | 3.3.2 | ORM |
| MySQL | 8.0+ | 数据库 |
| Nacos | 3.1.1 | 注册中心 + 配置中心 |
| Seata | 1.4.2 | 分布式事务（AT 模式） |
| RocketMQ | 4.9.4 | 消息队列 |
| Sentinel | 1.7.1 | 限流降级 |
| Redis | 5.0.14 | 缓存/Token 黑名单/验证码 |
| JWT | - | 鉴权 |
| Knife4j | - | API 文档 |

### 前端

- **yunh-admin**：Vue 3 + Element Plus + Vite，端口 5173
- **yunh-mini**：微信小程序

## 关键配置

- **Nacos 控制台**：http://127.0.0.1:8848/nacos（nacos/123456）
- **Sentinel Dashboard**：http://localhost:8098
- **API 文档**：各服务 `http://localhost:{port}/doc.html`
- **配置管理**：Data ID 规则为 `{spring.application.name}.yml`
- **测试用户密码**：统一为 `123456`（BCrypt 加密）

## 项目结构

```
yunh/
├── yunh-basic/           # 基础模块
│   ├── yunh-basic-common/    # 工具类、公共依赖
│   └── yunh-basic-dependency/  # 依赖聚合
├── yunh-pojo/            # 实体类模块（8个）
├── yunh-api/             # Feign Client 接口（4个）
├── yunh-service/         # 微服务实现（8个）
├── yunh-support/         # 基础设施（Gateway）
├── yunh-auth/            # 认证服务
├── yunh-admin/           # 管理端前端
├── yunh-mini/            # 微信小程序
├── config/               # 基础设施二进制 + 启动脚本
├── database/             # SQL 脚本
└── docs/                 # 文档
```
