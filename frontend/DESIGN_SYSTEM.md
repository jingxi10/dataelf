# 数流精灵设计系统文档

## 概述

数流精灵采用现代化的设计系统，遵循以下核心原则：
- **颜色分配**: 白色70%、蓝色25%、灰色5%
- **字体**: Inter 和 Roboto
- **设计风格**: 卡片化设计
- **响应式**: 支持 320px、768px、1024px、1440px 四个断点

## 颜色系统

### 主色调 - 蓝色 (25%)
```css
--color-primary: #2563eb
--color-primary-light: #3b82f6
--color-primary-lighter: #60a5fa
--color-primary-dark: #1d4ed8
--color-primary-darker: #1e40af
```

### 背景色 - 白色 (70%)
```css
--color-bg-primary: #ffffff
--color-bg-secondary: #f9fafb
--color-bg-tertiary: #f3f4f6
```

### 灰色系统 (5%)
```css
--color-gray-50: #f9fafb
--color-gray-100: #f3f4f6
--color-gray-200: #e5e7eb
--color-gray-300: #d1d5db
--color-gray-400: #9ca3af
--color-gray-500: #6b7280
--color-gray-600: #4b5563
--color-gray-700: #374151
--color-gray-800: #1f2937
--color-gray-900: #111827
```

### 语义化颜色
```css
--color-success: #10b981
--color-warning: #f59e0b
--color-error: #ef4444
--color-info: #3b82f6
```

## 字体系统

### 字体家族
```css
--font-family-base: 'Inter', 'Roboto', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif
--font-family-mono: 'Fira Code', 'Monaco', 'Consolas', 'Courier New', monospace
```

### 字体大小
```css
--font-size-xs: 12px
--font-size-sm: 14px
--font-size-base: 16px
--font-size-lg: 18px
--font-size-xl: 20px
--font-size-2xl: 24px
--font-size-3xl: 30px
--font-size-4xl: 36px
--font-size-5xl: 48px
```

### 字重
```css
--font-weight-normal: 400
--font-weight-medium: 500
--font-weight-semibold: 600
--font-weight-bold: 700
```

## 间距系统

```css
--spacing-xs: 4px
--spacing-sm: 8px
--spacing-md: 16px
--spacing-lg: 24px
--spacing-xl: 32px
--spacing-2xl: 48px
--spacing-3xl: 64px
```

## 圆角系统

```css
--radius-sm: 4px
--radius-base: 8px
--radius-md: 12px
--radius-lg: 16px
--radius-xl: 20px
--radius-full: 9999px
```

## 阴影系统

```css
--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05)
--shadow-base: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1)
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)
--shadow-card: 0 2px 8px rgba(0, 0, 0, 0.08)
```

## 响应式断点

### 断点定义
```css
--breakpoint-xs: 320px  /* 移动设备 */
--breakpoint-sm: 768px  /* 平板 */
--breakpoint-md: 1024px /* 桌面 */
--breakpoint-lg: 1440px /* 大屏幕 */
```

### 容器最大宽度
```css
--container-xs: 320px
--container-sm: 768px
--container-md: 1024px
--container-lg: 1440px
--container-xl: 1600px
```

### 响应式使用示例

#### HTML
```html
<div class="container">
  <div class="card-grid">
    <div class="card">内容1</div>
    <div class="card">内容2</div>
    <div class="card">内容3</div>
  </div>
</div>
```

#### 响应式显示/隐藏
```html
<!-- 移动端隐藏 -->
<div class="hide-mobile">桌面端内容</div>

<!-- 移动端显示 -->
<div class="show-mobile">移动端内容</div>

<!-- 平板端隐藏 -->
<div class="hide-tablet">非平板内容</div>

<!-- 桌面端显示 -->
<div class="show-desktop">桌面端内容</div>
```

## 卡片设计

### 基础卡片
```html
<div class="card">
  <div class="card-header">
    <h3 class="card-title">卡片标题</h3>
  </div>
  <div class="card-body">
    <p>卡片内容</p>
  </div>
  <div class="card-footer">
    <button>操作按钮</button>
  </div>
</div>
```

### 卡片特性
- 默认圆角: `var(--radius-md)` (12px)
- 默认阴影: `var(--shadow-card)`
- 悬停效果: 阴影增强至 `var(--shadow-md)`
- 默认内边距: `var(--spacing-lg)` (24px)

## 工具类

### 间距工具类
```html
<!-- Margin -->
<div class="mt-md">上边距 16px</div>
<div class="mb-lg">下边距 24px</div>
<div class="ml-sm">左边距 8px</div>
<div class="mr-xl">右边距 32px</div>

<!-- Padding -->
<div class="pt-md">上内边距 16px</div>
<div class="pb-lg">下内边距 24px</div>
<div class="pl-sm">左内边距 8px</div>
<div class="pr-xl">右内边距 32px</div>
```

### 文本工具类
```html
<p class="text-primary">主要文本颜色</p>
<p class="text-secondary">次要文本颜色</p>
<p class="text-sm">小号文本</p>
<p class="text-lg">大号文本</p>
<p class="font-semibold">半粗体</p>
<p class="text-center">居中对齐</p>
```

### Flex 工具类
```html
<div class="d-flex justify-between align-center gap-md">
  <div>左侧内容</div>
  <div>右侧内容</div>
</div>
```

### 网格工具类
```html
<!-- 响应式网格 -->
<div class="grid grid-cols-1 grid-cols-sm-2 grid-cols-md-3 grid-cols-lg-4 gap-lg">
  <div>项目1</div>
  <div>项目2</div>
  <div>项目3</div>
  <div>项目4</div>
</div>
```

## 组件示例

### 按钮
```html
<el-button type="primary">主要按钮</el-button>
<el-button type="success">成功按钮</el-button>
<el-button type="warning">警告按钮</el-button>
<el-button type="danger">危险按钮</el-button>
```

### 表单
```html
<el-form :model="form" label-width="120px">
  <el-form-item label="用户名">
    <el-input v-model="form.username" />
  </el-form-item>
  <el-form-item label="邮箱">
    <el-input v-model="form.email" type="email" />
  </el-form-item>
</el-form>
```

### 表格
```html
<el-table :data="tableData" style="width: 100%">
  <el-table-column prop="name" label="姓名" />
  <el-table-column prop="email" label="邮箱" />
  <el-table-column prop="status" label="状态" />
</el-table>
```

## 动画效果

### 过渡动画
```css
--transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1)
--transition-base: 250ms cubic-bezier(0.4, 0, 0.2, 1)
--transition-slow: 350ms cubic-bezier(0.4, 0, 0.2, 1)
```

### 使用示例
```html
<div class="transition-all hover:shadow-lg hover:scale-105">
  悬停时有动画效果
</div>
```

### 内置动画类
```html
<div class="fade-in">淡入动画</div>
<div class="slide-in-up">滑入动画</div>
<div class="is-loading">旋转加载动画</div>
<div class="pulse">脉冲动画</div>
<div class="bounce">弹跳动画</div>
```

## 最佳实践

### 1. 使用 CSS 变量
```css
/* 推荐 */
.my-component {
  color: var(--color-text-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-base);
}

/* 不推荐 */
.my-component {
  color: #111827;
  padding: 16px;
  border-radius: 8px;
}
```

### 2. 响应式设计
```css
/* 移动优先 */
.my-component {
  padding: var(--spacing-md);
}

/* 平板及以上 */
@media (min-width: 768px) {
  .my-component {
    padding: var(--spacing-lg);
  }
}

/* 桌面及以上 */
@media (min-width: 1024px) {
  .my-component {
    padding: var(--spacing-xl);
  }
}
```

### 3. 卡片化设计
```html
<!-- 推荐：使用卡片包装内容 -->
<div class="card">
  <div class="card-header">
    <h3 class="card-title">标题</h3>
  </div>
  <div class="card-body">
    内容区域
  </div>
</div>
```

### 4. 使用工具类
```html
<!-- 推荐：使用工具类快速构建布局 -->
<div class="d-flex justify-between align-center gap-md mb-lg">
  <h2 class="text-xl font-semibold">标题</h2>
  <button class="btn-primary">操作</button>
</div>
```

## 文件结构

```
frontend/src/styles/
├── variables.css           # CSS 变量定义
├── global.css             # 全局样式和重置
├── element-plus-theme.css # Element Plus 主题定制
├── responsive.css         # 响应式布局样式
└── utilities.css          # 工具类样式
```

## 浏览器支持

- Chrome (最新版本)
- Firefox (最新版本)
- Safari (最新版本)
- Edge (最新版本)
- 移动端浏览器 (iOS Safari, Chrome Mobile)

## 性能优化

1. **CSS 变量**: 使用 CSS 变量实现主题切换，无需重新编译
2. **响应式图片**: 使用 `srcset` 和 `sizes` 属性
3. **懒加载**: 对非关键内容使用懒加载
4. **代码分割**: 路由级别的代码分割
5. **Tree Shaking**: 只导入使用的组件和样式

## 可访问性

1. **颜色对比度**: 所有文本颜色符合 WCAG AA 标准
2. **键盘导航**: 所有交互元素支持键盘操作
3. **语义化 HTML**: 使用正确的 HTML 标签
4. **ARIA 属性**: 为复杂组件添加 ARIA 属性
5. **焦点管理**: 清晰的焦点指示器

## 更新日志

### v1.0.0 (2024-01-01)
- 初始设计系统发布
- 实现颜色系统（白色70%、蓝色25%、灰色5%）
- 配置 Inter 和 Roboto 字体
- 实现卡片化设计
- 实现响应式断点（320px、768px、1024px、1440px）
- Element Plus 主题定制
- 完整的工具类系统
