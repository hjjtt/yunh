# yunh-cloud 网关限流集成成果报告

## 一、项目概述

本项目为 yunh-cloud 微服务平台，基于 Spring Cloud Alibaba 技术栈，在 API 网关层集成了 Sentinel 实现流量控制。

### 技术版本

| 组件                   | 版本               |
| -------------------- | ---------------- |
| Spring Boot          | 2.2.5.RELEASE    |
| Spring Cloud         | Hoxton.SR3       |
| Spring Cloud Alibaba | 2.2.1.RELEASE    |
| Sentinel Core        | 1.7.1            |
| Sentinel Dashboard   | 1.7.1            |
| JDK                  | 20（需额外 JVM 参数兼容） |
| Nacos                | 3.1.1            |

### 服务架构

| 服务                   | 端口   | 说明          |
| -------------------- | ---- | ----------- |
| yunh-service-gateway | 9000 | API 网关      |
| yunh-service-user    | 9001 | 用户服务        |
| yunh-service-course  | 9002 | 课程服务        |
| Nacos                | 8848 | 注册中心 + 配置中心 |
| Sentinel Dashboard   | 8088 | 流控管理控制台     |

***

## 二、最终实现效果

### 限流验证结果

通过 Gateway 网关快速发送 10 个请求（QPS 阈值设为 2）：

<br />

<br />

测试代码

```
for ($i = 1; $i -le 10; $i++) { 
    try { 
        $r = Invoke-WebRequest -Uri "http://localhost:9000/api/user/list" -Headers @{"Authorization"="Bearer mock-jwt-token-test"} -UseBasicParsing
        Write-Host "第${i}次: $($r.StatusCode)" 
    } catch { 
        Write-Host "第${i}次: 被限流 - $($_.Exception.Message)" 
    } 
}





第1次: 200          ← 正常放行
第2次: 200          ← 正常放行
第3次: 被限流       ← Sentinel 限流生效
第4次: 被限流
第5次: 被限流
...
```

被限流时返回：

```json
HTTP 429 Too Many Requests
{"code":429,"message":"请求过于频繁，请稍后再试"}
```

### 管理方式

支持两种方式管理网关流控规则：

1. **Dashboard 可视化管理**：在 Sentinel Dashboard（<http://127.0.0.1:8088）中，进入网关服务的"流控规则>" → "新增网关流控"，即可动态添加/修改/删除限流规则，实时生效无需重启。
2. **代码初始化兜底**：`SentinelGatewayConfig.java` 中硬编码了默认规则（QPS=2），确保即使 Dashboard 未配置规则，也有基础限流保护。

***

## 三、修改文件清单

### 3.1 新增文件

#### `src/main/resources/META-INF/services/com.alibaba.csp.sentinel.slotchain.SlotChainBuilder`

```
com.alibaba.csp.sentinel.adapter.gateway.common.slot.GatewaySlotChainBuilder
```

**作用**：这是解决限流不生效的**核心文件**。通过 Java SPI 机制显式指定使用 `GatewaySlotChainBuilder`，确保 Sentinel 的 Slot Chain 中包含 `GatewayFlowSlot`，从而让 `GatewayFlowRule`（网关流控规则）能够被正确处理。

#### `src/main/java/com/yunh/gateway/config/SentinelGatewayConfig.java`

**作用**：Sentinel 网关限流的核心配置类，负责：

- 注册 `SentinelGatewayFilter`（拦截请求进行限流检查）
- 注册 `SentinelGatewayBlockExceptionHandler`（处理限流异常）
- 定义 API 分组（`user-api` 匹配 `/api/user/**`，`course-api` 匹配 `/api/course/**`）
- 初始化默认限流规则（Route ID 模式，QPS=2）
- 自定义限流响应（HTTP 429 + JSON）

### 3.2 修改文件

#### `GatewayApplication.java`

在 `main()` 方法第一行添加了：

```java
System.setProperty("csp.sentinel.app.type", "1");
```

**作用**：告知 Sentinel Dashboard 这是一个网关类型的应用，Dashboard 才会显示网关流控相关的 UI 入口。

#### `bootstrap.yml`（Gateway）

修改点：

- `sentinel.transport.dashboard` 端口从 `8080` 改为 `8088`（避免与本地其他服务端口冲突）
- 添加 `sentinel.eager: true`（服务启动时立即注册到 Dashboard，而非等待首次请求）
- 添加 `sentinel.filter.enabled: false`（禁用 Servlet 环境的 Sentinel Filter，避免与 Gateway 的 GlobalFilter 冲突）

#### `application.yml`（Gateway）

修改点：

- 添加 `sentinel.filter.enabled: false`（同上，双保险）
- 添加 `sentinel.scg.fallback` 配置（限流时的降级响应）

#### `bootstrap.yml`（用户服务、课程服务）

修改点：

- `sentinel.transport.dashboard` 端口从 `8080` 改为 `8088`

#### `pom.xml`（Gateway）

新增依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>
```

***

## 四、如何自动添加网关流控到 Sentinel

### 4.1 工作原理

Sentinel 的网关限流基于 **Slot Chain** 机制。请求经过 `SentinelGatewayFilter` 时，会依次经过一系列 Slot 处理：

```
请求进入 SentinelGatewayFilter
    │
    ▼
GatewayFlowSlot（检查 GatewayFlowRule 是否触发限流）
    │
    ▼
NodeSelectorSlot（构建资源调用树）
    │
    ▼
ClusterBuilderSlot（构建集群节点）
    │
    ▼
StatisticSlot（实时数据统计）
    │
    ▼
FlowSlot（检查标准 FlowRule）
    │
    ▼
... 其他 Slot
```

其中 `GatewayFlowSlot` 是网关限流的**核心 Slot**，它专门处理 `GatewayFlowRule`（网关流控规则）。这个 Slot 不在 Sentinel Core 的默认 Slot Chain 中，需要通过 `GatewaySlotChainBuilder` 来添加。

### 4.2 SPI 机制的作用

Sentinel 使用 Java SPI（Service Provider Interface）机制来发现 `SlotChainBuilder` 的实现：

```
classpath 中有三个 SPI 注册来源：
  ① 项目自身:  META-INF/services/com.alibaba.csp.sentinel.slotchain.SlotChainBuilder
     → GatewaySlotChainBuilder（包含 GatewayFlowSlot）

  ② sentinel-api-gateway-adapter-common-1.7.1.jar:
     → GatewaySlotChainBuilder

  ③ sentinel-core-1.7.1.jar:
     → DefaultSlotChainBuilder（不包含 GatewayFlowSlot）
```

Java SPI 的 `ServiceLoader` 只会加载**第一个**找到的实现。如果 classpath 排序导致 `sentinel-core` 的 `DefaultSlotChainBuilder` 被优先加载，那么 `GatewayFlowSlot` 就不存在于 Slot Chain 中，所有 `GatewayFlowRule` 都将形同虚设。

**这就是之前限流不生效的根本原因。**

### 4.3 解决方案

在项目 `src/main/resources/META-INF/services/` 下创建 SPI 文件，显式指定 `GatewaySlotChainBuilder`：

**文件路径**：`src/main/resources/META-INF/services/com.alibaba.csp.sentinel.slotchain.SlotChainBuilder`

**文件内容**：

```
com.alibaba.csp.sentinel.adapter.gateway.common.slot.GatewaySlotChainBuilder
```

**原理**：项目自身 classpath 的 SPI 文件会优先于依赖 jar 中的 SPI 文件被加载。这确保了无论 classpath 中有多少个 `SlotChainBuilder` 实现，项目指定的 `GatewaySlotChainBuilder` 始终被使用。

### 4.4 规则加载方式对比

| 方式                                       | 说明                            | 是否需要 SPI 修复 |  Dashboard 可管理  |
| ---------------------------------------- | ----------------------------- | :---------: | :-------------: |
| `GatewayFlowRule` + `GatewayRuleManager` | 网关专用规则，支持 Route ID 和 API 分组匹配 |     ✅ 需要    |       ✅ 是       |
| `FlowRule` + `FlowRuleManager`           | 标准 Sentinel 规则，不区分网关路由        |    ❌ 不需要    | ✅ 是（但不是网关流控 UI） |

本项目选择 `GatewayFlowRule` 方式，因为它与 Sentinel Dashboard 的"网关流控"UI 完美对接。

### 4.5 代码中自动初始化规则

`SentinelGatewayConfig.java` 的 `@PostConstruct init()` 方法在 Spring 容器启动时自动执行，完成以下初始化：

```java
@PostConstruct
public void init() {
    // 1. 注册自定义限流响应（返回 429 + JSON）
    initCustomizedBlockHandler();

    // 2. 注册 API 分组定义（让 Dashboard 能识别 API 维度）
    initApiDefinitions();

    // 3. 加载默认限流规则（Route ID 维度，QPS=2）
    initGatewayRules();
}
```

这些规则作为兜底保护，即使 Dashboard 没有配置规则，也能提供基础限流。

### 4.6 Dashboard 推送规则的流程

```
用户在 Dashboard 添加网关流控规则
    │
    ▼
Dashboard 通过 HTTP 推送规则到 Gateway 的 Sentinel 客户端端口（8721）
    │
    ▼
Gateway 的 SentinelTransport 接收规则
    │
    ▼
GatewayRuleManager.loadRules() 更新内存中的规则
    │
    ▼
后续请求经过 SentinelGatewayFilter 时使用新规则
```

***

## 五、在 Dashboard 中配置网关流控的步骤

1. 确保 Nacos、Sentinel Dashboard、Gateway 服务都已启动
2. 打开 Dashboard：<http://127.0.0.1:8088（账号密码：sentinel> / sentinel）
3. 在左侧菜单找到 `yunh-service-gateway` 服务
4. 点击"流控规则"
5. 点击"新增网关流控"按钮
6. 配置规则：

| 字段                | 说明                | 示例值                 |
| ----------------- | ----------------- | ------------------- |
| API 类型            | Route ID 或 API 分组 | Route ID            |
| Route ID / API 名称 | 路由 ID 或 API 分组名   | `yunh-service-user` |
| 阈值类型              | QPS 或线程数          | QPS                 |
| QPS 阈值            | 每秒允许的最大请求数        | 10                  |
| 间隔                | 统计时间窗口            | 1 秒                 |
| 流控方式              | 快速失败 或 匀速排队       | 快速失败                |

1. 点击"新增"即可实时生效

### Route ID 与 API 分组的区别

| 匹配方式     | 说明                         | 示例                                    |
| -------- | -------------------------- | ------------------------------------- |
| Route ID | 按网关路由 ID 匹配，一条规则覆盖该路由下所有路径 | `yunh-service-user` 覆盖 `/api/user/**` |
| API 分组   | 按自定义 API 分组匹配，可灵活组合多个路径模式  | `user-api` 匹配 `/api/user/**`          |

当前项目定义的对应关系：

| Route ID              | API 分组名      | 匹配路径             |
| --------------------- | ------------ | ---------------- |
| `yunh-service-user`   | `user-api`   | `/api/user/**`   |
| `yunh-service-course` | `course-api` | `/api/course/**` |

***

## 六、启动顺序

正确的服务启动顺序：

```
1. Nacos Server       → http://127.0.0.1:8848
2. Sentinel Dashboard → java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -jar sentinel-dashboard-1.7.1.jar --server.port=8088
3. yunh-service-user      (端口 9001)
4. yunh-service-course    (端口 9002)
5. yunh-service-gateway   (端口 9000)
```

***

## 七、踩坑记录

### 问题1：Sentinel Dashboard 1.7.1 无法在 JDK 20 上启动

**现象**：`IllegalStateException: Cannot load configuration class`

**原因**：JDK 17+ 对模块化访问有更严格的限制

**解决**：启动时添加 JVM 参数：

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -jar sentinel-dashboard-1.7.1.jar --server.port=8088
```

### 问题2：Dashboard 不显示"网关流控规则"菜单

**现象**：Dashboard 侧边栏只显示"流控规则"、"降级规则"等，没有"网关流控规则"

**原因**：

1. 未设置 `csp.sentinel.app.type=1`
2. 在 Dashboard 1.7.1 中，网关流控是**嵌入在"流控规则"页面内**的"新增网关流控"按钮，不是独立菜单

**解决**：

1. 在 `GatewayApplication.main()` 第一行设置 `System.setProperty("csp.sentinel.app.type", "1")`
2. 在"流控规则"页面中找到"新增网关流控"按钮

### 问题3：网关流控规则配置了但不生效（核心问题）

**现象**：在 Dashboard 中添加了网关流控规则（Route ID + QPS=2），但所有请求都正常通过，不被限流

**排查过程**：

1. 通过 actuator 确认 `SentinelGatewayFilter` 已注册为 GlobalFilter ✅
2. 通过诊断 Filter 确认 route ID 匹配正确（`yunh-service-user`）✅
3. 通过诊断 Filter 确认 `GatewayFlowRule` 已加载（2条规则）✅
4. 直接调用 `SphU.entry()` 手动限流 → 仍然不限流 ❌

**根因**：Java SPI 加载冲突。

`sentinel-core-1.7.1.jar` 和 `sentinel-api-gateway-adapter-common-1.7.1.jar` 都注册了 `SlotChainBuilder` 的 SPI 实现。Java SPI 机制只加载第一个实现，当 `DefaultSlotChainBuilder`（不含 `GatewayFlowSlot`）被优先加载时，`GatewayFlowRule` 无法被任何 Slot 处理。

**解决**：在项目 `src/main/resources/META-INF/services/com.alibaba.csp.sentinel.slotchain.SlotChainBuilder` 中显式指定：

```
com.alibaba.csp.sentinel.adapter.gateway.common.slot.GatewaySlotChainBuilder
```

项目的 SPI 文件优先于依赖 jar 的 SPI 文件，确保 `GatewaySlotChainBuilder` 始终被加载。

### 问题4：Sentinel Dashboard 端口 8080 被占用

**解决**：改用 `--server.port=8088`，并同步修改所有服务的 `bootstrap.yml` 中 `sentinel.transport.dashboard` 为 `8088`
