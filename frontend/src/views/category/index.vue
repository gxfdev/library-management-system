<template>
  <div class="category-management">
    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left"><el-button type="primary" @click="handleAdd(null)">新增分类</el-button></div>
      </div>

      <el-table v-loading="loading" :data="categoryList" row-key="id" border default-expand-all :tree-props="{ children: 'children' }" size="default">
        <el-table-column prop="name" label="分类名称" min-width="200">
          <template #default="{ row }">
            <span :style="{ paddingLeft: ((row.level - 1) * 20) + 'px' }">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80" align="center">
          <template #default="{ row }">第{{ row.level }}级</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="(val: number) => ElMessage.success(val ? '已启用' : '已禁用')" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAdd(row)">添加子分类</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :disabled="!!row.children?.length" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <CategoryDialog v-model:visible="dialogVisible" :category-id="editCategoryId" :parent-id="parentId" @success="fetchCategories" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CategoryDialog from './components/CategoryDialog.vue'
import { getCategoryList, deleteCategory } from '@/api/category'
import type { BookCategory } from '@/types'

const loading = ref(false)
const categoryList = ref<BookCategory[]>([])
const dialogVisible = ref(false)
const editCategoryId = ref<number | null>(null)
const parentId = ref<number | undefined>(undefined)

onMounted(() => fetchCategories())

async function fetchCategories() {
  loading.value = true
  try {
    const flatList = await getCategoryList() || []
    const map = new Map<number, BookCategory>()
    const tree: BookCategory[] = []
    flatList.forEach((item: BookCategory) => {
      item.children = []
      map.set(item.id, item)
    })
    flatList.forEach((item: BookCategory) => {
      if (item.parentId && item.parentId > 0 && map.has(item.parentId)) {
        map.get(item.parentId).children.push(item)
      } else {
        tree.push(item)
      }
    })
    categoryList.value = tree
  } catch {
    categoryList.value = []
  } finally { loading.value = false }
}

function handleAdd(parent?: BookCategory | null) {
  editCategoryId.value = null
  parentId.value = parent ? parent.id : 0
  dialogVisible.value = true
}

function handleEdit(row: BookCategory) {
  editCategoryId.value = row.id
  parentId.value = undefined
  dialogVisible.value = true
}

async function handleDelete(row: BookCategory) {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', { type: 'warning' })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch {}
}
</script>

<style lang="scss" scoped>
.category-management .toolbar-left { display: flex; gap: 8px; }
</style>
