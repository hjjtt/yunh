# 使用示例
## 基础使用：美化单个页面
### 示例1：美化课程列表页
```
使用复古学院风美化 pages/course/list
```
执行后会自动：
1. 读取pages/course/list.wxml和pages/course/list.wxss
2. 识别AI设计问题
3. 生成美化后的代码
4. 可以选择直接写入文件，或者手动复制

### 示例2：用手账风格美化个人中心
```
使用手账风格美化 pages/profile/index，装订边用紫色
```

### 示例3：用老报纸风格美化首页
```
使用老报纸风格美化 pages/home/index，禁用网点纹理，按钮用小圆角
```

## 批量美化
### 批量美化所有页面
```
批量美化以下页面：
pages/home/index
pages/course/list
pages/search/index
pages/profile/index
```

## 美化组件
### 美化课程卡片组件
```
美化组件 components/business/course-card
```

## 自定义配置示例
### 完全自定义风格
```
美化pages/order/confirm，配置如下：
- 风格：老课本
- 装订边颜色：#8b4513（棕色）
- 按钮样式：小圆角
- 禁用网点纹理
- 动效等级：medium
```

## 效果对比
### 美化前代码
```wxss
.card {
  border-radius: 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
  animation: float 3s ease-in-out infinite;
}
```

### 美化后代码
```wxss
/* 复古装订边 */
.vintage-card {
  border-left: 8rpx solid #2c3e50 !important;
  transition: all 0.2s ease !important;
}
.vintage-card:active {
  transform: translateY(2rpx) scale(0.98) !important;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.1) !important;
  border-left-color: #c0392b !important;
}
/* 复古按钮 */
.vintage-btn {
  border-radius: 4rpx !important;
  transition: all 0.2s ease !important;
  font-family: "Times New Roman", "PingFang SC", serif !important;
  letter-spacing: 2rpx !important;
}
.vintage-btn:active {
  transform: translateY(2rpx) !important;
  filter: brightness(0.9) !important;
}
/* 复古衬线字体 */
.text-serif {
  font-family: "Times New Roman", serif !important;
}
.card {
  border-radius: 4rpx;
  background: #f8f5f0;
  background-image: radial-gradient(#e8e4db 1px, transparent 0);
  background-size: 20rpx 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
}
```
