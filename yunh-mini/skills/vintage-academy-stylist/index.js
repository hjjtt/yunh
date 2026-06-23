/**
 * 复古学院风前端美化师核心逻辑
 */
const fs = require('fs')
const path = require('path')
const analyzer = require('./utils/analyzer')
const generator = require('./utils/generator')

// 支持的风格变体
const STYLE_VARIANTS = {
  textbook: require('./config/textbook'),
  notebook: require('./config/notebook'),
  newspaper: require('./config/newspaper')
}

/**
 * 美化指定页面
 * @param {String} pagePath 页面路径，比如pages/course/list
 * @param {Object} options 配置选项
 * @returns {Object} 美化结果，包含wxml和wxss的修改内容
 */
async function beautifyPage(pagePath, options = {}) {
  // 1. 解析配置，默认用老课本风格
  const config = {
    styleVariant: 'textbook',
    enableDotTexture: true,
    bindingEdgeColor: null,
    buttonStyle: 'sharp',
    enableSerifFont: true,
    animationLevel: 'low',
    ...options
  }
  const styleConfig = STYLE_VARIANTS[config.styleVariant] || STYLE_VARIANTS.textbook
  if (config.bindingEdgeColor) {
    styleConfig.bindingEdgeColor = config.bindingEdgeColor
  }

  // 2. 读取现有页面文件
  const rootPath = process.cwd()
  const wxmlPath = path.join(rootPath, 'miniprogram', `${pagePath}.wxml`)
  const wxssPath = path.join(rootPath, 'miniprogram', `${pagePath}.wxss`)
  
  if (!fs.existsSync(wxmlPath) || !fs.existsSync(wxssPath)) {
    throw new Error(`页面${pagePath}不存在`)
  }

  const originalWxml = fs.readFileSync(wxmlPath, 'utf8')
  const originalWxss = fs.readFileSync(wxssPath, 'utf8')

  // 3. 分析AI设计问题
  const aiIssues = analyzer.analyzeWxss(originalWxss)
  console.log(`识别到${aiIssues.length}个AI设计问题：`, aiIssues.map(i => i.type))

  // 4. 生成美化后的代码
  const beautifiedWxml = generator.generateWxml(originalWxml, styleConfig, config)
  const beautifiedWxss = generator.generateWxss(originalWxss, styleConfig, config, aiIssues)

  // 5. 返回结果
  return {
    pagePath,
    originalWxml,
    originalWxss,
    beautifiedWxml,
    beautifiedWxss,
    aiIssues,
    config,
    // 直接写入文件的方法
    writeToFile() {
      fs.writeFileSync(wxmlPath, beautifiedWxml, 'utf8')
      fs.writeFileSync(wxssPath, beautifiedWxss, 'utf8')
      console.log(`页面${pagePath}美化完成，已写入文件`)
    }
  }
}

/**
 * 批量美化多个页面
 * @param {Array<String>} pagePaths 页面路径数组
 * @param {Object} options 配置选项
 * @returns {Array<Object>} 美化结果数组
 */
async function batchBeautify(pagePaths, options = {}) {
  const results = []
  for (const pagePath of pagePaths) {
    try {
      const res = await beautifyPage(pagePath, options)
      results.push(res)
    } catch (e) {
      console.error(`美化页面${pagePath}失败：`, e.message)
      results.push({ pagePath, error: e.message })
    }
  }
  return results
}

/**
 * 美化单个组件
 * @param {String} componentPath 组件路径，比如components/business/course-card
 * @param {Object} options 配置选项
 * @returns {Object} 美化结果
 */
async function beautifyComponent(componentPath, options = {}) {
  const config = {
    styleVariant: 'textbook',
    enableDotTexture: true,
    ...options
  }
  const styleConfig = STYLE_VARIANTS[config.styleVariant] || STYLE_VARIANTS.textbook

  const rootPath = process.cwd()
  const wxmlPath = path.join(rootPath, 'miniprogram', `${componentPath}.wxml`)
  const wxssPath = path.join(rootPath, 'miniprogram', `${componentPath}.wxss`)
  
  if (!fs.existsSync(wxmlPath) || !fs.existsSync(wxssPath)) {
    throw new Error(`组件${componentPath}不存在`)
  }

  const originalWxml = fs.readFileSync(wxmlPath, 'utf8')
  const originalWxss = fs.readFileSync(wxssPath, 'utf8')

  const beautifiedWxml = generator.generateComponentWxml(originalWxml, styleConfig, config)
  const beautifiedWxss = generator.generateComponentWxss(originalWxss, styleConfig, config)

  return {
    componentPath,
    beautifiedWxml,
    beautifiedWxss,
    writeToFile() {
      fs.writeFileSync(wxmlPath, beautifiedWxml, 'utf8')
      fs.writeFileSync(wxssPath, beautifiedWxss, 'utf8')
    }
  }
}

module.exports = {
  beautifyPage,
  batchBeautify,
  beautifyComponent
}
