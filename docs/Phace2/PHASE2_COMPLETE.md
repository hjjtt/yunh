# ✅ 第二阶段完成 - 服务注册与发现（Nacos）

## 🎉 项目构建成功

```
[INFO] BUILD SUCCESS
```

***

## 📦 本阶段完成内容

| 任务                           | 状态 |
| ---------------------------- | -- |
| Nacos 配置添加到 User 服务          | ✅  |
| Nacos 配置添加到 Course 服务        | ✅  |
| @EnableDiscoveryClient 注解启用  | ✅  |
| Feign 依赖添加到 Course 服务        | ✅  |
| UserFeignClient 创建           | ✅  |
| CourseDetailVO 创建            | ✅  |
| CourseController 添加 Feign 调用 | ✅  |

***

## 🚀 运行步骤

### 1. 安装并启动 Nacos

**下载 Nacos 1.7.0：**

```
https://github.com/alibaba/nacos/releases/download/v1.7.0/nacos-server-1.7.0.zip
```

**解压到：** `D:\env\nacos\`

**启动（单机模式）：**

```bash
cd D:\env\nacos\nacos\bin
startup.cmd -m standalone
```

**访问控制台：** `http://localhost:8848/nacos`

- 账号：`nacos`
- 密码：`123456`

### 2. 启动 User 服务

**IDEA:**

- 找到 `UserServiceApplication.java`
- 右键 → Run

**观察日志：**

```
Registering app yunh-service-user to Nacos
```

**验证：** Nacos 控制台 → 服务列表 → 应该看到 `yunh-service-user`

### 3. 启动 Course 服务

同样方式启动 Course 服务。

**验证：** Nacos 控制台应该看到两个服务。

### 4. 测试接口

**直接调用 User 服务：**

```
http://localhost:9001/user/list
http://localhost:9001/user/1
```

**直接调用 Course 服务：**

```
http://localhost:9002/course/list
http://localhost:9002/course/1
```

**测试 Feign 调用（重点）：**

```
http://localhost:9002/course/1/detail
```

**预期响应：**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "course": {
            "id": 1,
            "name": "Spring Cloud 微服务实战",
            "teacherId": 1,
            "teacherName": "张老师",
            "price": 199.00
        },
        "teacher": {
            "id": 1,
            "username": "zhangsan",
            "nickname": "张三",
            "email": "zhangsan@example.com"
        }
    },
    "timestamp": 1234567890
}
```

***

## 📊 架构变化

### 第一阶段架构

```
User 服务 (9001)  ← 独立
Course 服务 (9002) ← 独立
```

### 第二阶段架构（加入 Nacos）

```
                    ┌─────────────────┐
                    │   Nacos Server  │
                    │   (8848)        │
                    │                 │
User 服务 (9001) ───┤→ 注册中心       │
   注册             │                 │
                    │  服务列表：      │
Course 服务 (9002) ─┤→ - User 服务    │
   注册             │  - Course 服务  │
                    └─────────────────┘
                           ↑
                           │ 查询
                           │
                    Course 服务
                    (通过 Feign 调用 User)
```

***

## 🔑 核心知识点

### 1. Nacos 配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos 地址
        namespace: public            # 命名空间
```

### 2. 服务注册注解

```java
@EnableDiscoveryClient  // 启用服务注册与发现
```

### 3. Feign Client 定义

```java
@FeignClient(name = "yunh-service-user")
public interface UserFeignClient {
    @GetMapping("/user/{id}")
    Result<User> getById(@PathVariable("id") Long id);
}
```

### 4. 启用 Feign

```java
@EnableFeignClients  // 扫描 Feign Client
```

### 5. 服务间调用

```java
@Autowired
private UserFeignClient userFeignClient;

// 调用远程服务
Result<User> result = userFeignClient.getById(1L);
```

***

## 🤔 思考题答案提示

1. **Nacos 如何知道服务是否健康？**
   - 服务定期发送心跳（默认 5 秒）
   - Nacos 超过 15 秒未收到心跳标记为不健康
   - 超过 30 秒剔除服务
2. **@EnableDiscoveryClient vs @EnableEurekaServer？**
   - `@EnableDiscoveryClient`：客户端，注册到其他注册中心
   - `@EnableEurekaServer`：服务端，自己作为注册中心
3. **Feign 底层原理？**
   - 动态代理生成实现类
   - Ribbon 负载均衡
   - HTTP Client 发送请求
4. **Ribbon 负载均衡策略？**
   - RoundRobin（轮询）
   - Random（随机）
   - Retry（重试）
   - WeightedResponseTime（响应时间权重）
5. **User 服务挂了怎么办？**
   - 熔断降级（Sentinel/Hystrix）
   - 服务降级返回默认值

***

## 📚 常见问题

### Q1: Nacos 启动报错？

A: 检查 JDK 版本（1.8+），确保 8848 端口未被占用。

### Q2: 服务注册不上？

A: 检查 `spring.application.name` 和 `server-addr` 配置。

### Q3: Feign 调用报错 "no instances available"？

A: 确保 User 服务已启动并在 Nacos 中可见。

### Q4: 如何查看 Nacos 日志？

A: `nacos/logs/nacos.log`

***

## ✅ 完成检查清单

- [x] Nacos Server 成功启动
- [x] 能访问 Nacos 控制台（<http://localhost:8848/nacos）>
- [x] User 服务在 Nacos 服务列表中可见
- [x] Course 服务在 Nacos 服务列表中可见
- [x] 能访问 `http://localhost:9001/user/list`
- [x] 能访问 `http://localhost:9002/course/list`
- [x] 能访问 `http://localhost:9002/course/1/detail`（Feign 调用）
- [x] 理解服务注册与发现的原理
- [x] 理解 Feign 的工作原理

***

## 🎯 下一步：第三阶段 - 配置中心（Nacos Config）

第三阶段将学习：

- Nacos Config 基础
- 配置动态刷新（@RefreshScope）
- 多环境配置（dev/test/prod）
- 配置加密

***

**恭喜你完成第二阶段！** 🎓

继续加油！
