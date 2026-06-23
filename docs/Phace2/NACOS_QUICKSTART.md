# 📖 Nacos 快速上手指南

## 🎯 本指南目标

帮助你在 10 分钟内完成 Nacos 安装和配置，让服务成功注册。

---

## 📥 第一步：下载 Nacos

### 版本选择

| Spring Cloud Alibaba | 推荐 Nacos 版本 |
|---------------------|----------------|
| 2.2.1.RELEASE       | 1.7.0          |
| 2.1.0.RELEASE       | 1.4.1          |

**本项目使用：** Nacos Server 1.7.0

### 下载地址

**GitHub Releases:**
```
https://github.com/alibaba/nacos/releases/tag/v1.7.0
```

**直接下载（nacos-server-1.7.0.zip）：**
```
https://github.com/alibaba/nacos/releases/download/v1.7.0/nacos-server-1.7.0.zip
```

---

## 📂 第二步：解压

推荐解压路径：
```
D:\env\nacos\nacos\
```

解压后目录结构：
```
nacos/
├── bin/              # 启动脚本
│   ├── startup.cmd   # Windows 启动脚本
│   └── shutdown.cmd  # Windows 关闭脚本
├── conf/             # 配置文件
│   └── application.properties
├── logs/             # 日志目录
└── data/             # 数据目录
```

---

## 🚀 第三步：启动 Nacos

### Windows 单机模式启动

```bash
# 1. 打开命令提示符
# 2. 进入 bin 目录
cd D:\env\nacos\nacos\bin

# 3. 单机模式启动（关键参数：-m standalone）
startup.cmd -m standalone
```

### 启动成功标志

看到以下日志表示启动成功：
```
Tomcat started on port(s): 8848 (http)
Nacos started successfully in stand alone mode.
```

### Linux/Mac 单机模式启动

```bash
cd nacos/bin
sh startup.sh -m standalone
```

---

## 🌐 第四步：访问控制台

浏览器打开：
```
http://localhost:8848/nacos
```

**默认登录信息：**
- 用户名：`nacos`
- 密码：`nacos`

### 控制台界面

登录后可以看到：
- 服务管理 → 服务列表
- 配置管理 → 配置列表
- 集群管理 → 节点列表

---

## ✅ 第五步：验证服务注册

### 启动 User 服务

在 IDEA 中运行 `UserServiceApplication.java`

### 观察 Nacos 控制台

1. 登录 Nacos 控制台
2. 点击左侧 **服务管理** → **服务列表**
3. 应该看到 `yunh-service-user`

### 查看服务详情

点击服务名称，可以看到：
- 服务名：yunh-service-user
- 实例数：1
- 健康实例数：1
- 实例地址：IP:Port

---

## 🔧 常见问题

### 问题 1：启动报错 "Java HotSpot VM"

**原因：** JDK 版本不兼容

**解决：** 使用 JDK 1.8

### 问题 2：端口 8848 被占用

**解决 1：** 查找占用端口的进程
```bash
netstat -ano | findstr :8848
```

**解决 2：** 杀死进程
```bash
taskkill /F /PID <进程 ID>
```

### 问题 3：启动后很快关闭

**原因：** 内存不足

**解决：** 修改 `bin/startup.cmd`，调整 JVM 参数
```
set JAVA_MEM_OPTS=-Xms512m -Xmx512m -Xmn256m
```

### 问题 4：无法访问控制台

**检查：**
1. Nacos 是否启动成功
2. 防火墙是否阻止 8848 端口
3. 浏览器地址是否正确（http://localhost:8848/nacos）

---

## 📊 Nacos 控制台功能

### 服务管理

- **服务列表**：查看所有注册的服务
- **服务详情**：查看服务的实例信息
- **编辑服务**：修改服务配置

### 配置管理

- **配置列表**：查看所有配置
- **配置详情**：查看和编辑配置内容
- **历史版本**：查看配置变更历史

### 集群管理

- **节点列表**：查看 Nacos 集群节点
- **节点监控**：监控节点健康状态

---

## 🧪 测试服务注册

### 1. 启动两个 User 服务实例

**实例 1：** 端口 9001
**实例 2：** 修改配置端口为 9011，然后启动

### 2. 观察 Nacos 控制台

在服务列表中，`yunh-service-user` 应该显示：
- 实例数：2
- 健康实例数：2

### 3. 测试负载均衡

多次访问 Course 服务的 Feign 接口：
```
http://localhost:9002/course/1/detail
```

观察日志，发现请求被分发到不同的 User 服务实例。

---

## 🛑 关闭 Nacos

```bash
cd D:\env\nacos\nacos\bin
shutdown.cmd
```

---

## 📚 下一步

完成 Nacos 安装后，继续学习：

1. **服务注册与发现** - PHASE2_GUIDE.md
2. **配置中心** - PHASE3_GUIDE.md（待完成）

---

## 📞 需要帮助？

- Nacos 官方文档：https://nacos.io/zh-cn/docs/quick-start.html
- 项目文档：PHASE2_COMPLETE.md

---

**祝你学习愉快！** 🎓
