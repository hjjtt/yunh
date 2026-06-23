/**
 * 应用入口文件
 * 在线教育平台管理后台 - Vue3 + Element Plus
 */
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import { createPinia } from 'pinia'
import './styles/global.scss'
import 'nprogress/nprogress.css'
import App from './App.vue'

// 创建 Vue 应用实例
const app = createApp(App)

// 注册 Element Plus 所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.use(createPinia())

// 挂载应用
app.mount('#app')
