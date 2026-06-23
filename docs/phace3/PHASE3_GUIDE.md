# 📖 第三阶段：配置中心（Nacos Config）

## 🎯 学习目标

1. 理解配置中心的作用和必要性
2. 掌握 Nacos Config 的配置方法
3. 实现配置动态刷新（无需重启服务）
4. 掌握多环境配置管理
5. 理解配置优先级

**预计时间：** 4-6 小时

***

## 📚 核心概念

### 为什么需要配置中心？

在微服务架构中，配置管理面临以下挑战：

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  User 服务   │    │ Course 服务  │    │ Order 服务  │
│ application │    │ application │    │ application │
│    .yml     │    │    .yml     │    │    .yml     │
└─────────────┘    └─────────────┘    └─────────────┘
       ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────┐
│                    问题：                            │
│  ❌ 配置分散，难以统一管理                           │
│  ❌ 修改配置需要重启服务                            │
│  ❌ 多环境配置容易混淆                              │
│  ❌ 敏感配置（密码）明文存储                        │
└─────────────────────────────────────────────────────┘
```

**配置中心解决这个问题：**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  User 服务   │    │ Course 服务  │    │ Order 服务  │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │
       └──────────────────┼──────────────────┘
                          ↓
                 ┌─────────────────┐
                 │  Nacos Config   │
                 │   配置中心      │
                 │                 │
                 │ - 统一管理      │
                 │ - 动态刷新      │
                 │ - 多环境        │
                 │ - 配置加密      │
                 └─────────────────┘
```

### Nacos Config 模型

```
Data ID = ${spring.application.name}.${file-extension}
        = yunh-service-user.yaml

Group = DEFAULT_GROUP（默认）

配置格式 = yaml / properties
```

***

## 🔧 第一步：添加 Nacos Config 依赖

### 1. 在 yunh-basic-dependency 中添加

编辑 `yunh-basic/yunh-basic-dependency/pom.xml`：

```xml
<dependencies>
    <!-- 已有依赖 -->
    
    <!-- Nacos Config -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
</dependencies>
```

### 2. 确保 yunh-service-common 依赖 yunh-basic-dependency

***

## 📝 第二步：创建 bootstrap.yml

**重要：** Spring Boot 配置加载顺序：

```
bootstrap.yml → application.yml → Nacos 配置
```

`bootstrap.yml` 优先级更高，用于加载 Nacos 配置。

### User 服务 bootstrap.yml

创建 `yunh-service-user/src/main/resources/bootstrap.yml`：

```yaml
spring:
  application:
    name: yunh-service-user
  profiles:
    active: dev  # 激活环境
  cloud:
    nacos:
      config:
        server-addr: localhost:8848  # Nacos 地址
        file-extension: yaml         # 配置文件格式
        group: DEFAULT_GROUP         # 配置分组
        namespace: public            # 命名空间
        prefix: ${spring.application.name}  # 配置前缀
      discovery:
        server-addr: localhost:8848
        namespace: public
```

### Course 服务 bootstrap.yml

创建 `yunh-service-course/src/main/resources/bootstrap.yml`：

```yaml
spring:
  application:
    name: yunh-service-course
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        group: DEFAULT_GROUP
        namespace: public
        prefix: ${spring.application.name}
      discovery:
        server-addr: localhost:8848
        namespace: public
```

***

## 📝 第三步：简化 application.yml

将数据库等配置移到 Nacos，本地 application.yml 只保留必要配置。

### User 服务 application.yml

编辑 `yunh-service-user/src/main/resources/application.yml`：

```yaml
server:
  port: 9001

# 其他配置移到 Nacos Config
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.yunh.user.pojo
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.yunh.user: debug
```

### Course 服务 application.yml

编辑 `yunh-service-course/src/main/resources/application.yml`：

```yaml
server:
  port: 9002

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.yunh.course.pojo
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.yunh.course: debug
```

***

## 📋 第四步：在 Nacos 创建配置

### 1. 登录 Nacos 控制台

访问：`http://localhost:8848/nacos`

### 2. 创建 User 服务配置

**配置管理 → 配置列表 → +**

| 字段      | 值                        |
| ------- | ------------------------ |
| Data ID | `yunh-service-user.yaml` |
| Group   | `DEFAULT_GROUP`          |
| 配置格式    | `YAML`                   |
| 描述      | `User 服务配置`              |

**配置内容：**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yunh_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

# 自定义配置 - 用于测试动态刷新
yunh:
  user:
    welcome: "欢迎来到用户中心"  # 修改这个测试动态刷新
    version: "1.0.0"
```

### 3. 创建 Course 服务配置

同样方式创建 `yunh-service-course.yaml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yunh_course?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

# 自定义配置
yunh:
  course:
    welcome: "欢迎来到课程中心"
    version: "1.0.0"
```

***

## 🔄 第五步：实现配置动态刷新

### 1. 添加配置属性类

**User 服务：** `yunh-service-user/src/main/java/com/yunh/user/config/UserProperties.java`

```java
package com.yunh.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 用户服务配置属性
 * 
 * @RefreshScope 注解：配置变更时自动刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "yunh.user")
public class UserProperties {
    
    /**
     * 欢迎语
     */
    private String welcome;
    
    /**
     * 版本号
     */
    private String version;
}
```

### 2. 在 Controller 中使用

编辑 `UserController.java`：

```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserProperties userProperties;
    
    /**
     * 获取配置信息（支持动态刷新）
     * GET /user/config
     */
    @GetMapping("/config")
    public Result<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("welcome", userProperties.getWelcome());
        config.put("version", userProperties.getVersion());
        return Result.success(config);
    }
    
    // ... 其他方法
}
```

### 3. 测试动态刷新

**步骤：**

1. 启动 User 服务
2. 访问：`http://localhost:9001/user/config`
   - 返回：`{"welcome": "欢迎来到用户中心", "version": "1.0.0"}`
3. 在 Nacos 控制台修改配置：
   - 将 `welcome` 改为 `"欢迎修改后的用户中心"`
   - 点击"发布"
4. 再次访问：`http://localhost:9001/user/config`
   - 返回：`{"welcome": "欢迎修改后的用户中心", "version": "1.0.0"}`

**✅ 无需重启服务，配置已生效！**

***

## 🌍 第六步：多环境配置

### 创建不同环境的配置

在 Nacos 中创建以下配置：

| Data ID                       | 环境   | 用途    |
| ----------------------------- | ---- | ----- |
| `yunh-service-user-dev.yaml`  | 开发环境 | 本地开发  |
| `yunh-service-user-test.yaml` | 测试环境 | 测试服务器 |
| `yunh-service-user-prod.yaml` | 生产环境 | 生产服务器 |

### dev 环境配置

`yunh-service-user-dev.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yunh_user?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password_here

yunh:
  user:
    welcome: "开发环境 - 用户中心"
    version: "1.0.0-dev"
```

### test 环境配置

`yunh-service-user-test.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/yunh_user?useUnicode=true&characterEncoding=utf8
    username: test_user
    password: test_password

yunh:
  user:
    welcome: "测试环境 - 用户中心"
    version: "1.0.0-test"
```

### prod 环境配置

`yunh-service-user-prod.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/yunh_user?useUnicode=true&characterEncoding=utf8
    username: prod_user
    password: ${DB_PASSWORD:default}  # 从环境变量读取

yunh:
  user:
    welcome: "生产环境 - 用户中心"
    version: "1.0.0"
```

### 切换环境

修改 `bootstrap.yml`：

```yaml
spring:
  profiles:
    active: test  # 改为 test 或 prod
```

***

## 📊 配置优先级

当多个来源都有相同配置时，优先级如下（高 → 低）：

```
1. 命令行参数
2. 环境变量
3. Nacos 配置（特定环境）
4. Nacos 配置（默认）
5. application-{profile}.yml
6. application.yml
7. @PropertySource
8. 默认属性
```

***

## 🔐 第七步：配置加密（可选）

### 使用 Nacos 加密插件

**1. 添加加密依赖**

```xml
<dependency>
    <groupId>com.alibaba.nacos</groupId>
    <artifactId>nacos-plugin-encryption</artifactId>
    <version>1.7.0</version>
</dependency>
```

**2. 配置加密**

在 Nacos 配置中使用加密前缀：

```yaml
spring:
  datasource:
    password: ENC(AES加密后的密文)
```

***

## 🧪 动手实践

### 任务 1：添加 Nacos Config 依赖

在 `yunh-basic-dependency/pom.xml` 中添加依赖。

### 任务 2：创建 bootstrap.yml

为 User 和 Course 服务分别创建。

### 任务 3：在 Nacos 创建配置

创建 `yunh-service-user.yaml` 和 `yunh-service-course.yaml`。

### 任务 4：实现动态刷新

1. 创建配置属性类
2. 在 Controller 中暴露配置接口
3. 修改 Nacos 配置，验证动态刷新

### 任务 5：多环境配置

创建 dev/test 环境配置，切换环境测试。

***

## 📊 项目结构变化

```
yunh-service-user/
├── src/main/resources/
│   ├── bootstrap.yml          (新增)
│   └── application.yml        (简化)
├── src/main/java/com/yunh/user/
│   ├── config/
│   │   └── UserProperties.java (新增)
│   └── controller/
│       └── UserController.java (添加配置接口)
└── pom.xml
```

***

## 🤔 思考题

1. 为什么需要 `bootstrap.yml` 而不是直接用 `application.yml`？
2. `@RefreshScope` 的工作原理是什么？
3. 如何保证配置变更的安全性（权限控制）？
4. 配置中心挂了，服务还能启动吗？
5. 如何实现配置变更的审计日志？

***

## 📚 常见问题

### Q1: 配置不生效？

A: 检查 `bootstrap.yml` 是否正确，确保 Nacos Config 依赖已添加。

### Q2: 动态刷新不生效？

A: 确保类上有 `@RefreshScope` 注解。

### Q3: 如何查看配置加载日志？

A: 添加日志配置：`logging.level.com.alibaba.cloud.nacos=DEBUG`

### Q4: 配置优先级混乱？

A: 遵循优先级规则，避免在多处配置相同 key。

***

## ✅ 完成检查清单

- [x] Nacos Config 依赖已添加
- [x] bootstrap.yml 创建完成
- [ ] application.yml 已简化
- [ ] Nacos 中创建了服务配置
- [ ] 服务能成功从 Nacos 加载配置
- [ ] 配置动态刷新验证成功
- [ ] 多环境配置创建完成
- [ ] 能切换环境并验证

***

**下一步：** 完成所有实践后，进入第四阶段 - 服务网关（Gateway）
