# 📖 第一阶段：基础搭建

## ✅ 完成状态

- [x] 创建父工程 pom.xml
- [x] 创建基础模块 (yunh-basic)
- [x] 创建 POJO 模块 (yunh-pojo)
- [x] 创建 API 模块 (yunh-api)
- [x] 创建服务模块 (yunh-service)
- [x] 创建支持模块 (yunh-support)
- [x] 创建通用工具类 (Result, BusinessException)
- [x] 创建 User 服务基础结构
- [x] 创建 Course 服务基础结构

---

## 📁 项目结构

```
yunh-cloud/
├── pom.xml                          # 父工程 - 统一依赖管理
├── yunh-basic/                      # 基础模块
│   ├── yunh-basic-common/           # 通用工具类 (Result, 异常等)
│   └── yunh-basic-dependency/       # 公共依赖封装
├── yunh-pojo/                       # 数据模型层
│   ├── yunh-pojo-common/
│   ├── yunh-pojo-user/              # 用户实体
│   └── yunh-pojo-course/            # 课程实体
├── yunh-api/                        # API 接口层 (Feign 调用用)
│   ├── yunh-api-user/
│   └── yunh-api-course/
├── yunh-service/                    # 服务层
│   ├── yunh-service-common/
│   ├── yunh-service-user/           # 用户服务 (端口 9001)
│   └── yunh-service-course/         # 课程服务 (端口 9002)
└── yunh-support/                    # 基础设施
    └── yunh-service-gateway/        # 网关服务
```

---

## 🎯 学习目标

1. **理解 Maven 多模块项目结构**
   - 父工程如何管理子模块
   - 模块间依赖关系
   - 版本统一管理

2. **理解分层架构**
   - POJO 层：存放实体类
   - API 层：存放接口定义（为 Feign 准备）
   - Service 层：具体业务实现

3. **掌握 Spring Boot 基础**
   - @SpringBootApplication
   - @RestController
   - @RequestMapping
   - 配置文件 application.yml

---

## 📝 知识点详解

### 1. Maven 多模块

**父工程 pom.xml 关键配置：**

```xml
<!-- 打包方式为 pom，表示只做管理，不写代码 -->
<packaging>pom</packaging>

<!-- 声明所有子模块 -->
<modules>
    <module>yunh-basic</module>
    <module>yunh-pojo</module>
    ...
</modules>

<!-- 统一版本管理 -->
<dependencyManagement>
    <dependencies>
        <!-- 这里定义的版本，子模块引用时不用写版本号 -->
    </dependencies>
</dependencyManagement>
```

### 2. 统一响应封装

**Result<T> 类的作用：**
- 统一前后端交互格式
- 包含 code（状态码）、message（消息）、data（数据）
- 提供静态方法快速构建响应

**使用示例：**
```java
// 成功响应（有数据）
return Result.success(userList);

// 成功响应（无数据）
return Result.success();

// 失败响应
return Result.error("用户不存在");
```

### 3. 全局异常处理

**@RestControllerAdvice 注解：**
- 全局拦截 Controller 层异常
- 统一处理，避免每个方法都 try-catch
- 返回统一格式的 error 响应

---

## 🔧 环境准备

### 1. 检查 JDK
```bash
java -version
# 应该显示 1.8.x 或更高版本
```

### 2. 检查 Maven
```bash
mvn -version
# 应该显示 Maven 3.6+
```

### 3. 准备 MySQL
```sql
-- 创建用户数据库
CREATE DATABASE yunh_user DEFAULT CHARACTER SET utf8mb4;

-- 创建课程数据库
CREATE DATABASE yunh_course DEFAULT CHARACTER SET utf8mb4;

-- 创建用户表
USE yunh_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO t_user (username, password, nickname, email, phone, status) VALUES
('zhangsan', '123456', '张三', 'zhangsan@example.com', '13800138001', 1),
('lisi', '123456', '李四', 'lisi@example.com', '13800138002', 1);
```

---

## 🚀 动手实践

### 任务 1：编译项目

在项目根目录执行：
```bash
cd D:\vis\yunh
mvn clean install
```

**预期结果：** 看到 `BUILD SUCCESS`

### 任务 2：启动 User 服务

1. 打开 IDEA，导入项目 `D:\vis\yunh\pom.xml`
2. 找到 `UserServiceApplication.java`
3. 右键 → Run
4. 观察控制台输出

**预期结果：** 看到 `Started UserServiceApplication`

### 任务 3：测试接口

使用浏览器或 Postman 访问：
```
http://localhost:9001/user/list
http://localhost:9001/user/1
http://localhost:9001/user/health
```

**预期响应：**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [...],
    "timestamp": 1234567890
}
```

### 任务 4：启动 Course 服务

同样方式启动 Course 服务，访问：
```
http://localhost:9002/course/list
http://localhost:9002/course/health
```

---

## 🤔 思考题

1. 为什么要把 POJO 单独拆分成一个模块？
2. `@RestController` 和 `@Controller` 有什么区别？
3. 为什么要统一响应格式？直接在 Controller 返回对象不行吗？
4. 父工程的 `<dependencyManagement>` 和 `<dependencies>` 有什么区别？

---

## 📚 扩展阅读

- [Maven 官方文档 - 多模块项目](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 官方文档](https://baomidou.com/)

---

## ✅ 完成检查清单

- [ ] 项目能在 IDEA 中成功导入
- [ ] `mvn clean install` 能成功构建
- [ ] User 服务能成功启动
- [ ] Course 服务能成功启动
- [ ] 能通过浏览器访问两个服务的接口
- [ ] 理解每个模块的作用
- [ ] 能回答上面的思考题

---

**下一步：** 完成第一阶段后，进入第二阶段 - 服务注册与发现（Nacos）
