# ✅ 第三阶段完成 - 配置中心（Nacos Config）

## 🎉 项目构建成功

```
[INFO] BUILD SUCCESS
```

---

## 📦 本阶段完成内容

| 任务 | 状态 |
|------|------|
| Nacos Config 依赖添加 | ✅ |
| User 服务 bootstrap.yml 创建 | ✅ |
| Course 服务 bootstrap.yml 创建 | ✅ |
| application.yml 简化 | ✅ |
| UserProperties 配置类创建 | ✅ |
| CourseProperties 配置类创建 | ✅ |
| UserController 添加配置接口 | ✅ |
| CourseController 添加配置接口 | ✅ |

---

## 🚀 实践步骤

### 1. 确保 Nacos Server 已启动

```bash
cd D:\env\nacos\nacos\bin
startup.cmd -m standalone
```

访问：`http://localhost:8848/nacos`

### 2. 在 Nacos 创建配置

**参考文档：** `docs/NACOS_CONFIG_EXAMPLES.md`

**创建两个配置：**

| Data ID | Group | 格式 |
|---------|-------|------|
| `yunh-service-user.yaml` | DEFAULT_GROUP | YAML |
| `yunh-service-course.yaml` | DEFAULT_GROUP | YAML |

**配置内容参考：** `docs/NACOS_CONFIG_EXAMPLES.md`

### 3. 启动 User 服务

IDEA 中运行 `UserServiceApplication.java`

**观察日志：**
```
Loading config from Nacos
Connect to Nacos config success
```

### 4. 启动 Course 服务

同样方式启动 Course 服务。

### 5. 测试配置加载

**访问 User 服务配置接口：**
```
http://localhost:9001/user/config
```

**预期响应：**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "welcome": "欢迎来到用户中心 - 开发环境",
        "version": "1.0.0-dev",
        "message": "配置来自 Nacos，修改后无需重启即可生效"
    }
}
```

**访问 Course 服务配置接口：**
```
http://localhost:9002/course/config
```

### 6. 测试动态刷新（重点！）

**步骤：**

1. 在 Nacos 控制台找到 `yunh-service-user.yaml`
2. 点击"编辑"
3. 修改配置：
   ```yaml
   yunh:
     user:
       welcome: "🎉 动态刷新成功！配置已更新"
       version: "1.0.1"
   ```
4. 点击"发布"
5. 立即访问：`http://localhost:9001/user/config`
6. 观察 `welcome` 和 `version` 字段已更新

**✅ 无需重启服务，配置实时生效！**

---

## 📊 架构变化

### 第二阶段架构
```
服务配置在本地 application.yml
修改配置 → 重启服务
```

### 第三阶段架构（加入配置中心）
```
┌─────────────┐    ┌─────────────┐
│  User 服务   │    │ Course 服务  │
└──────┬──────┘    └──────┬──────┘
       │                  │
       └──────────────────┘
                ↓
       ┌─────────────────┐
       │  Nacos Config   │
       │   配置中心      │
       │                 │
       │ yunh-service-   │
       │ user.yaml       │
       │                 │
       │ yunh-service-   │
       │ course.yaml     │
       └─────────────────┘
              ↓
       修改配置 → 实时生效
```

---

## 🔑 核心知识点

### 1. bootstrap.yml 优先级

```
bootstrap.yml → application.yml → Nacos 配置
```

`bootstrap.yml` 用于加载系统级别的配置，优先级更高。

### 2. Nacos Config 配置

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        prefix: ${spring.application.name}
        refresh-enabled: true
```

### 3. @RefreshScope 注解

```java
@RefreshScope  // 配置变更时自动刷新
@ConfigurationProperties(prefix = "yunh.user")
public class UserProperties {
    private String welcome;
    // getter/setter
}
```

### 4. 配置优先级

```
1. 命令行参数
2. 环境变量
3. Nacos 配置（特定环境）
4. Nacos 配置（默认）
5. application-{profile}.yml
6. application.yml
```

### 5. 多环境配置

```
yunh-service-user-dev.yaml   → 开发环境
yunh-service-user-test.yaml  → 测试环境
yunh-service-user-prod.yaml  → 生产环境
```

---

## 🤔 思考题

1. **为什么需要 bootstrap.yml？**
   - bootstrap.yml 优先级更高，在 application.yml 之前加载
   - 用于加载配置中心等系统级配置

2. **@RefreshScope 的工作原理？**
   - 创建 Bean 的代理
   - 配置变更时销毁原 Bean
   - 下次请求时重新创建 Bean

3. **如何保证配置变更的安全性？**
   - Nacos 权限控制
   - 配置变更审计
   - 敏感配置加密

4. **配置中心挂了怎么办？**
   - 本地缓存配置
   - 服务仍能启动（使用本地配置）

5. **如何实现配置版本管理？**
   - Nacos 配置历史版本
   - 配置变更回滚

---

## 📚 常见问题

### Q1: 配置不生效？
A: 检查 `bootstrap.yml` 是否正确，确保 Data ID 匹配。

### Q2: 动态刷新不生效？
A: 确保类上有 `@RefreshScope` 注解。

### Q3: 日志显示 "Connect to Nacos config failed"？
A: 检查 Nacos 是否启动，server-addr 是否正确。

### Q4: 如何查看配置加载日志？
A: 添加日志配置：
```yaml
logging:
  level:
    com.alibaba.cloud.nacos: DEBUG
```

### Q5: 多环境如何切换？
A: 修改 `bootstrap.yml` 中的 `spring.profiles.active`。

---

## ✅ 完成检查清单

- [ ] Nacos Config 依赖已添加
- [ ] bootstrap.yml 创建完成（User 和 Course 服务）
- [ ] application.yml 已简化
- [ ] Nacos 中创建了服务配置
- [ ] 服务能成功从 Nacos 加载配置
- [ ] 能访问 `/user/config` 和 `/course/config`
- [ ] 配置动态刷新验证成功
- [ ] 理解配置优先级
- [ ] 理解 @RefreshScope 原理

---

## 🎯 下一步：第四阶段 - 服务网关（Gateway）

第四阶段将学习：
- Spring Cloud Gateway 基础
- 路由配置
- 过滤器（Filter）
- 集成 Sentinel 限流

---

**恭喜你完成第三阶段！** 🎓

继续加油！
