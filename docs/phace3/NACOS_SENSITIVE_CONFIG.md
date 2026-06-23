# Nacos 配置中心 - 敏感数据配置指南

## 📋 配置说明

本地 `application.yml` 已移除敏感数据（数据库账号密码），所有敏感配置统一从 Nacos 加载。

##  Nacos 配置信息

**Nacos 控制台**: http://10.150.9.193:8080/index.html
- 账号：`nacos`
- 密码：`123456`

**已创建配置**:
| Data ID | Group | 格式 | 说明 |
|---------|-------|------|------|
| `yunh-service-user.yml` | DEFAULT_GROUP | YAML | User 服务配置（含数据库密码） |
| `yunh-service-course.yml` | DEFAULT_GROUP | YAML | Course 服务配置（含数据库密码） |

---

##  本地配置说明

本地 `application.yml` 现在只包含：
- ✅ 服务端口
- ✅ 连接池配置（非敏感）
- ✅ MyBatis-Plus 配置
- ✅ 日志配置

❌ **已移除**:
- 数据库 URL
- 数据库用户名
- 数据库密码

这些敏感数据现在全部从 Nacos 加载。

---

## 🔒 安全优势

| 项目 | 之前 | 现在 |
|------|------|------|
| 数据库密码 | 明文在代码仓库 | 仅存储在 Nacos |
| 代码审查 | 暴露敏感信息 | 无敏感信息 |
| 新成员加入 | 能看到密码 | 只能看到配置结构 |
| 配置修改 | 需要改代码重新部署 | Nacos 控制台直接修改 |
| 环境切换 | 改配置文件 | 切换 Nacos 命名空间 |

---

## ⚠️ 注意事项

1. **Nacos 必须运行**: 服务启动依赖 Nacos 配置，确保 Nacos 服务器正常运行
2. **兜底方案**: 如需本地开发兜底，可创建 `application-local.yml`（已加入 .gitignore）
3. **权限管理**: Nacos 控制台密码不要泄露
