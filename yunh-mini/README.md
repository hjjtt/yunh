# yunh-mini

`yunh-mini` 是 `yunh` 项目的原生微信小程序学员端，已经补成可以直接被微信开发者工具导入的标准工程结构。

## 直接打开方式

1. 打开微信开发者工具
2. 导入项目根目录 [yunh-mini](D:/vis/yunh/yunh-mini)
3. 使用测试号或你自己的小程序 `AppID`
4. 如果本地联调走 `http://127.0.0.1:9000`，在开发者工具里勾选“不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书”

当前 [project.config.json](D:/vis/yunh/yunh-mini/project.config.json) 中已经写入了一个可直接导入的 `AppID`：`wx02da9208e46ca6a0`。  
如果你要切换到自己的小程序环境，直接替换成你自己的 `AppID` 即可。

## 当前已具备的工程能力

- 标准小程序项目配置：`project.config.json`
- 主包与分包路由：`app.json`
- 全局样式与主题变量：`app.wxss` + `styles`
- 环境配置：`env/dev.js`、`env/prod.js`
- 请求封装：`utils/request.js`
- 会话与缓存工具：`store/session.js`、`utils/storage.js`
- 基础服务层：`services/auth.js`、`services/course.js`、`services/order.js`、`services/user.js`
- 可直接打开的基础页面：首页、登录、课程列表、搜索、课程详情、章节、订单确认、支付结果、个人中心、我的课程

## 目录重点

```text
yunh-mini/
├─ project.config.json
├─ project.private.config.json
├─ package.json
├─ jsconfig.json
└─ miniprogram/
   ├─ app.js
   ├─ app.json
   ├─ app.wxss
   ├─ env/
   ├─ services/
   ├─ utils/
   ├─ store/
   ├─ components/
   ├─ pages/
   ├─ packageCourse/
   ├─ packageOrder/
   └─ packageProfile/
```

## 默认联调地址

开发环境默认网关地址是：

`http://127.0.0.1:9000`

如果你想切换，改这里就行：

- [dev.js](D:/vis/yunh/yunh-mini/miniprogram/env/dev.js)
- [prod.js](D:/vis/yunh/yunh-mini/miniprogram/env/prod.js)

## 下一步最值得继续做的内容

- 接入微信登录 `code -> token`
- 把课程列表、课程详情、我的课程改成真实接口联调
- 完善 tabBar 和通用业务组件
- 补充支付和学习记录链路
