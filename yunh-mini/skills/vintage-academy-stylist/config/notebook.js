/**
 * 手账本风格配置
 */
module.exports = {
  name: 'notebook',
  displayName: '手账本',
  // 配色：马卡龙色系
  colors: {
    primary: '#9b59b6', // 香芋紫主色
    primaryLight: '#f4ecf7',
    accent: '#e74c3c', // 草莓红强调色
    accentLight: '#fdedec',
    paper: '#fff9e6', // 米黄纸张色（笔记本内页）
    inkDark: '#34495e',
    inkMedium: '#7f8c8d',
    inkLight: '#bdc3c7',
    line: '#e8dcc9', // 虚线装订线色
  },
  // 字体配置
  fonts: {
    title: '"Comic Sans MS", "PingFang SC", cursive', // 手写体标题
    body: '"PingFang SC", "Microsoft YaHei", sans-serif',
    number: '"Comic Sans MS", cursive',
  },
  // 圆角配置
  borderRadius: {
    card: '8rpx',
    button: '8rpx',
    input: '8rpx',
    tag: '4rpx',
  },
  // 装订边配置（虚线）
  bindingEdge: {
    width: '4rpx',
    highlightWidth: '6rpx',
    activeColor: '#e74c3c',
    style: 'dashed', // 虚线装订线
  },
  // 纹理配置
  texture: {
    dot: 'radial-gradient(#f0e6d6 1px, transparent 0)',
    dotSize: '24rpx',
    line: 'linear-gradient(to bottom, transparent 95%, #e8dcc9 95%)', // 横线笔记本纹理
    lineSize: '40rpx',
  },
  // 阴影配置
  shadow: {
    card: '0 6rpx 16rpx rgba(155,89,182,0.1)',
    press: '0 2rpx 8rpx rgba(155,89,182,0.15)',
  },
  // 动效配置
  animation: {
    press: 'all 0.3s ease',
    float: '15s ease-in-out infinite', // 贴纸浮动动效
  }
}
