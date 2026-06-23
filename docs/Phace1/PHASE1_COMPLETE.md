# ✅ 第一阶段完成 - 基础搭建

## 🎉 构建成功

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  48.521 s
```

---

## 📦 已完成模块

| 模块 | 说明 | 状态 |
|------|------|------|
| yunh-cloud | 父工程 | ✅ |
| yunh-basic-common | 通用工具类（Result、异常处理） | ✅ |
| yunh-basic-dependency | 公共依赖封装 | ✅ |
| yunh-pojo-common | POJO 公共模块 | ✅ |
| yunh-pojo-user | 用户实体 | ✅ |
| yunh-pojo-course | 课程实体 | ✅ |
| yunh-api-user | 用户 API 接口 | ✅ |
| yunh-api-course | 课程 API 接口 | ✅ |
| yunh-service-common | 服务公共模块 | ✅ |
| yunh-service-user | 用户服务（9001） | ✅ |
| yunh-service-course | 课程服务（9002） | ✅ |
| yunh-service-gateway | 网关服务 | ✅ |

---

## 🚀 下一步操作

### 1. 初始化数据库

```bash
# 方式 1: 命令行
mysql -u root -p < D:\vis\yunh\database\init.sql

# 方式 2: MySQL Workbench / Navicat
# 打开 D:\vis\yunh\database\init.sql 并执行
```

### 2. 修改数据库配置（如果需要）

编辑以下文件，修改 MySQL 用户名和密码：

- `yunh-service-user/src/main/resources/application.yml`
- `yunh-service-course/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

### 3. 启动 User 服务

**方式 1: IDEA（推荐）**
1. 打开 IDEA → Open → 选择 `D:\vis\yunh\pom.xml`
2. 找到 `yunh-service-user/src/main/java/com/yunh/user/UserServiceApplication.java`
3. 右键 → Run 'UserServiceApplication'

**方式 2: Maven 命令**
```bash
cd D:\vis\yunh
mvn spring-boot:run -pl yunh-service/yunh-service-user
```

### 4. 测试接口

浏览器访问：
```
http://localhost:9001/user/list
http://localhost:9001/user/health
```

预期响应：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "username": "zhangsan",
            "nickname": "张三",
            "email": "zhangsan@example.com",
            "phone": "13800138001",
            "status": 1
        }
    ],
    "timestamp": 1234567890
}
```

### 5. 启动 Course 服务

同样方式启动 Course 服务：
```
http://localhost:9002/course/list
http://localhost:9002/course/health
```

---

## 📚 学习要点

### 1. Maven 多模块结构
- 父工程管理所有子模块
- 模块间依赖关系清晰
- 统一版本管理

### 2. 分层架构
```
POJO 层 (实体类)
    ↓
API 层 (接口定义，为 Feign 准备)
    ↓
Service 层 (业务实现)
```

### 3. 统一响应封装
```java
// 成功响应
return Result.success(data);

// 失败响应
return Result.error("错误消息");
```

### 4. 全局异常处理
- `@RestControllerAdvice` 全局拦截
- 统一返回格式

---

## 🤔 思考题

1. 为什么要把 POJO 单独拆分成一个模块？
2. `@RestController` 和 `@Controller` 有什么区别？
3. 为什么要统一响应格式？
4. 父工程的 `<dependencyManagement>` 和 `<dependencies>` 有什么区别？

---

## 📖 下一步：第二阶段 - 服务注册与发现

学习前准备：
- [x] 完成第一阶段所有实践
- [x] 能独立启动两个服务
- [ ] 安装 Nacos Server

第二阶段将学习：
- Nacos 安装与配置
- 服务注册到 Nacos
- 服务间互相调用
- Ribbon 负载均衡

---

**恭喜你完成第一阶段！** 🎓

继续加油！
