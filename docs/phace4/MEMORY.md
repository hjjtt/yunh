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
| 控制台地址 | `http://127.0.0.1:8848/nacos` |
| 用户名 | `nacos` |
| 密码 | `123456` |
| 启动命令 | `startup.cmd -m standalone` |

### Nacos 配置中心 - 敏感数据管理

| Data ID | Group | 格式 | 包含敏感数据 |
|---------|-------|------|-------------|
| `yunh-service-user.yml` | DEFAULT_GROUP | YAML | ✅ 数据库密码 |
| `yunh-service-course.yml` | DEFAULT_GROUP | YAML | ✅ 数据库密码 |

**安全策略**: 
- 本地 `application.yml` 不包含数据库密码，敏感数据统一从 Nacos 加载
- **无本地兜底配置** - 服务启动完全依赖 Nacos Config
- bootstrap.yml 配置 Nacos 账号密码用于认证

**bootstrap.yml 关键配置**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: your_nacos_username
        password: your_password_here
      config:
        server-addr: 127.0.0.1:8848
        username: your_nacos_username
        password: your_password_here
```

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

| 阶段 | 状态 | 说明 | 完成时间 |
|------|------|------|----------|
| 阶段一：基础搭建 | ✅ 完成 | Maven 多模块、统一响应、异常处理 | 2026-04-03 |
| 阶段二：服务注册与发现 | ✅ 完成 | Nacos Discovery、Feign 调用 | 2026-04-03 |
| 阶段三：配置中心 | ✅ 完成 | Nacos Config、动态刷新、敏感数据管理 | 2026-04-04 |
| 阶段四：服务网关 | ⏭️ 待开始 | Spring Cloud Gateway | - |
| 阶段五：服务调用 | ⏭️ 待开始 | OpenFeign 深入 | - |
| 阶段六：分布式事务 | ⏭️ 待开始 | Seata | - |
| 阶段七：消息队列 | ⏭️ 待开始 | RocketMQ | - |
| 阶段八：高级特性 | ⏭️ 待开始 | 链路追踪、监控 | - |

---

## 📝 重要决策

### 敏感数据管理（2026-04-04）
- **策略**: 数据库密码等敏感数据统一从 Nacos Config 加载
- **本地兜底**: 无 - 服务启动完全依赖 Nacos Config
- **Nacos 配置**: Data ID 格式为 `${spring.application.name}.yml`
- **安全策略**: `.gitignore` 排除敏感配置文件

### 阶段三完成记录（2026-04-04）
- ✅ Nacos 服务器成功启动
- ✅ User 服务（9001）和 Course 服务（9002）成功启动
- ✅ 配置从 Nacos 成功加载（包含数据库密码）
- ✅ 配置动态刷新验证通过
- ✅ 服务注册到 Nacos Discovery

### 配置修改总结
1. `bootstrap.yml`: 启用 Nacos 注册中心和配置中心，配置账号密码
2. `application.yml`: 移除敏感数据，添加 Actuator 日志屏蔽配置
3. MyBatis Plus 配置暂时注释，后续开发时取消

---

## 🎯 阶段四：服务网关（Spring Cloud Gateway）

**学习目标**:
- 理解网关在微服务架构中的作用
- 掌握 Spring Cloud Gateway 的核心概念（路由、断言、过滤器）
- 实现统一鉴权、限流、跨域等功能

**典型应用场景**:
- 统一入口：所有外部请求通过网关访问内部服务
- 身份验证：JWT 校验、权限拦截
- 路由转发：根据路径/域名转发到不同服务
- 限流熔断：保护后端服务

---

## 📞 联系信息

- **用户称呼：** boss
- **时区：** Asia/Shanghai

---

**最后更新：** 2026-04-04
