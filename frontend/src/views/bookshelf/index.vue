<template>
  <div class="bookshelf-management">
    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增书架</el-button>
        </div>
        <div class="toolbar-right">
          <el-input v-model="keyword" placeholder="搜索书架名称/编号" clearable style="width: 200px" @clear="fetchData" @keyup.enter="fetchData">
            <template #append><el-button @click="fetchData"><el-icon><Search /></el-icon></el-button></template>
          </el-input>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border size="default">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="code" label="书架编号" width="120" />
        <el-table-column prop="name" label="书架名称" min-width="160" />
        <el-table-column prop="deptName" label="所属部门" width="120" />
        <el-table-column prop="location" label="位置描述" min-width="140" show-overflow-tooltip />
        <el-table-column prop="capacity" label="容量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewStoreys(row)">层管理</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑书架' : '新增书架'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="书架编号" prop="code">
          <el-input v-model="form.code" placeholder="如: A-001" />
        </el-form-item>
        <el-form-item label="书架名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入书架名称" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置描述" prop="location">
          <el-input v-model="form.location" placeholder="如: 一楼东侧" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="storeyDialogVisible" :title="`书架层管理 - ${currentBookshelf?.name || ''}`" width="800px" destroy-on-close>
      <div style="margin-bottom: 12px">
        <el-button type="primary" size="small" @click="handleAddStorey">新增书架层</el-button>
      </div>
      <el-table :data="storeyList" border size="small">
        <el-table-column prop="levelNum" label="层号" width="70" align="center" />
        <el-table-column prop="name" label="层名称" min-width="120" />
        <el-table-column prop="capacity" label="容量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库位数" width="80" align="center">
          <template #default="{ row }">{{ row.locations?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewLocations(row)">库位</el-button>
            <el-button type="primary" link size="small" @click="handleEditStorey(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteStorey(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="storeyFormVisible" :title="storeyEditId ? '编辑书架层' : '新增书架层'" width="460px" destroy-on-close>
      <el-form ref="storeyFormRef" :model="storeyForm" :rules="storeyRules" label-width="80px">
        <el-form-item label="层号" prop="levelNum">
          <el-input-number v-model="storeyForm.levelNum" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="层名称" prop="name">
          <el-input v-model="storeyForm.name" placeholder="如: 第1层" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="storeyForm.capacity" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storeyFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitStorey">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="locationDialogVisible" :title="`库位管理 - ${currentStorey?.name || ''}`" width="700px" destroy-on-close>
      <div style="margin-bottom: 12px">
        <el-button type="primary" size="small" @click="handleAddLocation">新增库位</el-button>
      </div>
      <el-table :data="currentStorey?.locations || []" border size="small">
        <el-table-column prop="code" label="库位编号" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'EMPTY' ? 'info' : 'success'" size="small">{{ row.status === 'EMPTY' ? '空闲' : '占用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookTitle" label="图书" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'EMPTY'" type="primary" link size="small" @click="handleAssignBook(row)">分配图书</el-button>
            <el-button v-if="row.status === 'OCCUPIED'" type="warning" link size="small" @click="handleClearLocation(row)">清空</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="locationFormVisible" title="新增库位" width="400px" destroy-on-close>
      <el-form ref="locationFormRef" :model="locationForm" :rules="locationRules" label-width="80px">
        <el-form-item label="库位编号" prop="code">
          <el-input v-model="locationForm.code" placeholder="如: A-001-1-01" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="locationFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitLocation">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配图书到库位" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="图书ID">
          <el-input-number v-model="assignBookId" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDoAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getBookshelves, createBookshelf, updateBookshelf, deleteBookshelf, getStoreys, createStorey, updateStorey, deleteStorey, createLocation, assignBookToLocation, clearLocation } from '@/api/bookshelf'
import { getDeptList } from '@/api/dept'

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const form = reactive({ code: '', name: '', deptId: null as number | null, location: '', capacity: 50, status: 1 })
const rules = { code: [{ required: true, message: '请输入书架编号', trigger: 'blur' }], name: [{ required: true, message: '请输入书架名称', trigger: 'blur' }] }

const storeyDialogVisible = ref(false)
const storeyFormVisible = ref(false)
const currentBookshelf = ref<any>(null)
const storeyList = ref<any[]>([])
const storeyEditId = ref<number | null>(null)
const storeyFormRef = ref()
const storeyForm = reactive({ bookshelfId: 0, levelNum: 1, name: '', capacity: 20, status: 1 })
const storeyRules = { levelNum: [{ required: true, message: '请输入层号', trigger: 'blur' }], name: [{ required: true, message: '请输入层名称', trigger: 'blur' }] }

const locationDialogVisible = ref(false)
const locationFormVisible = ref(false)
const currentStorey = ref<any>(null)
const locationFormRef = ref()
const locationForm = reactive({ storeyId: 0, code: '', status: 'EMPTY' })
const locationRules = { code: [{ required: true, message: '请输入库位编号', trigger: 'blur' }] }

const assignDialogVisible = ref(false)
const assignLocationId = ref<number>(0)
const assignBookId = ref<number>(0)
const deptList = ref<any[]>([])

onMounted(() => { fetchData(); fetchDeptList() })

async function fetchDeptList() {
  try { deptList.value = await getDeptList() || [] } catch { deptList.value = [] }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getBookshelves({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  editId.value = null
  Object.assign(form, { code: '', name: '', deptId: null, location: '', capacity: 50, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  try {
    const data = { ...form, id: editId.value }
    if (editId.value) { await updateBookshelf(data); ElMessage.success('更新成功') }
    else { await createBookshelf(data); ElMessage.success('创建成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除书架"${row.name}"吗？`, '提示', { type: 'warning' })
    await deleteBookshelf(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleViewStoreys(row: any) {
  currentBookshelf.value = row
  storeyDialogVisible.value = true
  await fetchStoreys(row.id)
}

async function fetchStoreys(bookshelfId: number) {
  try {
    storeyList.value = await getStoreys(bookshelfId) || []
  } catch { storeyList.value = [] }
}

function handleAddStorey() {
  storeyEditId.value = null
  Object.assign(storeyForm, { bookshelfId: currentBookshelf.value.id, levelNum: storeyList.value.length + 1, name: `第${storeyList.value.length + 1}层`, capacity: 20, status: 1 })
  storeyFormVisible.value = true
}

function handleEditStorey(row: any) {
  storeyEditId.value = row.id
  Object.assign(storeyForm, row)
  storeyFormVisible.value = true
}

async function handleSubmitStorey() {
  await storeyFormRef.value?.validate()
  try {
    const data = { ...storeyForm, id: storeyEditId.value }
    if (storeyEditId.value) { await updateStorey(data); ElMessage.success('更新成功') }
    else { await createStorey(data); ElMessage.success('创建成功') }
    storeyFormVisible.value = false
    fetchStoreys(currentBookshelf.value.id)
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleDeleteStorey(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.name}"吗？`, '提示', { type: 'warning' })
    await deleteStorey(row.id)
    ElMessage.success('删除成功')
    fetchStoreys(currentBookshelf.value.id)
  } catch {}
}

function handleViewLocations(row: any) {
  currentStorey.value = row
  locationDialogVisible.value = true
}

function handleAddLocation() {
  Object.assign(locationForm, { storeyId: currentStorey.value.id, code: '', status: 'EMPTY' })
  locationFormVisible.value = true
}

async function handleSubmitLocation() {
  await locationFormRef.value?.validate()
  try {
    await createLocation(locationForm)
    ElMessage.success('创建成功')
    locationFormVisible.value = false
    fetchStoreys(currentBookshelf.value.id)
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

function handleAssignBook(row: any) {
  assignLocationId.value = row.id
  assignBookId.value = 0
  assignDialogVisible.value = true
}

async function handleDoAssign() {
  try {
    await assignBookToLocation(assignLocationId.value, assignBookId.value)
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    fetchStoreys(currentBookshelf.value.id)
  } catch (e: any) { ElMessage.error(e.message || '分配失败') }
}

async function handleClearLocation(row: any) {
  try {
    await ElMessageBox.confirm('确定要清空该库位吗？', '提示', { type: 'warning' })
    await clearLocation(row.id)
    ElMessage.success('清空成功')
    fetchStoreys(currentBookshelf.value.id)
  } catch {}
}
</script>

<style lang="scss" scoped>
.bookshelf-management {
  .table-toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
  .pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
