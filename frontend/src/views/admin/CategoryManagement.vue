<template>
  <div class="category-management">
    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="never">
      <div class="operation-bar">
        <div class="left-actions">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建根分类
          </el-button>
          <el-button @click="loadCategories">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="right-actions">
          <el-switch
            v-model="expandAll"
            active-text="展开全部"
            inactive-text="收起全部"
          />
        </div>
      </div>
    </el-card>

    <!-- 分类树 -->
    <el-card class="tree-card" shadow="never" v-loading="loading">
      <CategoryTree
        :categories="categories"
        :default-expand-all="expandAll"
        :show-actions="true"
        @node-click="handleNodeClick"
        @add="handleAddChild"
        @edit="handleEdit"
        @delete="handleDelete"
      />

      <el-empty v-if="!loading && categories.length === 0" description="暂无分类，点击上方按钮创建">
        <el-button type="primary" @click="handleCreate">创建分类</el-button>
      </el-empty>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="父分类" v-if="!isEdit">
          <el-cascader
            v-model="form.parentId"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="选择父分类（不选则为根分类）"
            clearable
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="当前层级" v-if="isEdit">
          <el-tag>第 {{ form.level }} 级</el-tag>
          <span class="level-tip">（编辑时不能修改层级）</span>
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 分类详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="分类详情"
      size="400px"
    >
      <div v-if="selectedCategory" class="category-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="分类名称">
            {{ selectedCategory.name }}
          </el-descriptions-item>
          <el-descriptions-item label="层级">
            第 {{ selectedCategory.level }} 级
          </el-descriptions-item>
          <el-descriptions-item label="排序">
            {{ selectedCategory.sortOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="内容数量">
            {{ selectedCategory.contentCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="子分类数量">
            {{ selectedCategory.children?.length || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedCategory.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" v-if="selectedCategory.description">
            {{ selectedCategory.description }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button type="primary" @click="handleEdit(selectedCategory)">
            编辑
          </el-button>
          <el-button
            v-if="selectedCategory.level < 3"
            @click="handleAddChild(selectedCategory)"
          >
            添加子分类
          </el-button>
          <el-button type="danger" @click="handleDelete(selectedCategory)">
            删除
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import CategoryTree from '@/components/CategoryTree.vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

const loading = ref(false)
const categories = ref([])
const expandAll = ref(false)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const selectedCategory = ref(null)

const formRef = ref(null)
const form = ref({
  name: '',
  parentId: null,
  description: '',
  sortOrder: 0,
  level: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  if (isEdit.value) {
    return '编辑分类'
  }
  return form.value.parentId ? '创建子分类' : '创建根分类'
})

const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true,
  emitPath: false
}

const categoryOptions = computed(() => {
  // 过滤掉第3级分类（不能再添加子分类）
  const filterLevel3 = (cats) => {
    return cats.filter(cat => cat.level < 3).map(cat => ({
      ...cat,
      children: cat.children ? filterLevel3(cat.children) : []
    }))
  }
  return filterLevel3(categories.value)
})

const loadCategories = async () => {
  loading.value = true
  try {
    const response = await getCategoryTree()
    // axios拦截器已经解包
    categories.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('Failed to load categories:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  form.value = {
    name: '',
    parentId: null,
    description: '',
    sortOrder: 0,
    level: 1
  }
  dialogVisible.value = true
}

const handleAddChild = (parent) => {
  isEdit.value = false
  form.value = {
    name: '',
    parentId: parent.id,
    description: '',
    sortOrder: 0,
    level: parent.level + 1
  }
  dialogVisible.value = true
}

const handleEdit = (category) => {
  isEdit.value = true
  form.value = {
    id: category.id,
    name: category.name,
    parentId: category.parentId,
    description: category.description || '',
    sortOrder: category.sortOrder || 0,
    level: category.level
  }
  dialogVisible.value = true
  drawerVisible.value = false
}

const handleDelete = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCategory(category.id)
    ElMessage.success('分类删除成功')
    await loadCategories()
    drawerVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete category:', error)
      const message = error.response?.data?.error?.message || '删除失败'
      ElMessage.error(message)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await updateCategory(form.value.id, {
          name: form.value.name,
          description: form.value.description,
          sortOrder: form.value.sortOrder
        })
        ElMessage.success('分类更新成功')
      } else {
        await createCategory({
          name: form.value.name,
          parentId: form.value.parentId,
          description: form.value.description,
          sortOrder: form.value.sortOrder
        })
        ElMessage.success('分类创建成功')
      }

      dialogVisible.value = false
      await loadCategories()
    } catch (error) {
      console.error('Failed to save category:', error)
      const message = error.response?.data?.error?.message || '保存失败'
      ElMessage.error(message)
    } finally {
      submitting.value = false
    }
  })
}

const handleNodeClick = (category) => {
  selectedCategory.value = category
  drawerVisible.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 0;
}

.operation-card {
  margin-bottom: 20px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-actions {
  display: flex;
  gap: 10px;
}

.right-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.tree-card {
  min-height: 400px;
}

.level-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.category-detail {
  padding: 10px 0;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
