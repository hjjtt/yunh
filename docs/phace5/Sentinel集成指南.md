# Sentinel 集成指南

## 一、Sentinel 是什么？

**Sentinel** 是阿里巴巴开源的**面向分布式服务架构的流量控制组件**，主要以流量为切入点，提供以下核心功能：

| 功能 | 说明 |
|------|------|
| **流量控制（Flow Control）** | 限制 QPS（每秒请求数），防止服务被突发流量压垮 |
| **熔断降级（Circuit Breaking）** | 当服务调用失败率或响应时间超过阈值时，自动熔断，避免级联故障 |
| **系统自适应保护** | 根据系统负载（CPU、RT、线程数等）自动调节入口流量 |
| **热点参数限流** | 针对某个热点参数（如商品 ID）进行精细化限流 |
| **来源访问控制** | 根据调用来源进行黑白名单控制 |

### Sentinel vs Hystrix 对比

| 对比项 | Sentinel | Hystrix |
|--------|----------|---------|
| 维护状态 | 阿里持续维护 | 已停止维护（进入维护模式） |
| 流量控制 | 支持 QPS 限流 | 不支持 |
| 熔断策略 | 慢调用比例、异常比例、异常数 | 异常比例、异常数 |
| 实时监控 | 内置 Dashboard 可视化 | 需借助 Turbine |
| 规则配置 | 动态（支持 Nacos 等数据源） | 静态（配置文件） |
| 扩展性 | 多种数据源、SPI 扩展 | 有限 |

> **结论**：本项目原来使用 Hystrix 作为熔断降级方案，现已切换为 Sentinel。

---

## 二、本项目集成架构

```
                          ┌─────────────────────┐
                          │   Sentinel Dashboard │
                          │   (端口: 8080)        │
                          │   可视化监控与规则配置  │
                          └──────────┬───────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ▼                      ▼                      ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │  Gateway (9000)  │  │  User (9001)     │  │  Course (9002)   │
   │  Sentinel 整合   │  │  Sentinel 整合   │  │  Sentinel 整合   │
   │  Gateway 限流    │  │  Feign 降级      │  │  Feign 降级      │
   │  端口: 8721      │  │  端口: 8719      │  │  端口: 8720      │
   └──────────────────┘  └──────────────────┘  └──────────────────┘
              │                      │                      │
              └──────────────────────┼──────────────────────┘
                                     │
                          ┌──────────▼───────────┐
                          │    Nacos (8848)       │
                          │    服务注册/配置中心    │
                          └──────────────────────┘
```

### 集成覆盖范围

| 服务 | 集成内容 | Sentinel 端口 |
|------|----------|---------------|
| yunh-service-gateway | Sentinel + Gateway 限流适配器 | 8721 |
| yunh-service-user | Sentinel + Feign 整合 | 8719 |
| yunh-service-course | Sentinel + Feign 整合 | 8720 |

---

## 三、集成改动详情

### 3.1 依赖引入

#### yunh-service-common（服务公共模块）

所有业务服务（user、course）都依赖此模块，添加 Sentinel 后自动对所有服务生效：

```xml
<!-- Sentinel 流控降级 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

#### yunh-service-gateway（网关模块）

网关需要额外的 Gateway 适配器：

```xml
<!-- Sentinel 流控降级 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- Sentinel 整合 Gateway -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>
```

> **版本说明**：版本由根 POM 的 `spring-cloud-alibaba-dependencies`（2.2.1.RELEASE）统一管理，Sentinel Core 版本为 1.7.1。

### 3.2 Bootstrap 配置

每个服务的 `bootstrap.yml` 中新增 Sentinel 连接配置：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8080    # Sentinel Dashboard 地址
        port: 8719                    # 与 Dashboard 通信端口（每个服务需不同）
      eager: true                     # 服务启动时立即初始化 Sentinel
```

**端口分配**：
- user 服务：8719
- course 服务：8720
- gateway 服务：8721

> `eager: true` 表示服务启动时立即向 Dashboard 注册，无需等待第一次请求。

### 3.3 Feign 整合

Nacos 远程配置中已将 `feign.hystrix.enabled` 替换为 `feign.sentinel.enabled`：

```yaml
feign:
  sentinel:
    enabled: true    # 启用 Sentinel 作为 Feign 的熔断降级方案
```

现有的 Feign fallback 类（如 `UserFeignClientFallback`、`CourseFeignClientFallback`）**无需修改**，Sentinel 完全兼容 Hystrix 的 fallback 机制。

### 3.4 Gateway 限流配置

创建了 `SentinelGatewayConfig` 配置类，功能包括：

1. **注册 Sentinel 过滤器**：拦截所有经过网关的请求
2. **自定义限流响应**：被限流时返回 HTTP 429 + JSON 错误信息
3. **异常处理器**：处理 Sentinel 触发的阻塞异常

限流响应格式：
```json
{
    "code": 429,
    "message": "请求过于频繁，请稍后再试"
}
```

---

## 四、Sentinel Dashboard 使用

### 4.1 启动 Dashboard

1. 下载 Sentinel Dashboard JAR 包：
   ```
   https://github.com/alibaba/Sentinel/releases/download/1.7.1/sentinel-dashboard-1.7.1.jar
   ```
   > 版本需与项目中 Sentinel Core 版本一致（1.7.1）

2. 启动 Dashboard：
   ```bash
   java -jar sentinel-dashboard-1.7.1.jar
   ```

3. 访问 Dashboard：`http://127.0.0.1:8080`
   - 默认账号：`sentinel`
   - 默认密码：`sentinel`

### 4.2 Dashboard 核心功能

| 菜单 | 功能说明 |
|------|----------|
| 实时监控 | 查看各服务的 QPS、响应时间、异常数等实时数据 |
| 流控规则 | 配置限流规则（QPS 阈值、流控模式等） |
| 降级规则 | 配置熔断策略（慢调用比例、异常比例、异常数） |
| 热点规则 | 针对具体参数进行限流 |
| 系统规则 | 全局系统级保护规则 |
| 网关流控 | 专门针对 API 网关的限流规则（按路由 ID 或 API 分组） |
| 集群流控 | 集群模式下的统一限流 |

### 4.3 配置限流规则示例

#### 对 Gateway 路由限流

1. 在 Dashboard 左侧找到 `yunh-service-gateway`
2. 点击「**流控规则**」→ 点击「**新增网关流控**」按钮（注意：在 1.7.1 版本中，网关流控入口在「流控规则」页面内）
3. 配置：
   - API 名称：选择路由 ID（如 `yunh-service-user`）
   - 限流阈值：10（QPS）
4. 保存后立即生效

#### 对普通服务接口限流

1. 在 Dashboard 左侧找到 `yunh-service-user`
2. 先访问一次该服务的接口（Sentinel 是懒加载的，需要先有请求才显示资源）
3. 点击「流控规则」→「新增流控」
4. 配置项详解：
   - **资源名**：要限流的接口路径，如 `GET:/user/{id}`（从下拉列表选择）
   - **针对来源**：填 `default` 表示不区分调用方；填具体服务名如 `yunh-service-course` 表示只对该调用方限流
   - **阈值类型**：选 `QPS`（每秒请求数）最常用；`线程数` 用于保护慢调用
   - **单机阈值**：每台机器允许的最大值，建议先设 10~20 进行测试
   - **是否集群**：选 `否`（单机限流即可）
   - **流控模式**：
     - `直接`：直接对当前资源限流（**推荐，最常用**）
     - `关联`：当关联资源达到阈值时限流当前资源（如写接口达到阈值时限制读接口）
     - `链路`：只限制从某个入口过来的流量（精细控制调用链路）
   - **流控效果**：
     - `快速失败`：超出阈值直接拒绝，返回 429（**推荐，默认选择**）
     - `Warm Up`：冷启动，阈值从低到高逐渐达到设定值（防止冷启动瞬间流量冲击）
     - `排队等待`：请求排队匀速通过（削峰填谷，处理突发流量）
5. 保存后立即生效

#### 配置熔断降级规则

1. 在 Dashboard 找到目标服务
2. 点击「降级规则」→「新增降级」
3. 配置示例：
   - 资源名：`GET:/user/{id}`
   - 策略：慢调用比例
   - 最大 RT：500ms（超过 500ms 算慢调用）
   - 比例阈值：0.5（50% 慢调用触发熔断）
   - 熔断时长：10s
   - 最小请求数：5

---

## 五、核心概念

### 5.1 资源（Resource）

资源是 Sentinel 保护的对象，可以是：
- 一个接口方法
- 一个服务调用
- 一个 Gateway 路由

### 5.2 规则（Rule）

定义如何保护资源，主要类型：

| 规则类型 | 核心参数 | 适用场景 |
|----------|----------|----------|
| 流控规则 | QPS 阈值、流控模式（直接/关联/链路） | 限流保护 |
| 降级规则 | 策略（慢调用比例/异常比例/异常数）、阈值、熔断时长 | 熔断降级 |
| 热点规则 | 参数索引、阈值 | 热点数据限流 |
| 系统规则 | CPU 使用率、RT、线程数、入口 QPS | 全局保护 |

### 5.3 流控模式

| 模式 | 说明 |
|------|------|
| 直接 | 资源自身达到阈值时限流 |
| 关联 | 关联资源达到阈值时限流当前资源 |
| 链路 | 只统计从指定入口进来的流量 |

### 5.4 流控效果

| 效果 | 说明 |
|------|------|
| 快速失败 | 直接拒绝请求（默认） |
| Warm Up | 预热模式，逐步增加流量 |
| 排队等待 | 匀速通过，适合突发流量 |

---

## 六、Sentinel 注解使用

除了在 Dashboard 配置规则，还可以通过注解在代码中定义资源：

```java
@SentinelResource(value = "getUserById",
    blockHandler = "getUserByIdBlockHandler",    // 限流/降级处理
    fallback = "getUserByIdFallback")             // 异常处理
public Result<User> getById(Long id) {
    // 业务逻辑
}

// 限流时的处理方法（必须与原方法返回类型一致）
public Result<User> getUserByIdBlockHandler(Long id, BlockException ex) {
    return Result.error("请求过于频繁");
}

// 异常时的处理方法
public Result<User> getUserByIdFallback(Long id, Throwable ex) {
    return Result.error("服务异常: " + ex.getMessage());
}
```

> **注意**：`blockHandler` 处理限流/熔断触发的情况，`fallback` 处理业务异常。

---

## 七、注意事项

1. **Sentinel 是懒加载的**：服务启动后需要在 Dashboard 中先看到资源（通过访问一次接口），才能配置规则。已在 bootstrap.yml 中配置 `eager: true` 加速注册。

2. **规则持久化**：Dashboard 中配置的规则默认存储在内存中，服务重启后丢失。生产环境建议接入 Nacos 数据源实现规则持久化。

3. **端口冲突**：每个服务的 `sentinel.transport.port` 必须不同，用于与 Dashboard 建立通信连接。

4. **Hystrix 兼容**：已将 `feign.hystrix.enabled` 替换为 `feign.sentinel.enabled`，原有的 fallback 类无需修改。

5. **Dashboard 版本匹配**：Dashboard 版本需与项目中 Sentinel Core 版本（1.7.1）保持一致。

---

## 八、快速验证步骤

1. **启动 Nacos**：确保 Nacos Server 在 8848 端口运行
2. **启动 Sentinel Dashboard**：`java -jar sentinel-dashboard-1.7.1.jar`
3. **启动各微服务**：依次启动 gateway（9000）、user（9001）、course（9002）
4. **访问 Dashboard**：`http://127.0.0.1:8080`，在左侧菜单查看各服务是否注册成功
5. **发送测试请求**：通过 Gateway 访问接口，如 `http://localhost:9000/api/user/1`
6. **配置流控规则**：在 Dashboard 中为接口添加 QPS=1 的限流规则
7. **验证限流**：快速多次请求，应返回 429 错误
