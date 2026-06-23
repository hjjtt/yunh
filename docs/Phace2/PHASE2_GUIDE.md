# 📖 第二阶段：服务注册与发现（Nacos）

## 🎯 学习目标

1. 理解服务注册与发现的概念
2. 掌握 Nacos Server 的安装与配置
3. 将服务注册到 Nacos
4. 实现服务间互相调用
5. 理解 Ribbon 负载均衡

**预计时间：** 6-8 小时

***

## 📚 核心概念

### 什么是服务注册与发现？

在微服务架构中，服务数量众多，服务之间需要互相调用。传统方式是硬编码 IP 和端口，但这样存在以下问题：

- 服务地址变化需要修改代码
- 无法动态扩缩容
- 无法感知服务健康状态

**服务注册与发现** 解决了这些问题：

```
┌─────────────┐     注册     ┌─────────────┐
│  User 服务   │───────────→│   Nacos     │
│ 192.168.1.10│             │ 注册中心     │
└─────────────┘             │             │
                            │  服务列表：   │
┌─────────────┐     注册     │ - User 服务  │
│ Course 服务  │───────────→│ - Course 服务│
│ 192.168.1.20│             │ - Order 服务 │
└─────────────┘             └─────────────┘
                                   ↑
                                   │ 查询
                                   │
                            ┌─────────────┐
                            │  Gateway    │
                            │  (服务调用方)│
                            └─────────────┘
```

### Nacos 是什么？

Nacos = **Naming** + **Configuration** Service

- **注册中心**：服务注册与发现
- **配置中心**：集中管理配置

Spring Cloud Alibaba 的核心组件之一。

***

## 🔧 第一步：安装 Nacos Server

### 方式 1：直接下载（推荐学习使用）

**1. 下载 Nacos**

访问：<https://github.com/alibaba/nacos/releases>

下载 `nacos-server-1.7.0.zip`（与 Spring Cloud Alibaba 2.2.1.RELEASE 匹配）

**2. 解压到本地**

```
D:\env\nacos\
```

**3. 单机模式启动**

```bash
cd D:\env\nacos\nacos\bin
startup.cmd -m standalone
```

**4. 访问控制台**

浏览器打开：`http://localhost:8848/nacos`

默认账号密码：`nacos / nacos`

### 方式 2：Docker 启动（如果你有 Docker）

```bash
docker run --name nacos -e MODE=standalone -p 8848:8848 nacos/nacos-server:1.7.0
```

### 方式 3：源码编译（不推荐新手）

***

## 📝 第二步：配置 User 服务注册到 Nacos

### 1. 添加 Nacos 依赖

`yunh-service-common/pom.xml` 已添加：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### 2. 修改 User 服务配置

编辑 `yunh-service-user/src/main/resources/application.yml`：

```yaml
server:
  port: 9001

spring:
  application:
    name: yunh-service-user  # 服务名称
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos 地址
        namespace: public  # 命名空间
  datasource:
    # ... 数据库配置保持不变
```

### 3. 启用服务发现注解

编辑 `UserServiceApplication.java`：

```java
@SpringBootApplication
@EnableDiscoveryClient  // 添加这个注解
@MapperScan("com.yunh.user.mapper")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

***

## 📝 第三步：配置 Course 服务

同样修改 Course 服务：

**`yunh-service-course/src/main/resources/application.yml`：**

```yaml
server:
  port: 9002

spring:
  application:
    name: yunh-service-course
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
  datasource:
    # ... 数据库配置
```

**`CourseServiceApplication.java`：**

```java
@SpringBootApplication
@EnableDiscoveryClient  // 添加这个注解
@MapperScan("com.yunh.course.mapper")
public class CourseServiceApplication {
    // ...
}
```

***

## 🔗 第四步：服务间调用（Feign）

场景：Course 服务需要调用 User 服务获取讲师信息。

### 1. 在 Course 服务中添加 Feign 依赖

编辑 `yunh-service-course/pom.xml`：

```xml
<dependencies>
    <!-- 已有依赖 -->
    
    <!-- OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
</dependencies>
```

### 2. 创建 Feign Client

在 Course 服务中创建：

`yunh-service-course/src/main/java/com/yunh/course/feign/UserFeignClient.java`

```java
package com.yunh.course.feign;

import com.yunh.common.result.Result;
import com.yunh.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * 
 * @FeignClient 注解说明：
 * - name: 服务名称（从 Nacos 获取）
 * - fallback: 降级处理类（可选）
 */
@FeignClient(name = "yunh-service-user")
public interface UserFeignClient {
    
    /**
     * 根据 ID 查询用户
     * 调用的是 User 服务的 GET /user/{id} 接口
     */
    @GetMapping("/user/{id}")
    Result<User> getById(@PathVariable("id") Long id);
}
```

### 3. 在 CourseServiceApplication 启用 Feign

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // 添加这个注解
@MapperScan("com.yunh.course.mapper")
public class CourseServiceApplication {
    // ...
}
```

### 4. 在 Controller 中使用 Feign Client

编辑 `CourseController.java`：

```java
@RestController
@RequestMapping("/course")
public class CourseController {
    
    @Autowired
    private UserFeignClient userFeignClient;
    
    /**
     * 查询课程详情（包含讲师信息）
     * GET /course/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long id) {
        // 1. 查询课程信息
        Course course = COURSE_LIST.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (course == null) {
            return Result.error("课程不存在");
        }
        
        // 2. 通过 Feign 调用 User 服务获取讲师信息
        Result<User> userResult = userFeignClient.getById(course.getTeacherId());
        
        // 3. 组装返回数据
        CourseDetailVO detailVO = new CourseDetailVO();
        detailVO.setCourse(course);
        if (userResult.isSuccess() && userResult.getData() != null) {
            detailVO.setTeacher(userResult.getData());
        }
        
        return Result.success(detailVO);
    }
}
```

### 5. 创建 VO 类

`yunh-pojo-course/src/main/java/com/yunh/course/pojo/CourseDetailVO.java`

```java
package com.yunh.course.pojo;

import com.yunh.user.pojo.User;
import lombok.Data;
import java.io.Serializable;

/**
 * 课程详情 VO（包含讲师信息）
 */
@Data
public class CourseDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程信息
     */
    private Course course;
    
    /**
     * 讲师信息
     */
    private User teacher;
}
```

***

## ⚖️ 第五步：理解负载均衡

Ribbon 是 Spring Cloud 的负载均衡器。

### 测试负载均衡

**1. 启动两个 User 服务实例**

- 启动第一个：端口 9001
- 启动第二个：修改配置端口为 9011

**2. 观察 Nacos 控制台**

在 `http://localhost:8848/nacos` 可以看到两个 User 服务实例。

**3. 多次调用 Course 服务**

每次调用都会轮询到不同的 User 服务实例。

***

## 🧪 动手实践

### 任务 1：安装并启动 Nacos

```bash
cd D:\env\nacos\nacos\bin
startup.cmd -m standalone
```

访问：`http://localhost:8848/nacos`

### 任务 2：配置 User 服务

1. 修改 `application.yml` 添加 Nacos 配置
2. 添加 `@EnableDiscoveryClient` 注解
3. 启动服务

### 任务 3：验证服务注册

在 Nacos 控制台 → 服务管理 → 服务列表

应该看到 `yunh-service-user`

### 任务 4：配置 Course 服务

同样配置并启动，验证注册。

### 任务 5：实现 Feign 调用

1. 添加 Feign 依赖
2. 创建 Feign Client 接口
3. 添加 `@EnableFeignClients` 注解
4. 在 Controller 中调用

### 任务 6：测试接口

```
http://localhost:9002/course/1/detail
```

应该返回课程 + 讲师信息。

***

## 📊 项目结构变化

```
yunh-service-course/
├── src/main/java/com/yunh/course/
│   ├── CourseServiceApplication.java  (添加 @EnableFeignClients)
│   ├── controller/
│   │   └── CourseController.java      (添加 Feign 调用)
│   ├── feign/                         (新增)
│   │   └── UserFeignClient.java
│   └── pojo/
│       └── CourseDetailVO.java        (新增)
└── pom.xml                            (添加 Feign 依赖)
```

***

## 🤔 思考题

1. 服务注册到 Nacos 后，Nacos 如何知道服务是否健康？
2. `@EnableDiscoveryClient` 和 `@EnableEurekaServer` 有什么区别？
3. Feign 底层是如何实现服务调用的？
4. Ribbon 的负载均衡策略有哪些？
5. 如果 User 服务挂了，Course 服务会怎样？如何解决？

***

## 📚 常见问题

### Q1: Nacos 启动失败？

A: 检查 JDK 版本（需要 1.8+），确保 8848 端口未被占用。

### Q2: 服务注册不上？

A: 检查 `spring.application.name` 和 `server-addr` 配置。

### Q3: Feign 调用报错 "no instances available"？

A: 确保被调用的服务已启动并注册到 Nacos。

### Q4: 如何查看 Nacos 日志？

A: `nacos/logs/start.out` 和 `nacos/logs/nacos.log`

***

## ✅ 完成检查清单

- [x] Nacos Server 成功启动
- [x] 能访问 Nacos 控制台
- [x] User 服务注册到 Nacos
- [x] Course 服务注册到 Nacos
- [x] Feign Client 创建成功
- [x] 能通过 Course 服务调用 User 服务
- [x] 理解服务注册与发现的原理
- [x] 能回答思考题

***

**下一步：** 完成所有实践后，进入第三阶段 - 配置中心（Nacos Config）
