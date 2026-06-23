/**
 * 美化代码生成器
 */

/**
 * 生成美化后的wxml
 * @param {String} originalWxml 原始wxml
 * @param {Object} styleConfig 风格配置
 * @param {Object} options 用户选项
 * @returns {String} 美化后的wxml
 */
function generateWxml(originalWxml, styleConfig, options) {
  let wxml = originalWxml

  // 1. 给所有卡片类元素添加装订边标识
  wxml = wxml.replace(/class="(.*card.*)"/gi, (match, cls) => {
    return `class="${cls} vintage-card"`
  })

  // 2. 给按钮添加复古类
  wxml = wxml.replace(/class="(.*button.*|.*btn.*)"/gi, (match, cls) => {
    return `class="${cls} vintage-btn"`
  })

  // 3. 给英文/数字标题添加衬线字体类
  if (options.enableSerifFont) {
    wxml = wxml.replace(/class="(.*title.*|.*price.*|.*number.*)"/gi, (match, cls) => {
      return `class="${cls} text-serif"`
    })
  }

  return wxml
}

/**
 * 生成美化后的wxss
 * @param {String} originalWxss 原始wxss
 * @param {Object} styleConfig 风格配置
 * @param {Object} options 用户选项
 * @param {Array<Object>} aiIssues 识别到的AI问题
 * @returns {String} 美化后的wxss
 */
function generateWxss(originalWxss, styleConfig, options, aiIssues) {
  let wxss = originalWxss

  // 1. 替换大圆角为小圆角
  if (aiIssues.some(i => i.type === 'large_border_radius' || i.type === 'capsule_button')) {
    // 替换大圆角为风格配置的圆角
    wxss = wxss.replace(/border-radius\s*:\s*[\d.]+r?px\s*;?/gi, (match) => {
      if (match.includes('999') || match.includes('50%')) { // 胶囊按钮
        return `border-radius: ${styleConfig.borderRadius.button};`
      }
      // 普通圆角
      return `border-radius: ${styleConfig.borderRadius.card};`
    })
  }

  // 2. 替换渐变背景为纯色+纹理
  if (aiIssues.some(i => i.type === 'gradient_abuse')) {
    wxss = wxss.replace(/background\s*:\s*(linear-gradient|radial-gradient).*?;/gi, () => {
      let bg = `background: ${styleConfig.colors.paper};`
      if (options.enableDotTexture) {
        bg += `\n  background-image: ${styleConfig.texture.dot};`
        bg += `\n  background-size: ${styleConfig.texture.dotSize} ${styleConfig.texture.dotSize};`
      }
      return bg
    })
  }

  // 3. 移除玻璃态效果
  if (aiIssues.some(i => i.type === 'glass_effect')) {
    wxss = wxss.replace(/backdrop-filter\s*:.*?;?/gi, '')
    wxss = wxss.replace(/filter\s*:.*blur.*?;?/gi, '')
  }

  // 4. 替换通用阴影为复古阴影
  if (aiIssues.some(i => i.type === 'generic_shadow')) {
    wxss = wxss.replace(/box-shadow\s*:.*?rgba\(0,0,0,0.1\).*?;?/gi, `box-shadow: ${styleConfig.shadow.card};`)
  }

  // 5. 移除浮夸动效
  if (aiIssues.some(i => i.type === 'excessive_animation')) {
    wxss = wxss.replace(/animation\s*:.*(float|bounce).*?;?/gi, '')
    wxss = wxss.replace(/transition\s*:.*all\s*0\.5s.*?;?/gi, `transition: ${styleConfig.animation.press};`)
  }

  // 6. 添加装订边样式
  const bindingStyle = `
/* 复古装订边 */
.vintage-card {
  border-left: ${styleConfig.bindingEdge.width} solid ${styleConfig.bindingEdgeColor || styleConfig.colors.primary} !important;
  transition: ${styleConfig.animation.press} !important;
}
.vintage-card:active {
  transform: translateY(2rpx) scale(0.98) !important;
  box-shadow: ${styleConfig.shadow.press} !important;
  border-left-color: ${styleConfig.bindingEdge.activeColor} !important;
}
`
  // 7. 添加按钮样式
  const buttonStyle = `
/* 复古按钮 */
.vintage-btn {
  border-radius: ${styleConfig.borderRadius.button} !important;
  transition: ${styleConfig.animation.press} !important;
  font-family: ${styleConfig.fonts.title} !important;
  letter-spacing: 2rpx !important;
}
.vintage-btn:active {
  transform: translateY(2rpx) !important;
  filter: brightness(0.9) !important;
}
`
  // 8. 添加衬线字体样式
  const fontStyle = options.enableSerifFont ? `
/* 复古衬线字体 */
.text-serif {
  font-family: ${styleConfig.fonts.number} !important;
}
` : ''

  // 9. 添加全局样式到wxss顶部
  const globalStyle = `${bindingStyle}\n${buttonStyle}\n${fontStyle}\n`
  wxss = globalStyle + wxss

  return wxss
}

/**
 * 生成组件美化后的wxml
 * @param {String} originalWxml 原始组件wxml
 * @param {Object} styleConfig 风格配置
 * @param {Object} options 用户选项
 * @returns {String} 美化后的wxml
 */
function generateComponentWxml(originalWxml, styleConfig, options) {
  return generateWxml(originalWxml, styleConfig, options)
}

/**
 * 生成组件美化后的wxss
 * @param {String} originalWxss 原始组件wxss
 * @param {Object} styleConfig 风格配置
 * @param {Object} options 用户选项
 * @returns {String} 美化后的wxss
 */
function generateComponentWxss(originalWxss, styleConfig, options) {
  // 组件wxss不需要加全局样式，直接处理即可
  let wxss = originalWxss

  // 替换圆角
  wxss = wxss.replace(/border-radius\s*:\s*[\d.]+r?px\s*;?/gi, `border-radius: ${styleConfig.borderRadius.card};`)
  
  // 添加装订边
  wxss += `
.root {
  border-left: ${styleConfig.bindingEdge.width} solid ${styleConfig.colors.primary};
  transition: ${styleConfig.animation.press};
}
.root:active {
  border-left-color: ${styleConfig.colors.accent};
  transform: translateY(2rpx) scale(0.98);
}
`

  // 添加纹理
  if (options.enableDotTexture) {
    wxss += `
.root {
  background-image: ${styleConfig.texture.dot};
  background-size: ${styleConfig.texture.dotSize} ${styleConfig.texture.dotSize};
}
`
  }

  return wxss
}

module.exports = {
  generateWxml,
  generateWxss,
  generateComponentWxml,
  generateComponentWxss
}
