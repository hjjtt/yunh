# RocketMQ 启动指南（云课堂项目）

## 一、环境信息

| 项目 | 说明 |
|------|------|
| RocketMQ 版本 | 4.9.4 |
| 安装目录 | `d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release` |
| rocketmq-spring-boot-starter | 2.2.3 |
| rocketmq-client | 5.0.0（starter 传递依赖） |
| JDK | 20 / 21 |
| 操作系统 | Windows 11 |

---

## 二、RocketMQ 架构说明

```
┌─────────────────────────────────────────────────────────┐
│                    云课堂微服务                           │
│                                                         │
│  yunh-service-user (端口 9001)                          │
│    ├── MqMessageService（生产者，发送消息）                │
│    ├── SmsNotificationConsumer（消费者，短信通知）         │
│    ├── EmailNotificationConsumer（消费者，邮件通知）       │
│    └── LogConsumer（消费者，日志记录）                     │
└───────────────────────┬─────────────────────────────────┘
                        │ 发送/消费消息
                        ▼
┌─────────────────────────────────────────────────────────┐
│                  RocketMQ 集群                           │
│                                                         │
│  NameServer（端口 9876）   ← 路由注册中心，管理 Topic 路由 │
│       │                                                 │
│  Broker（端口 10911）      ← 消息存储和转发               │
│    ├── broker-a（Master）                                │
│    ├── Topic: course_order_topic                        │
│    │     └── Tag: order_create / order_pay / order_cancel│
│    └── 自动创建 Topic: autoCreateTopicEnable = true      │
└─────────────────────────────────────────────────────────┘
```

### 核心概念

| 概念 | 说明 | 本项目中的值 |
|------|------|-------------|
| **NameServer** | 路由注册中心，类似注册中心 | `127.0.0.1:9876` |
| **Broker** | 消息服务器，负责存储和转发 | `broker-a`，端口 `10911` |
| **Topic** | 消息第一级分类 | `course_order_topic`（选课订单） |
| **Tag** | 消息第二级分类 | `order_create`（创建）/ `order_pay`（支付）/ `order_cancel`（取消） |
| **Producer Group** | 生产者组 | yunh-service-user 中的 `RocketMQTemplate` |
| **Consumer Group** | 消费者组 | `yunh-sms-consumer-group` / `yunh-email-consumer-group` / `yunh-log-consumer-group` |

### 消费者组说明

```
一条消息发布到 course_order_topic（Tag: order_create）
    │
    ├── yunh-sms-consumer-group     → 短信消费者  → 发短信通知用户
    ├── yunh-email-consumer-group   → 邮件消费者  → 发邮件通知用户
    └── yunh-log-consumer-group     → 日志消费者  → 记录操作日志

规则：组间广播（每个组都收到），组内负载均衡（同组只有一个消费者收到）
```

---

## 三、启动前准备

### 3.1 确认 JDK 已安装

```powershell
java -version
# 应显示 JDK 20 或 21
```

### 3.2 确认 JAVA_HOME 已设置

```powershell
echo $env:JAVA_HOME
# 应输出类似：C:\Program Files\Java\jdk-21
```

> 如果没有设置，需要在系统环境变量中添加 JAVA_HOME，指向 JDK 安装目录。

### 3.3 确认 ROCKETMQ_HOME

每次打开新的 PowerShell 窗口，都需要设置 ROCKETMQ_HOME：

```powershell
$env:ROCKETMQ_HOME = "d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
```

> 也可以添加到系统环境变量中永久生效。

---

## 四、启动步骤（按顺序执行）

### ⚠️ 重要：必须按以下顺序启动，缺一不可

```
启动顺序：
1. Nacos（配置中心 + 注册中心）
2. Seata Server（分布式事务）
3. RocketMQ NameServer（消息路由）
4. RocketMQ Broker（消息存储）
5. yunh-service-course（课程服务）
6. yunh-service-user（用户服务，包含 MQ 生产者和消费者）
```

### 步骤 1：启动 Nacos

```powershell
# 打开新的 PowerShell 窗口
cd d:\vis\yunh\config\nacos-server-3.1.1\bin
.\startup.cmd -m standalone
```

验证：浏览器访问 http://localhost:8848/nacos ，账号密码 `nacos / 123456`

### 步骤 2：启动 Seata Server

```powershell
# 打开新的 PowerShell 窗口
cd d:\vis\yunh\config\seata-server-1.4.2\bin
.\seata-server.bat
```

验证：端口 8091 正在监听

### 步骤 3：启动 RocketMQ NameServer

```powershell
# 打开新的 PowerShell 窗口
$env:ROCKETMQ_HOME = "d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
cd d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\bin
.\mqnamesrv.cmd
```

成功标志：控制台输出
```
The Name Server boot success. serializeType=JSON
```

NameServer 监听端口：**9876**

### 步骤 4：启动 RocketMQ Broker

```powershell
# 打开新的 PowerShell 窗口
$env:ROCKETMQ_HOME = "d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
cd d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\bin
.\mqbroker.cmd -c d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\conf\broker.conf
```

> ⚠️ **必须加 `-c` 参数指定 broker.conf**，否则配置不会加载！

成功标志：控制台输出
```
The broker[broker-a, xxx.xxx.xxx.xxx:10911] boot success. serializeType=JSON and name server is 127.0.0.1:9876
```

> ⚠️ 必须看到 `name server is 127.0.0.1:9876`，否则说明配置文件未加载成功。

Broker 监听端口：**10911**

### 步骤 5：启动 Course 服务

在 IDEA 中启动 `yunh-service-course` 主类（端口 9002）

### 步骤 6：启动 User 服务

在 IDEA 中启动 `yunh-service-user` 主类（端口 9001）

启动后观察控制台，应能看到消费者组注册成功的日志。

---

## 五、broker.conf 配置说明

配置文件路径：`d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\conf\broker.conf`

```properties
# 集群名称
brokerClusterName = DefaultCluster

# Broker 名称
brokerName = broker-a

# Broker ID（0 表示 Master）
brokerId = 0

# 删除过期文件的时间点（凌晨4点）
deleteWhen = 04

# 文件保留时间（48小时）
fileReservedTime = 48

# Broker 角色：ASYNC_MASTER = 异步复制 Master
brokerRole = ASYNC_MASTER

# 刷盘方式：ASYNC_FLUSH = 异步刷盘
flushDiskType = ASYNC_FLUSH

# 允许自动创建 Topic（开发环境必备）
autoCreateTopicEnable = true

# Broker 监听端口
listenPort = 10911

# NameServer 地址（必须配置，否则 Broker 不知道往哪注册）
namesrvAddr = 127.0.0.1:9876

# 存储路径
storePathRootDir = d:/vis/yunh/config/rocketmq-all-4.9.4-bin-release/store
storePathCommitLog = d:/vis/yunh/config/rocketmq-all-4.9.4-bin-release/store/commitlog
```

---

## 六、Nacos 中的 MQ 配置

在 Nacos 配置中心，`yunh-service-user.yml`（DEFAULT_GROUP）中需要有以下配置：

```yaml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: yunh-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

查看方式：登录 Nacos 控制台 → 配置管理 → 配置列表 → 找到 `yunh-service-user.yml`

---

## 七、验证 RocketMQ 是否正常

### 7.1 检查 NameServer 和 Broker 进程

```powershell
# 检查 NameServer（端口 9876）
netstat -ano | findstr "9876"

# 检查 Broker（端口 10911）
netstat -ano | findstr "10911"
```

两个端口都在监听才算正常。

### 7.2 检查 Topic 列表

```powershell
$env:ROCKETMQ_HOME = "d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
cd d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\bin
.\mqadmin.cmd topicList -n 127.0.0.1:9876
```

正常输出应包含：
```
course_order_topic
```

### 7.3 测试发送消息（选课接口）

```powershell
# 用户1 选课程4（Docker 容器化实战）
Invoke-RestMethod -Uri "http://localhost:9001/user/course/select" -Method Post -ContentType "application/json" -Body '{"userId":1,"courseId":4,"payType":1}'
```

成功返回：
```json
{"code":200,"message":"操作成功","data":null}
```

### 7.4 观察消费日志

在 yunh-service-user 的控制台中，应能看到以下日志：

```
同步发送消息成功：topic=course_order_topic, tag=order_create, messageId=xxx

SMS消费者:  收到短信通知消息：orderId=xx → 短信发送成功 ✅
Email消费者: 收到邮件通知消息：orderId=xx → 邮件发送成功 ✅
Log消费者:  收到日志记录消息：orderId=xx → 操作日志记录成功 ✅
```

---

## 八、完整消息流程图

```
用户调用选课接口
POST /user/course/select  {userId, courseId, payType}
         │
         ▼
┌─────────────────────────────────────────────┐
│  UserCourseService.selectCourse()           │
│  @GlobalTransactional (Seata 分布式事务)     │
│                                             │
│  ① INSERT t_user_course（本地事务）          │
│  ② Feign → Course 服务扣减库存              │
│  ③ MqMessageService.sendCourseOrderSuccess()│
│     └── 同步发送到 RocketMQ                  │
│         Topic: course_order_topic           │
│         Tag:   order_create                 │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
              ┌────────────────┐
              │  RocketMQ      │
              │  Broker        │
              │  (端口 10911)  │
              └───────┬────────┘
                      │ 广播给所有消费者组
          ┌───────────┼───────────┐
          ▼           ▼           ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │ SMS      │ │ Email    │ │ Log      │
    │ 消费者    │ │ 消费者    │ │ 消费者    │
    │          │ │          │ │          │
    │ 发短信    │ │ 发邮件    │ │ 记日志    │
    └──────────┘ └──────────┘ └──────────┘
```

---

## 九、关闭 RocketMQ

### 关闭顺序

```
1. 先关闭微服务（IDEA 中停止）
2. 关闭 Broker 窗口（Ctrl + C）
3. 关闭 NameServer 窗口（Ctrl + C）
```

### 强制关闭（如果 Ctrl + C 无效）

```powershell
# 查找 Broker 进程
netstat -ano | findstr "10911"
# 根据输出的 PID 杀进程
taskkill /PID <PID号> /F

# 查找 NameServer 进程
netstat -ano | findstr "9876"
taskkill /PID <PID号> /F
```

---

## 十、常见问题排查

### 问题 1：No route info of this topic

```
org.apache.rocketmq.client.exception.MQClientException: No route info of this topic: course_order_topic
```

**原因**：Broker 未正确加载 broker.conf，导致没有连接到 NameServer，Topic 无法注册。

**排查步骤**：

```powershell
# 1. 检查 Broker 进程的启动命令是否包含 -c 参数
# 先找到 Broker 的 PID
netstat -ano | findstr "10911"
# 查看完整命令行
(Get-CimInstance Win32_Process -Filter "ProcessId = <PID>").CommandLine
```

如果命令行中没有 `-c` 参数，说明 Broker 启动时没有加载配置文件。

**解决**：重启 Broker，必须使用：
```powershell
.\mqbroker.cmd -c d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release\conf\broker.conf
```

### 问题 2：Please set the ROCKETMQ_HOME variable

**解决**：
```powershell
$env:ROCKETMQ_HOME = "d:\vis\yunh\config\rocketmq-all-4.9.4-bin-release"
```

### 问题 3：找不到主类（CLASSPATH 空格问题）

```
错误: 找不到或无法加载主类 Files\Java\jdk-21.0.10\lib\dt.jar
```

**原因**：系统 CLASSPATH 包含空格路径（如 `C:\Program Files\...`），RocketMQ 脚本未加引号。

**解决**：本项目已修改 `runserver.cmd` 和 `runbroker.cmd`，不再继承系统 CLASSPATH，并且 `-cp` 参数加了引号。如果重新下载 RocketMQ，需要重新修改这两个文件。

### 问题 4：Unrecognized VM option 'UseConcMarkSweepGC'

**原因**：JDK 15+ 已移除 CMS 垃圾回收器。

**解决**：本项目已将 `runserver.cmd` 和 `runbroker.cmd` 中的 CMS 改为 G1GC，并移除了不兼容参数。如果重新下载 RocketMQ，需要重新修改。

### 问题 5：Duplicate entry 'x-x' for key 'uk_user_course'

**原因**：同一用户重复选同一门课，触犯唯一索引。

**解决**：
```sql
-- 查看当前选课记录
SELECT * FROM yunh_user.t_user_course;

-- 清除测试数据（按需）
DELETE FROM yunh_user.t_user_course WHERE user_id = 1 AND course_id = 1;
```

---

## 十一、已修改的 RocketMQ 脚本文件清单

| 文件 | 修改内容 |
|------|---------|
| `bin/runserver.cmd` | 去掉系统 CLASSPATH 继承；CMS→G1GC；移除不兼容参数；`-cp` 加引号；内存缩减到 1g |
| `bin/runbroker.cmd` | 同上 |
| `conf/broker.conf` | 添加 `autoCreateTopicEnable=true`、`namesrvAddr`、`listenPort`、`storePath*` |

> 如果重新下载 RocketMQ 4.9.4，需要对以上三个文件做相同的修改。
