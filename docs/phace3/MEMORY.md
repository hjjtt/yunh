# 🧠 云课堂 (YunH) 项目记忆

## 📚 项目信息

- **项目名称：** 云课堂 (YunH)
- **项目路径：** `D:\vis\yunh`
- **项目类型：** Spring Cloud 微服务学习项目
- **创建时间：** 2026-04-03
- **学习目标：** 系统学习 Spring Cloud 微服务架构

---

## 🛠️ 工具设置

### Nacos Server

| 配置项 | 值 |
|--------|-----|
| 安装路径 | `D:\env\nacos\nacos\` |
| 控制台地址 | `http://10.150.9.193:8080/index.html` |
| 用户名 | `nacos` |
| 密码 | `123456` |
| 启动命令 | `startup.cmd -m standalone` |

### Nacos 配置中心 - 敏感数据管理

| Data ID | Group | 格式 | 包含敏感数据 |
|---------|-------|------|-------------|
| `yunh-service-user.yml` | DEFAULT_GROUP | YAML | ✅ 数据库密码 |
| `yunh-service-course.yml` | DEFAULT_GROUP | YAML | ✅ 数据库密码 |

**安全策略**: 本地 `application.yml` 不包含数据库密码，敏感数据统一从 Nacos 加载

### MySQL

| 配置项 | 值 |
|--------|-----|
| 地址 | `localhost:3306` |
| 用户名 | `root` |
| 密码 | `123456` |
| 用户数据库 | `yunh_user` |
| 课程数据库 | `yunh_course` |

---

## 📁 项目结构

```
yunh-cloud/
├── yunh-basic/             # 基础模块
├── yunh-pojo/              # 实体类模块
├── yunh-api/               # API 接口模块
├── yunh-service/           # 服务层
│   ├── yunh-service-user/  # 用户服务 (9001)
│   └── yunh-service-course/# 课程服务 (9002)
└── yunh-support/           # 基础设施
    └── yunh-service-gateway/ # 网关
```

---

## 📖 学习进度

| 阶段 | 状态 | 说明 |
|------|------|------|
| 阶段一：基础搭建 | ✅ 完成 | Maven 多模块、统一响应、异常处理 |
| 阶段二：服务注册与发现 | ✅ 完成 | Nacos Discovery、Feign 调用 |
| 阶段三：配置中心 | ✅ 完成 | Nacos Config、动态刷新 |
| 阶段四：服务网关 | ⏭️ 待开始 | Spring Cloud Gateway |
| 阶段五：服务调用 | ⏭️ 待开始 | OpenFeign 深入 |
| 阶段六：分布式事务 | ⏭️ 待开始 | Seata |
| 阶段七：消息队列 | ⏭️ 待开始 | RocketMQ |
| 阶段八：高级特性 | ⏭️ 待开始 | 链路追踪、监控 |

---

## 📝 重要决策

- 使用本地兜底配置，确保 Nacos 不可用时服务仍能启动
- Nacos 配置优先级高于本地配置

---

## 📞 联系信息

- **用户称呼：** boss
- **时区：** Asia/Shanghai

---

**最后更新：** 2026-04-04
