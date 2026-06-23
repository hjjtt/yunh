# 📋 Nacos Config 配置示例

## 🎯 本文件内容

将以下配置创建到 Nacos 控制台，用于第三阶段实践。

***

## 📝 配置 1：User 服务配置

### 登录 Nacos 控制台

访问：`http://localhost:8848/nacos`

- 用户名：`nacos`
- 密码：`123456`

### 创建配置

**配置管理 → 配置列表 → +**

| 字段          | 值                        |
| ----------- | ------------------------ |
| **Data ID** | `yunh-service-user.yaml` |
| **Group**   | `DEFAULT_GROUP`          |
| **配置格式**    | `YAML`                   |
| **描述**      | `User 服务配置 - 第三阶段`       |

### 配置内容

```yaml
# 数据源配置
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
    welcome: "欢迎来到用户中心 - 开发环境"
    version: "1.0.0-dev"
```

***

## 📝 配置 2：Course 服务配置

### 创建配置

**配置管理 → 配置列表 → +**

| 字段          | 值                          |
| ----------- | -------------------------- |
| **Data ID** | `yunh-service-course.yaml` |
| **Group**   | `DEFAULT_GROUP`            |
| **配置格式**    | `YAML`                     |
| **描述**      | `Course 服务配置 - 第三阶段`       |

### 配置内容

```yaml
# 数据源配置
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
    welcome: "欢迎来到课程中心 - 开发环境"
    version: "1.0.0-dev"
```

***

## 🧪 测试动态刷新

### 步骤 1：启动服务

启动 User 服务和 Course 服务。

### 步骤 2：访问配置接口

```
http://localhost:9001/user/config
```

预期响应：

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

### 步骤 3：修改 Nacos 配置

1. 在 Nacos 控制台找到 `yunh-service-user.yaml`
2. 点击"编辑"
3. 修改 `welcome` 字段：
   ```yaml
   yunh:
     user:
       welcome: "欢迎修改后的用户中心 - 动态刷新测试"
   ```
4. 点击"发布"

### 步骤 4：再次访问接口

```
http://localhost:9001/user/config
```

预期响应中 `welcome` 字段已更新，**无需重启服务**！

***

## 🌍 多环境配置示例

### dev 环境

**Data ID:** `yunh-service-user-dev.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yunh_user
    username: root
    password: your_password_here

yunh:
  user:
    welcome: "开发环境 - 用户中心"
    version: "1.0.0-dev"
```

### test 环境

**Data ID:** `yunh-service-user-test.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/yunh_user
    username: test_user
    password: test_password

yunh:
  user:
    welcome: "测试环境 - 用户中心"
    version: "1.0.0-test"
```

### prod 环境

**Data ID:** `yunh-service-user-prod.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/yunh_user
    username: prod_user
    password: ${DB_PASSWORD:default}

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
    active: test  # 改为 dev/test/prod
```

***

## 🔧 配置优先级说明

当多个来源都有相同配置时，优先级（高 → 低）：

1. 命令行参数
2. 环境变量
3. Nacos 配置（特定环境，如 `yunh-service-user-dev.yaml`）
4. Nacos 配置（默认，如 `yunh-service-user.yaml`）
5. `application-{profile}.yml`
6. `application.yml`

***

## 📞 遇到问题？

查看 `PHASE3_GUIDE.md` 获取详细学习指南。
查看 `PHASE3_COMPLETE.md` 获取完成检查清单。
