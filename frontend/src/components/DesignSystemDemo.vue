<template>
  <div class="design-system-demo">
    <div class="container">
      <div class="page-content">
        <!-- 标题区域 -->
        <div class="mb-xl">
          <h1 class="text-responsive-xl mb-md">数流精灵设计系统</h1>
          <p class="text-secondary">展示设计系统的核心组件和样式</p>
        </div>

        <!-- 颜色系统 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">颜色系统</h2>
          </div>
          <div class="card-body">
            <div class="grid grid-cols-1 grid-cols-sm-2 grid-cols-md-3 gap-md">
              <!-- 主色调 -->
              <div>
                <h4 class="mb-sm">主色调 - 蓝色 (25%)</h4>
                <div class="d-flex flex-column gap-sm">
                  <div class="color-swatch bg-blue" style="background-color: var(--color-primary)">
                    <span class="color-label">Primary</span>
                  </div>
                  <div class="color-swatch bg-blue-light" style="background-color: var(--color-primary-light)">
                    <span class="color-label">Primary Light</span>
                  </div>
                </div>
              </div>

              <!-- 背景色 -->
              <div>
                <h4 class="mb-sm">背景色 - 白色 (70%)</h4>
                <div class="d-flex flex-column gap-sm">
                  <div class="color-swatch border" style="background-color: var(--color-bg-primary)">
                    <span class="color-label text-primary">Primary</span>
                  </div>
                  <div class="color-swatch border" style="background-color: var(--color-bg-secondary)">
                    <span class="color-label text-primary">Secondary</span>
                  </div>
                </div>
              </div>

              <!-- 灰色系统 -->
              <div>
                <h4 class="mb-sm">灰色系统 (5%)</h4>
                <div class="d-flex flex-column gap-sm">
                  <div class="color-swatch bg-gray-100" style="background-color: var(--color-gray-100)">
                    <span class="color-label text-primary">Gray 100</span>
                  </div>
                  <div class="color-swatch" style="background-color: var(--color-gray-500)">
                    <span class="color-label">Gray 500</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 字体系统 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">字体系统</h2>
          </div>
          <div class="card-body">
            <div class="mb-md">
              <p class="text-xs mb-xs">Extra Small (12px)</p>
              <p class="text-sm mb-xs">Small (14px)</p>
              <p class="text-base mb-xs">Base (16px)</p>
              <p class="text-lg mb-xs">Large (18px)</p>
              <p class="text-xl mb-xs">Extra Large (20px)</p>
            </div>
            <div>
              <p class="font-normal mb-xs">Normal Weight (400)</p>
              <p class="font-medium mb-xs">Medium Weight (500)</p>
              <p class="font-semibold mb-xs">Semibold Weight (600)</p>
              <p class="font-bold mb-xs">Bold Weight (700)</p>
            </div>
          </div>
        </div>

        <!-- 卡片设计 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">卡片化设计</h2>
          </div>
          <div class="card-body">
            <div class="card-grid">
              <div class="card hover:shadow-lg transition-all">
                <h3 class="mb-sm">卡片 1</h3>
                <p class="text-secondary">这是一个示例卡片，展示卡片化设计风格</p>
              </div>
              <div class="card hover:shadow-lg transition-all">
                <h3 class="mb-sm">卡片 2</h3>
                <p class="text-secondary">悬停时会有阴影增强效果</p>
              </div>
              <div class="card hover:shadow-lg transition-all">
                <h3 class="mb-sm">卡片 3</h3>
                <p class="text-secondary">响应式网格布局自动适配</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 按钮组件 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">按钮组件</h2>
          </div>
          <div class="card-body">
            <div class="d-flex flex-wrap gap-md">
              <el-button type="primary">主要按钮</el-button>
              <el-button type="success">成功按钮</el-button>
              <el-button type="warning">警告按钮</el-button>
              <el-button type="danger">危险按钮</el-button>
              <el-button type="info">信息按钮</el-button>
              <el-button>默认按钮</el-button>
            </div>
          </div>
        </div>

        <!-- 标签和徽章 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">标签和徽章</h2>
          </div>
          <div class="card-body">
            <div class="mb-md">
              <h4 class="mb-sm">标签</h4>
              <div class="d-flex flex-wrap gap-sm">
                <span class="tag">默认标签</span>
                <span class="tag tag-primary">主要标签</span>
                <span class="tag tag-success">成功标签</span>
                <span class="tag tag-warning">警告标签</span>
                <span class="tag tag-error">错误标签</span>
              </div>
            </div>
            <div>
              <h4 class="mb-sm">徽章</h4>
              <div class="d-flex flex-wrap gap-sm">
                <span class="badge">99+</span>
                <span class="badge badge-success">新</span>
                <span class="badge badge-warning">热</span>
                <span class="badge badge-error">!</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 响应式断点 -->
        <div class="card mb-lg">
          <div class="card-header">
            <h2 class="card-title">响应式断点</h2>
          </div>
          <div class="card-body">
            <div class="grid grid-cols-1 grid-cols-sm-2 grid-cols-md-4 gap-md">
              <div class="card bg-secondary">
                <h4>320px</h4>
                <p class="text-sm text-secondary">移动设备</p>
              </div>
              <div class="card bg-secondary">
                <h4>768px</h4>
                <p class="text-sm text-secondary">平板</p>
              </div>
              <div class="card bg-secondary">
                <h4>1024px</h4>
                <p class="text-sm text-secondary">桌面</p>
              </div>
              <div class="card bg-secondary">
                <h4>1440px</h4>
                <p class="text-sm text-secondary">大屏幕</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 工具类示例 -->
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">工具类示例</h2>
          </div>
          <div class="card-body">
            <div class="d-flex justify-between align-center mb-md">
              <span class="font-semibold">Flex 布局</span>
              <el-button size="small">操作</el-button>
            </div>
            <div class="divider"></div>
            <div class="grid grid-cols-2 gap-md">
              <div class="text-center p-md border rounded">
                <p class="text-primary font-semibold">居中对齐</p>
              </div>
              <div class="text-right p-md border rounded">
                <p class="text-secondary">右对齐</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 这是一个纯展示组件，不需要逻辑
</script>

<style scoped>
.design-system-demo {
  background-color: var(--color-bg-secondary);
  min-height: 100vh;
  padding: var(--spacing-lg) 0;
}

.color-swatch {
  padding: var(--spacing-md);
  border-radius: var(--radius-base);
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-label {
  color: var(--color-text-inverse);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
}
</style>
