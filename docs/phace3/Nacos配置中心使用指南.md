# Nacos 配置中心使用指南

## 一、概述

本文档说明如何将微服务配置导入 Nacos 配置中心，实现配置的集中管理和动态更新。

## 二、前置条件

### 2.1 环境要求

| 组件    | 版本  | 说明            |
| ----- | --- | ------------- |
| Nacos | 2.x | 配置中心 & 服务注册中心 |
| JDK   | 11+ | 项目运行环境        |
| Maven | 3.x | 项目构建工具        |

### 2.2 启动 Nacos

```bash
# Windows
cmd.exe /c startup.cmd -m standalone

# Linux / Mac
sh startup.sh -m standalone
```

Nacos 控制台地址：`http://127.0.0.1:8848/nacos`
默认用户名/密码：`nacos / 1234`

***

## 三、导入配置到 Nacos

### 3.1 配置文件说明

项目 `nacos-config/DEFAULT_GROUP/` 目录下包含 4 个服务的配置文件：

| 配置文件                   | 对应服务 | 端口   |
| ---------------------- | ---- | ---- |
| `business-service.yml` | 采购服务 | 8084 |
| `storage-service.yml`  | 库存服务 | 8081 |
| `order-service.yml`    | 订单服务 | 8082 |
| `account-service.yml`  | 账户服务 | 8083 |

### 3.2 导入方式一：Nacos 控制台导入（不推荐）

进入 **配置管理 → 配置列表 → 导入配置**，上传 `nacos-config/DEFAULT_GROUP.zip` 文件。

> 注意：此方式可能因 zip 打包格式或 Nacos 版本兼容性问题导致导入失败。

### 3.3 导入方式二：Nacos Open API（推荐）

使用 Nacos 提供的 HTTP API 逐个发布配置，稳定性更高。

#### PowerShell 脚本

创建 `import-nacos.ps1`：

```powershell
$nacosAddr = "http://127.0.0.1:8848"
$group = "DEFAULT_GROUP"
$configDir = "D:\java\springseata\nacos-config\DEFAULT_GROUP"
$files = @("business-service.yml", "storage-service.yml", "order-service.yml", "account-service.yml")

Add-Type -AssemblyName System.Web

foreach ($file in $files) {
    $dataId = $file
    $content = Get-Content "$configDir\$file" -Raw -Encoding UTF8
    $encoded = [System.Web.HttpUtility]::UrlEncode($content)
    $uri = "$nacosAddr/nacos/v1/cs/configs?dataId=$dataId&group=$group&type=yaml&content=$encoded"

    try {
        $resp = Invoke-RestMethod -Uri $uri -Method POST
        if ($resp -eq "true") {
            Write-Host "[OK] $file" -ForegroundColor Green
        } else {
            Write-Host "[FAIL] $file : $resp" -ForegroundColor Red
        }
    } catch {
        Write-Host "[ERROR] $file : $_" -ForegroundColor Red
    }
}
Write-Host "Done!"
```

#### 执行脚本

```bash
powershell -ExecutionPolicy Bypass -File D:\java\springseata\import-nacos.ps1
```

#### curl 方式（备选）

```bash
cd D:\java\springseata\nacos-config\DEFAULT_GROUP

curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs" \
  -d "dataId=business-service.yml" \
  -d "group=DEFAULT_GROUP" \
  -d "type=yaml" \
  -d "content=$(cat business-service.yml)"

# 其他服务配置同理...
```

### 3.4 验证导入结果

#### API 查询

```bash
curl "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=business-service.yml&group=DEFAULT_GROUP"
```

返回配置文件内容则表示导入成功。

#### 控制台查看

进入 **配置管理 → 配置列表**，筛选 Group = `DEFAULT_GROUP`，确认 4 个配置文件均已存在。

***

## 四、服务读取 Nacos 配置

配置导入后，**各服务需要在本地添加** **`bootstrap.yml`** **才能读取 Nacos 配置中心的配置**。

### 4.1 添加 Nacos Config 依赖

在 `pom.xml` 中添加（已添加 `spring-cloud-starter-alibaba-nacos-discovery` 的基础上）：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 4.2 创建 bootstrap.yml

在 `src/main/resources/` 下创建 `bootstrap.yml`：

#### business-service

```yaml
spring:
  application:
    name: business-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
        group: DEFAULT_GROUP
```

#### storage-service

```yaml
spring:
  application:
    name: storage-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
        group: DEFAULT_GROUP
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/seata_storage?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password_here
```

#### order-service

```yaml
spring:
  application:
    name: order-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
        group: DEFAULT_GROUP
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/seata_order?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password_here
```

#### account-service

```yaml
spring:
  application:
    name: account-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
        group: DEFAULT_GROUP
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/seata_account?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password_here
```

> **注意**：`bootstrap.yml` 的加载优先级高于 `application.yml`，因此数据库等敏感配置建议同时保留在本地 `application.yml` 中，或使用 Nacos 的加密配置功能。

***

## 五、服务注册到 Nacos

项目已配置好 Nacos 服务发现，各服务会自动注册。

### 5.1 验证服务注册

启动各服务后，访问 Nacos 控制台 **服务管理 → 服务列表**，应看到：

| 服务名              | 分组             | 实例数 |
| ---------------- | -------------- | --- |
| business-service | DEFAULT\_GROUP | 1   |
| storage-service  | DEFAULT\_GROUP | 1   |
| order-service    | DEFAULT\_GROUP | 1   |
| account-service  | DEFAULT\_GROUP | 1   |

### 5.2 启动顺序

1. **Nacos**（8848）
2. **Seata Server**（8091）- 分布式事务协调者
3. **MySQL**（3306）- 数据库
4. **storage-service**（8081）
5. **account-service**（8083）
6. **order-service**（8082）
7. **business-service**（8084）

***

## 六、常见问题

### Q1: 导入配置时提示"未读取到合法数据"

- 检查 zip 文件结构，确保压缩包内直接是配置文件，不包含嵌套目录
- 建议使用 API 方式导入，避免压缩格式问题

### Q2: 服务启动后无法读取 Nacos 配置

- 确认已添加 `spring-cloud-starter-alibaba-nacos-config` 依赖
- 确认 `bootstrap.yml` 文件存在且配置正确
- 检查 `Data ID` 和 `Group` 是否与配置文件名完全匹配

### Q3: Nacos 连接失败

- 确认 Nacos 已启动：`curl http://127.0.0.1:8848/nacos/v1/console/health/readiness`
- 检查防火墙是否放行 8848 端口

### Q4: 如何在 Nacos 中修改配置后生效

- Nacos 支持**热更新**：配置修改后，服务会自动感知并重新加载配置（部分配置需重启生效）
- 如需强制刷新：调用服务 actuator 端点 `POST /actuator/refresh`

***

## 七、相关文档

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba Nacos Config](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config)
- 项目启动测试接口：`POST http://localhost:8084/business/purchase?userId=1&productId=1&count=2`

