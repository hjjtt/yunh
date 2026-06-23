---
alwaysApply: false
---

# Nacos 配置发布规则

## 核心原理

`spring.application.name` + `file-extension` = Nacos Data ID。如 `xxx-service` + `yml` → `xxx-service.yml`。

## 发布配置

```powershell
$body = @"
<YAML 内容>
"@
$encodedBody = [System.Uri]::EscapeDataString($body)
Invoke-RestMethod -Uri "http://127.0.0.1:8848/nacos/v1/cs/configs" -Method POST -Body "dataId=<DataID>.yml&group=DEFAULT_GROUP&type=yaml&content=$encodedBody&tenant=&username=your_nacos_username&password=your_nacos_password_here" -ContentType "application/x-www-form-urlencoded"
```

返回 `True` 即成功。

## 查询配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=<DataID>.yml&group=DEFAULT_GROUP&tenant=&username=your_nacos_username&password=your_nacos_password_here"
```

## Gateway 路由配置

路由核心字段：

- `id`：路由唯一标识
- `uri`：`lb://服务名` 表示负载均衡
- `predicates`：匹配条件，如 `Path=/api/user/**`
- `filters`：过滤器，如 `StripPrefix=1` 去掉路径前缀

转发示例：`/api/user/1` → StripPrefix=1 → `/user/1` → 转发到 user 服务

## Nacos 控制台操作

http://127.0.0.1:8848/nacos → 配置管理 → 配置列表 → 新建 → 填写 Data ID + Group + YAML 内容 → 发布
