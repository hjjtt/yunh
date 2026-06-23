# 🧠 云课堂 (YunH) 项目记忆

## 📚 项目信息

- **项目名称：** 云课堂 (YunH)
- **项目路径：** `D:\vis\yunh`
- **项目类型：** Spring Cloud 微服务学习项目
- **创建时间：** 2026-04-03
- **学习目标：** 系统学习 Spring Cloud 微服务架构

---

## 🛠️ 基础设施

### Nacos Server
| 配置项 | 值 |
|--------|-----|
| 安装路径 | `D:\env\nacos\nacos\` |
| 控制台地址 | `http://127.0.0.1:8848/nacos` |
| 用户名/密码 | `nacos` / `123456` |
| 启动命令 | `startup.cmd -m standalone` |

### Nacos 配置中心
- Data ID 格式：`${spring.application.name}.yml`
- 本地备份：`config/nacos-server-3.1.1/nacos/data/tenant-config-data/public/DEFAULT_GROUP/`
- Seata 配置组：`SEATA_GROUP`
- **安全策略**：本地 `application.yml` 不含数据库密码，完全依赖 Nacos Config

### MySQL
| 配置项 | 值 |
|--------|-----|
| 地址 | `localhost:3306` |
| 用户名/密码 | `root` / `123456` |

**8 个独立数据库**：yunh_user, yunh_course, yunh_order, yunh_pay, yunh_video, yunh_interaction, yunh_statistics

### Redis
- Auth 服务使用（验证码存储、Refresh Token、Token 黑名单）

### Seata Server
- 地址：`127.0.0.1:8091`
- 事务组：`yunh_tx_group`
- 模式：AT
- 注册/配置通过 Nacos（`SEATA_GROUP`）

### RocketMQ
- Name Server：`127.0.0.1:9876`
- 二进制包：`config/rocketmq-all-4.9.4-bin-release/`

### Sentinel
- Dashboard：`127.0.0.1:8098`
- 各服务端口范围：8719-8728

---

## 📁 项目架构

### 模块依赖（自底向上）
```
yunh-basic-common → yunh-basic-dependency → yunh-service-common
                                                  ↑
yunh-pojo-* (7个)  ←──  yunh-api-* (4个)  ←──  yunh-service-* (8个)
```

### 服务端口
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

### 服务间调用
- Auth → User（Feign：登录验证）
- User → Course（Feign：选课减库存）
- Course → User（Feign：获取教师信息）
- Order → User / Course（Feign：跨服务调用）

### 前端
- **yunh-admin**：Vue 3 + Element Plus + Pinia + Vite
- **yunh-mini**：微信小程序（含自定义 TabBar）

---

## 📖 学习进度

| 阶段 | 状态 | 说明 | 完成时间 |
|------|------|------|----------|
| 阶段一：基础搭建 | ✅ 完成 | Maven 多模块、统一响应、异常处理 | 2026-04-03 |
| 阶段二：服务注册与发现 | ✅ 完成 | Nacos Discovery、Feign 调用 | 2026-04-03 |
| 阶段三：配置中心 | ✅ 完成 | Nacos Config、动态刷新、敏感数据管理 | 2026-04-04 |
| 阶段四：服务网关 | ✅ 完成 | Gateway、JWT 鉴权、CORS、日志过滤 | 2026-04-04 |
| 阶段五：服务调用 | ✅ 完成 | OpenFeign 高级配置、Fallback、拦截器 | 2026-04-06 |
| 阶段六：分布式事务 | ✅ 完成 | Seata AT 模式、选课全局事务 | 2026-04-07 |
| 阶段七：消息队列 | ✅ 完成 | RocketMQ（同步/异步/延迟/事务消息）、订单超时取消 | 2026-04-08 |
| 阶段八：高级特性 | ✅ 完成 | Sentinel 限流（网关+服务）、熔断降级 | 2026-04-09 |
| 阶段九：生产项目 | ✅ 完成 | 前后端联调、Admin/小程序、完整业务闭环 | 2026-04-12 |

---

## 📝 重要决策

### 敏感数据管理（2026-04-04）
- 数据库密码等敏感数据统一从 Nacos Config 加载
- 无本地兜底配置，服务启动完全依赖 Nacos

### 鉴权架构（2026-04-04）
- Auth 服务签发 JWT（access 2h + refresh 7d）
- Gateway `AuthGlobalFilter` 校验 + 注入用户头
- Refresh Token 存 Redis，登出加黑名单

### 分布式事务（2026-04-07）
- 选课流程使用 `@GlobalTransactional`（Seata AT）
- FeignConfig 拦截器透传 Seata XID
- Gateway `SeataGatewayFilter` 透传 XID

### 消息队列（2026-04-08）
- Topic/Tag 常量集中在 `MqConstant`
- 选课超时取消：延迟消息 30 分钟
- 支持事务消息保证一致性

---

**最后更新：** 2026-04-13
