# Postman 服务验证测试指南

## 一、前置条件检查

在测试接口之前，必须确认以下服务已启动：

| 依赖服务             | 地址                            | 验证方式                        |
| ---------------- | ----------------------------- | --------------------------- |
| **Nacos**        | `http://localhost:8848/nacos` | 浏览器打开，账号 `nacos` / `123456` |
| **MySQL**        | `localhost:3306`              | `mysql -u root -p123456`    |
| **Sentinel**（可选） | `http://localhost:8098`       | 浏览器打开                       |

先启动各微服务（至少启动 Gateway + 你要测试的服务），然后通过 Nacos 控制台确认服务注册成功。

***

## 二、两种测试方式

- **方式 A**：通过网关（端口 `9000`）统一访问（推荐，验证完整链路）
- **方式 B**：直接访问各微服务端口（用于排查单个服务问题）

***

## 三、服务端口一览

| 服务              | 端口   | 直接地址                    |
| --------------- | ---- | ----------------------- |
| **Gateway**     | 9000 | `http://localhost:9000` |
| **User**        | 9002 | `http://localhost:9002` |
| **Course**      | 9003 | `http://localhost:9003` |
| **Order**       | 9004 | `http://localhost:9004` |
| **Pay**         | 9005 | `http://localhost:9005` |
| **Video**       | 9006 | `http://localhost:9006` |
| **Interaction** | 9007 | `http://localhost:9007` |
| **Search**      | 9008 | `http://localhost:9008` |
| **Statistics**  | 9009 | `http://localhost:9009` |

***

## 四、鉴权说明

通过网关访问时，鉴权过滤器（AuthGlobalFilter）规则如下：

| 路径                        | 是否需要 Token                     |
| ------------------------- | ------------------------------ |
| `/api/user/login`         | 免鉴权                            |
| `/api/user/course/select` | 免鉴权                            |
| `/api/user/course/pay/**` | 免鉴权                            |
| `/actuator/**`            | 免鉴权                            |
| 其他所有接口                    | 需要 `Authorization: Bearer xxx` |

> 注意：当前 JWT 验证标记为 TODO，任意字符串都可以作为 Token 通过，例如 `Bearer test-token-123`。
>
> 直接访问各服务端口时不需要 Token（鉴权只在网关层）。

***

## 五、通过网关测试（方式 A）

网关路由规则：`http://localhost:9000/api/{service}/**` → StripPrefix 后转发到对应服务。

> 当前网关路由只配置了 user 和 course 两个服务，其他服务需使用方式 B 直接访问。

### 5.1 User 服务

#### ① 健康检查（免鉴权）

```
GET http://localhost:9000/api/user/health
```

#### ② 用户列表（需 Token）

```
GET http://localhost:9000/api/user/list
Headers:
  Authorization: Bearer test-token-123
```

#### ③ 根据ID查询用户（需 Token）

```
GET http://localhost:9000/api/user/1
Headers:
  Authorization: Bearer test-token-123
```

#### ④ 根据用户名查询（需 Token）

```
GET http://localhost:9000/api/user/query?username=admin
Headers:
  Authorization: Bearer test-token-123
```

#### ⑤ Nacos 动态配置验证（需 Token）

```
GET http://localhost:9000/api/user/config
Headers:
  Authorization: Bearer test-token-123
```

#### ⑥ 用户课程列表 - Feign 调用（需 Token）

```
GET http://localhost:9000/api/user/1/courses
Headers:
  Authorization: Bearer test-token-123
```

### 5.2 Course 服务

#### ① 健康检查（需 Token）

```
GET http://localhost:9000/api/course/health
Headers:
  Authorization: Bearer test-token-123
```

#### ② 课程列表（需 Token）

```
GET http://localhost:9000/api/course/list
Headers:
  Authorization: Bearer test-token-123
```

#### ③ 根据ID查询课程（需 Token）

```
GET http://localhost:9000/api/course/1
Headers:
  Authorization: Bearer test-token-123
```

#### ④ 课程详情 - 含 Feign 调用（需 Token）

```
GET http://localhost:9000/api/course/1/detail
Headers:
  Authorization: Bearer test-token-123
```

#### ⑤ 根据讲师查询课程（需 Token）

```
GET http://localhost:9000/api/course/teacher/1
Headers:
  Authorization: Bearer test-token-123
```

***

## 六、直接访问各服务端口测试（方式 B）

### 6.1 User 服务（9002）

| 方法   | URL                                               | 说明              |
| ---- | ------------------------------------------------- | --------------- |
| GET  | `http://localhost:9002/user/health`               | 健康检查            |
| GET  | `http://localhost:9002/user/list`                 | 用户列表            |
| GET  | `http://localhost:9002/user/1`                    | 根据ID查询用户        |
| GET  | `http://localhost:9002/user/query?username=admin` | 根据用户名查询         |
| GET  | `http://localhost:9002/user/config`               | Nacos 动态配置      |
| GET  | `http://localhost:9002/user/1/courses`            | 用户课程列表（Feign调用） |
| POST | `http://localhost:9002/user/login`                | 用户登录            |

### 6.2 Course 服务（9003）

| 方法  | URL                                           | 说明                  |
| --- | --------------------------------------------- | ------------------- |
| GET | `http://localhost:9003/course/health`         | 健康检查                |
| GET | `http://localhost:9003/course/list`           | 课程列表                |
| GET | `http://localhost:9003/course/1`              | 根据ID查询课程            |
| GET | `http://localhost:9003/course/1/detail`       | 课程详情（Feign调用User服务） |
| GET | `http://localhost:9003/course/teacher/1`      | 根据讲师ID查询课程          |
| GET | `http://localhost:9003/course/config`         | Nacos 动态配置          |
| PUT | `http://localhost:9003/course/stock/decrease` | 扣减库存                |
| PUT | `http://localhost:9003/course/stock/increase` | 恢复库存                |

### 6.3 Order 服务（9004）

| 方法   | URL                                                      | 说明       |
| ---- | -------------------------------------------------------- | -------- |
| GET  | `http://localhost:9004/order/list`                       | 订单列表     |
| GET  | `http://localhost:9004/order/1`                          | 根据ID查询订单 |
| GET  | `http://localhost:9004/order/no/{orderNo}`               | 根据订单号查询  |
| GET  | `http://localhost:9004/order/user/1`                     | 查询用户订单列表 |
| POST | `http://localhost:9004/order/create?userId=1&courseId=1` | 创建订单     |
| POST | `http://localhost:9004/order/cancel/{orderNo}`           | 取消订单     |
| POST | `http://localhost:9004/order/pay/{orderNo}?payType=1`    | 支付订单     |

### 6.4 Cart 购物车（9004，Order 服务内）

| 方法     | URL                                  | 说明      |
| ------ | ------------------------------------ | ------- |
| GET    | `http://localhost:9004/cart/user/1`  | 查询用户购物车 |
| POST   | `http://localhost:9004/cart/add`     | 添加到购物车  |
| DELETE | `http://localhost:9004/cart/remove`  | 从购物车移除  |
| DELETE | `http://localhost:9004/cart/clear/1` | 清空购物车   |

### 6.5 Pay 服务（9005）

| 方法   | URL                                          | 说明     |
| ---- | -------------------------------------------- | ------ |
| GET  | `http://localhost:9005/pay/list`             | 支付记录列表 |
| POST | `http://localhost:9005/pay/create`           | 创建支付   |
| GET  | `http://localhost:9005/pay/status/{orderNo}` | 查询支付状态 |
| POST | `http://localhost:9005/pay/callback`         | 支付回调   |
| POST | `http://localhost:9005/pay/refund`           | 申请退款   |
| GET  | `http://localhost:9005/pay/refund/{orderNo}` | 查询退款记录 |

### 6.6 Video 服务（9006）

| 方法     | URL                                    | 说明       |
| ------ | -------------------------------------- | -------- |
| GET    | `http://localhost:9006/video/course/1` | 课程视频列表   |
| GET    | `http://localhost:9006/video/1`        | 根据ID查询视频 |
| POST   | `http://localhost:9006/video/upload`   | 上传视频     |
| PUT    | `http://localhost:9006/video/update`   | 更新视频     |
| DELETE | `http://localhost:9006/video/1`        | 删除视频     |
| POST   | `http://localhost:9006/video/play/1`   | 增加播放次数   |

### 6.7 Chapter 章节（9006，Video 服务内）

| 方法     | URL                                      | 说明     |
| ------ | ---------------------------------------- | ------ |
| GET    | `http://localhost:9006/chapter/course/1` | 课程章节列表 |
| POST   | `http://localhost:9006/chapter/create`   | 创建章节   |
| PUT    | `http://localhost:9006/chapter/update`   | 更新章节   |
| DELETE | `http://localhost:9006/chapter/1`        | 删除章节   |

### 6.8 Interaction 互动服务（9007）

| 方法     | URL                                       | 说明     |
| ------ | ----------------------------------------- | ------ |
| GET    | `http://localhost:9007/comment/course/1`  | 课程评论列表 |
| POST   | `http://localhost:9007/comment/create`    | 创建评论   |
| DELETE | `http://localhost:9007/comment/1`         | 删除评论   |
| GET    | `http://localhost:9007/question/course/1` | 课程问答列表 |
| POST   | `http://localhost:9007/question/create`   | 创建问题   |
| DELETE | `http://localhost:9007/question/1`        | 删除问题   |
| GET    | `http://localhost:9007/answer/question/1` | 问题回答列表 |
| POST   | `http://localhost:9007/answer/create`     | 创建回答   |
| POST   | `http://localhost:9007/answer/accept/1`   | 采纳回答   |
| GET    | `http://localhost:9007/note/user/1`       | 用户笔记列表 |
| GET    | `http://localhost:9007/note/public`       | 公开笔记列表 |
| POST   | `http://localhost:9007/note/create`       | 创建笔记   |
| DELETE | `http://localhost:9007/note/1`            | 删除笔记   |

### 6.9 Search 搜索服务（9008）

| 方法     | URL                                                | 说明     |
| ------ | -------------------------------------------------- | ------ |
| GET    | `http://localhost:9008/search/course?keyword=Java` | 搜索课程   |
| POST   | `http://localhost:9008/search/sync`                | 同步课程索引 |
| DELETE | `http://localhost:9008/search/course/1`            | 删除课程索引 |

### 6.10 Statistics 统计服务（9009）

| 方法   | URL                                         | 说明     |
| ---- | ------------------------------------------- | ------ |
| GET  | `http://localhost:9009/statistics/platform` | 平台统计   |
| GET  | `http://localhost:9009/statistics/course/1` | 课程统计   |
| POST | `http://localhost:9009/statistics/daily`    | 生成每日统计 |

***

## 七、推荐的验证顺序

```
1. 验证 Nacos 是否运行       → 浏览器打开 http://localhost:8848/nacos
2. 启动各微服务               → Nacos 控制台查看服务列表确认注册
3. 直接访问各服务健康检查     → 确认服务本身正常
4. 通过网关访问               → 确认路由、鉴权、限流等配置生效
5. 测试 Feign 调用链路        → 如 /course/1/detail（会调用 User 服务）
```

***

## 八、快速存活验证清单

一次性快速验证所有服务是否存活（返回 200 即表示服务正常）：

```
GET http://localhost:9002/user/health           → User 服务
GET http://localhost:9003/course/health         → Course 服务
GET http://localhost:9004/order/list            → Order 服务
GET http://localhost:9005/pay/list              → Pay 服务
GET http://localhost:9006/video/course/1        → Video 服务
GET http://localhost:9007/comment/course/1      → Interaction 服务
GET http://localhost:9008/search/course         → Search 服务
GET http://localhost:9009/statistics/platform   → Statistics 服务
```

***

## 九、常见问题排查

| 现象                        | 可能原因        | 解决方案                               |
| ------------------------- | ----------- | ---------------------------------- |
| 连接超时                      | 服务未启动       | 检查服务进程是否运行                         |
| 404 Not Found             | 端口或路径错误     | 确认服务端口和 Controller 路径              |
| 401 Unauthorized          | 网关鉴权拦截      | 添加 `Authorization: Bearer xxx` 请求头 |
| 429 Too Many Requests     | Sentinel 限流 | 降低请求频率（当前限制 2次/秒）                  |
| 500 Internal Server Error | 服务内部错误      | 查看服务日志排查                           |
| 502 Bad Gateway           | 网关找不到服务实例   | 检查 Nacos 注册是否正常                    |

