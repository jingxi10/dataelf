<template>
  <div class="category-tree">
    <el-tree
      :data="treeData"
      :props="treeProps"
      :default-expand-all="defaultExpandAll"
      :expand-on-click-node="false"
      node-key="id"
      :highlight-current="true"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <span class="node-label">
            <el-icon v-if="data.level === 1"><Folder /></el-icon>
            <el-icon v-else-if="data.level === 2"><FolderOpened /></el-icon>
            <el-icon v-else><Document /></el-icon>
            <span class="label-text">{{ node.label }}</span>
            <el-tag v-if="data.contentCount > 0" size="small" type="info">
              {{ data.contentCount }}
            </el-tag>
          </span>
          <span v-if="showActions" class="node-actions">
            <el-button
              v-if="data.level < 3"
              link
              type="primary"
              size="small"
              @click.stop="handleAdd(data)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click.stop="handleEdit(data)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click.stop="handleDelete(data)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Folder, FolderOpened, Document, Plus, Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['node-click', 'add', 'edit', 'delete'])

const treeProps = {
  children: 'children',
  label: 'name'
}

const treeData = computed(() => props.categories)

const handleNodeClick = (data) => {
  emit('node-click', data)
}

const handleAdd = (data) => {
  emit('add', data)
}

const handleEdit = (data) => {
  emit('edit', data)
}

const handleDelete = (data) => {
  emit('delete', data)
}
</script>

<style scoped>
.category-tree {
  width: 100%;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-text {
  font-weight: 500;
}

.node-actions {
  display: none;
  gap: 4px;
}

.custom-tree-node:hover .node-actions {
  display: flex;
}

:deep(.el-tree-node__content) {
  height: 36px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}
</style>
