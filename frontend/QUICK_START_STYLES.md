# 样式快速入门指南

## 快速开始

设计系统已经全局加载，你可以直接在任何组件中使用。

## 常用模式

### 1. 创建卡片布局

```vue
<template>
  <div class="container">
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">我的标题</h2>
      </div>
      <div class="card-body">
        <p>这里是内容</p>
      </div>
    </div>
  </div>
</template>
```

### 2. 响应式网格

```vue
<template>
  <!-- 移动端1列，平板2列，桌面3列，大屏4列 -->
  <div class="card-grid">
    <div class="card" v-for="item in items" :key="item.id">
      {{ item.name }}
    </div>
  </div>
</template>
```

### 3. Flex 布局

```vue
<template>
  <!-- 水平排列，两端对齐，垂直居中 -->
  <div class="d-flex justify-between align-center gap-md">
    <h2>标题</h2>
    <el-button type="primary">操作</el-button>
  </div>
</template>
```

### 4. 使用 CSS 变量

```vue
<style scoped>
.my-component {
  /* 使用颜色变量 */
  color: var(--color-text-primary);
  background-color: var(--color-bg-primary);
  
  /* 使用间距变量 */
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  
  /* 使用圆角变量 */
  border-radius: var(--radius-base);
  
  /* 使用阴影变量 */
  box-shadow: var(--shadow-card);
  
  /* 使用过渡变量 */
  transition: all var(--transition-base);
}
</style>
```

### 5. 响应式显示/隐藏

```vue
<template>
  <!-- 移动端隐藏 -->
  <div class="hide-mobile">
    这段内容在移动端不显示
  </div>
  
  <!-- 移动端显示，其他隐藏 -->
  <div class="show-mobile hide-tablet">
    这段内容只在移动端显示
  </div>
  
  <!-- 桌面端显示 -->
  <div class="show-desktop">
    这段内容只在桌面端显示
  </div>
</template>
```

### 6. 间距工具类

```vue
<template>
  <div class="mt-lg mb-xl pt-md pb-lg">
    <!-- mt-lg: margin-top: 24px -->
    <!-- mb-xl: margin-bottom: 32px -->
    <!-- pt-md: padding-top: 16px -->
    <!-- pb-lg: padding-bottom: 24px -->
  </div>
</template>
```

### 7. 文本样式

```vue
<template>
  <div>
    <h1 class="text-responsive-xl font-bold text-primary">
      大标题
    </h1>
    <p class="text-base text-secondary">
      正文内容
    </p>
    <span class="text-sm text-tertiary">
      辅助文本
    </span>
  </div>
</template>
```

### 8. 标签和徽章

```vue
<template>
  <div class="d-flex gap-sm">
    <span class="tag tag-primary">主要</span>
    <span class="tag tag-success">成功</span>
    <span class="badge">99+</span>
  </div>
</template>
```

### 9. 悬停效果

```vue
<template>
  <div class="card hover:shadow-lg hover:scale-105 transition-all">
    悬停时会有阴影和缩放效果
  </div>
</template>
```

### 10. 加载状态

```vue
<template>
  <div v-if="loading" class="d-flex justify-center align-center" style="min-height: 200px">
    <el-icon class="is-loading" :size="40">
      <Loading />
    </el-icon>
  </div>
</template>
```

## 颜色使用指南

### 主色调（蓝色）- 用于主要操作
```vue
<el-button type="primary">主要按钮</el-button>
<a class="text-primary">主要链接</a>
```

### 背景色（白色）- 用于容器背景
```vue
<div class="bg-primary">白色背景</div>
<div class="bg-secondary">浅灰背景</div>
```

### 灰色 - 用于边框和次要元素
```vue
<div class="border">带边框</div>
<p class="text-secondary">次要文本</p>
```

## 响应式断点

| 断点 | 宽度 | 设备 | 网格列数 |
|------|------|------|----------|
| xs | 320px | 移动设备 | 1列 |
| sm | 768px | 平板 | 2列 |
| md | 1024px | 桌面 | 3列 |
| lg | 1440px | 大屏幕 | 4列 |

## Element Plus 组件

所有 Element Plus 组件已经应用了自定义主题，直接使用即可：

```vue
<template>
  <el-button type="primary">按钮</el-button>
  <el-input v-model="value" placeholder="请输入" />
  <el-select v-model="selected">
    <el-option label="选项1" value="1" />
  </el-select>
</template>
```

## 常见问题

### Q: 如何自定义颜色？
A: 在组件的 `<style>` 中使用 CSS 变量：
```css
.my-element {
  color: var(--color-primary);
}
```

### Q: 如何创建响应式布局？
A: 使用 `card-grid` 类或响应式网格类：
```html
<div class="grid grid-cols-1 grid-cols-sm-2 grid-cols-md-3">
```

### Q: 如何添加动画效果？
A: 使用 `transition-all` 类配合悬停状态：
```html
<div class="transition-all hover:shadow-lg">
```

### Q: 如何确保移动端友好？
A: 使用响应式工具类和移动优先的设计：
```html
<div class="container">
  <div class="card-grid">
    <!-- 自动响应式 -->
  </div>
</div>
```

## 更多信息

- 完整文档: `DESIGN_SYSTEM.md`
- 实现细节: `THEME_IMPLEMENTATION.md`
- 示例组件: `src/components/DesignSystemDemo.vue`

## 提示

1. 优先使用工具类，避免编写自定义 CSS
2. 使用 CSS 变量保持一致性
3. 遵循移动优先的响应式设计
4. 利用 Element Plus 组件减少开发时间
5. 保持卡片化设计风格
