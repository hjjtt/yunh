/**
 * 老报纸风格配置
 */
module.exports = {
  name: 'newspaper',
  displayName: '老报纸',
  // 配色：复古做旧
  colors: {
    primary: '#1a1a1a', // 墨黑主色（报纸标题）
    primaryLight: '#f2f0e6',
    accent: '#8b0000', // 暗红强调色（报纸头版）
    accentLight: '#f5e6e6',
    paper: '#f0ede0', // 泛黄报纸色
    inkDark: '#1a1a1a',
    inkMedium: '#4a4a4a',
    inkLight: '#7a7a7a',
    line: '#d4d0c0',
  },
  // 字体配置：报纸排版
  fonts: {
    title: '"Georgia", "SimSun", serif', // 宋体标题
    body: '"SimSun", "Microsoft YaHei", serif', // 宋体正文
    number: '"Georgia", serif',
  },
  // 圆角配置：几乎直角
  borderRadius: {
    card: '2rpx',
    button: '2rpx',
    input: '2rpx',
    tag: '0rpx',
  },
  // 装订边配置
  bindingEdge: {
    width: '6rpx',
    highlightWidth: '8rpx',
    activeColor: '#8b0000',
  },
  // 纹理配置：印刷网点+做旧纹理
  texture: {
    dot: 'radial-gradient(#e0dccc 1px, transparent 0)',
    dotSize: '16rpx',
    grain: 'url("data:image/svg+xml,%3Csvg viewBox=\'0 0 200 200\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cfilter id=\'noiseFilter\'%3E%3CfeTurbulence type=\'fractalNoise\' baseFrequency=\'0.65\' numOctaves=\'3\' stitchTiles=\'stitch\'/%3E%3C/filter%3E%3Crect width=\'100%\' height=\'100%\' filter=\'url(%23noiseFilter)\' opacity=\'0.1\'/%3E%3C/svg%3E")', // 噪点纹理
  },
  // 阴影配置：轻微
  shadow: {
    card: '0 2rpx 8rpx rgba(0,0,0,0.06)',
    press: '0 1rpx 4rpx rgba(0,0,0,0.08)',
  },
  // 动效配置：非常克制
  animation: {
    press: 'all 0.15s ease',
  }
}
