/**
 * 老课本风格配置
 */
module.exports = {
  name: 'textbook',
  displayName: '老课本',
  // 配色
  colors: {
    primary: '#2c3e50', // 墨蓝主色（课本封面色）
    primaryLight: '#ecf0f1', // 浅灰蓝
    accent: '#c0392b', // 砖红强调色（红笔批注色）
    accentLight: '#fae1dd', // 浅砖红
    paper: '#f8f5f0', // 米黄纸张色（道林纸）
    inkDark: '#2d3436', // 深墨色（印刷文字）
    inkMedium: '#636e72', // 中墨色（注释文字）
    inkLight: '#b2bec3', // 浅墨色（辅助文字）
    line: '#dcd7c9', // 装订线/分割线色
  },
  // 字体配置
  fonts: {
    title: '"Times New Roman", "PingFang SC", serif', // 标题用衬线
    body: '"PingFang SC", "Microsoft YaHei", sans-serif', // 正文无衬线
    number: '"Times New Roman", serif', // 数字/英文衬线
  },
  // 圆角配置
  borderRadius: {
    card: '4rpx', // 卡片小圆角
    button: '4rpx', // 按钮小圆角
    input: '4rpx', // 输入框小圆角
    tag: '2rpx', // 标签更小的圆角
  },
  // 装订边配置
  bindingEdge: {
    width: '8rpx', // 普通卡片装订边宽度
    highlightWidth: '12rpx', // 重点卡片装订边宽度
    activeColor: '#c0392b', // 激活时装订边颜色
  },
  // 纹理配置
  texture: {
    dot: 'radial-gradient(#e8e4db 1px, transparent 0)', // 网点纹理
    dotSize: '20rpx', // 网点大小
    dotDark: 'radial-gradient(rgba(255,255,255,0.1) 1px, transparent 0)', // 深色背景网点
  },
  // 阴影配置
  shadow: {
    card: '0 4rpx 12rpx rgba(0,0,0,0.08)', // 普通卡片阴影（纸张叠放）
    press: '0 2rpx 6rpx rgba(0,0,0,0.1)', // 按压时阴影
  },
  // 动效配置
  animation: {
    press: 'all 0.2s ease', // 按压动效时长
    dotFloat: '20s linear infinite', // 网点浮动动画时长
  }
}
