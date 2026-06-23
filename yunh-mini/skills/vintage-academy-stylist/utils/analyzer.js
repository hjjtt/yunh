/**
 * AI设计问题分析器
 * 识别现有代码里的AI常见烂大街设计
 */

// AI设计问题规则
const AI_DESIGN_RULES = [
  {
    type: 'large_border_radius',
    name: '大圆角',
    pattern: /border-radius\s*:\s*([\d.]+r?px)/gi,
    check: (match) => {
      const value = match[1]
      if (value.endsWith('rpx')) {
        const num = parseInt(value, 10)
        return num > 16 // rpx大于16就算大圆角
      } else if (value.endsWith('px')) {
        const num = parseInt(value, 10)
        return num > 8 // px大于8就算大圆角
      }
      return false
    }
  },
  {
    type: 'gradient_abuse',
    name: '滥用渐变',
    pattern: /background\s*:\s*(linear-gradient|radial-gradient)/gi,
    check: () => true
  },
  {
    type: 'glass_effect',
    name: '玻璃态效果',
    pattern: /backdrop-filter|filter.*blur/gi,
    check: () => true
  },
  {
    type: 'capsule_button',
    name: '胶囊按钮/标签',
    pattern: /border-radius\s*:\s*(999|50|100%).*r?px/gi,
    check: () => true
  },
  {
    type: 'generic_shadow',
    name: '千篇一律的阴影',
    pattern: /box-shadow\s*:\s*0\s*([\d.]+)r?px\s*([\d.]+)r?px\s*([\d.]+)r?px\s*rgba\(0,0,0,0.1\)/gi,
    check: (match) => {
      const y = parseInt(match[1], 10)
      const blur = parseInt(match[3], 10)
      return y > 10 && blur > 20 // 大阴影是AI常用
    }
  },
  {
    type: 'excessive_animation',
    name: '浮夸动效',
    pattern: /animation\s*:.*(float|bounce|scale.*1\.1|translateY.*-10)/gi,
    check: () => true
  }
]

/**
 * 分析wxss中的AI设计问题
 * @param {String} wxssContent wxss内容
 * @returns {Array<Object>} 问题列表
 */
function analyzeWxss(wxssContent) {
  const issues = []
  for (const rule of AI_DESIGN_RULES) {
    const matches = [...wxssContent.matchAll(rule.pattern)]
    for (const match of matches) {
      if (rule.check(match)) {
        issues.push({
          type: rule.type,
          name: rule.name,
          match: match[0],
          index: match.index
        })
      }
    }
  }
  // 去重
  return Array.from(new Set(issues.map(i => i.type))).map(type => issues.find(i => i.type === type))
}

/**
 * 分析wxml中的AI设计问题
 * @param {String} wxmlContent wxml内容
 * @returns {Array<Object>} 问题列表
 */
function analyzeWxml(wxmlContent) {
  const issues = []
  // 识别没用语义的class名，比如card、btn这种AI常用命名
  const genericClasses = ['card', 'btn', 'item', 'box', 'container', 'wrapper']
  for (const cls of genericClasses) {
    if (wxmlContent.includes(`class="${cls}"`) || wxmlContent.includes(`class='${cls}'`)) {
      issues.push({
        type: 'generic_class_name',
        name: '通用无语义类名',
        match: cls
      })
    }
  }
  return issues
}

module.exports = {
  analyzeWxss,
  analyzeWxml
}
