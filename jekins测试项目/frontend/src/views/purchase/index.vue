<template>
  <div class="purchase-management">
    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增采购单</el-button>
          <el-button type="success" @click="handleExport">导出Excel</el-button>
        </div>
        <div class="toolbar-right">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="审批中" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border size="default">
        <el-table-column prop="orderNo" label="采购单号" width="200" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">{{ statusMap[row.status]?.label || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT' || row.status === 'REJECTED'" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT' || row.status === 'REJECTED'" type="success" link size="small" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 'PENDING'" type="warning" link size="small" @click="handleApprove(row)">审批</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="success" link size="small" @click="handleInstock(row)">入库</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑采购单' : '新增采购单'" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期" prop="deadline">
              <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-divider>采购项目</el-divider>
        <div v-for="(item, idx) in form.items" :key="idx" style="margin-bottom: 12px; padding: 12px; border: 1px solid #ebeef5; border-radius: 4px">
          <el-row :gutter="12">
            <el-col :span="6"><el-form-item label="书名" :prop="`items.${idx}.bookTitle`" :rules="[{ required: true, message: '请输入书名', trigger: 'blur' }]"><el-input v-model="item.bookTitle" /></el-form-item></el-col>
            <el-col :span="4"><el-form-item label="作者"><el-input v-model="item.author" /></el-form-item></el-col>
            <el-col :span="4"><el-form-item label="ISBN"><el-input v-model="item.isbn" /></el-form-item></el-col>
            <el-col :span="4"><el-form-item label="分类"><el-select v-model="item.categoryId" placeholder="选择分类" clearable style="width: 100%"><el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item></el-col>
            <el-col :span="3"><el-form-item label="数量" :prop="`items.${idx}.quantity`" :rules="[{ required: true, message: '请输入数量', trigger: 'blur' }]"><el-input-number v-model="item.quantity" :min="1" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="3"><el-form-item label="单价"><el-input-number v-model="item.price" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="出版社"><el-input v-model="item.publisher" /></el-form-item></el-col>
            <el-col :span="14"><el-form-item label="备注"><el-input v-model="item.remark" /></el-form-item></el-col>
            <el-col :span="2"><el-button type="danger" link @click="form.items.splice(idx, 1)">删除</el-button></el-col>
          </el-row>
        </div>
        <el-button type="primary" link @click="addItem">+ 添加采购项目</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="采购单详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="采购单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusMap[currentOrder.status]?.type">{{ statusMap[currentOrder.status]?.label }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentOrder.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ currentOrder.deadline }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="currentOrder?.items?.length" :data="currentOrder.items" border size="small" style="margin-top: 16px">
        <el-table-column prop="bookTitle" label="书名" min-width="140" />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="quantity" label="采购数量" width="90" align="center" />
        <el-table-column prop="instockQuantity" label="已入库" width="80" align="center" />
        <el-table-column prop="price" label="单价" width="80" align="center" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="approveDialogVisible" title="审批采购单" width="460px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-radio-group v-model="approveAction">
            <el-radio value="APPROVE">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="approveComment" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDoApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseOrders, getPurchaseOrderById, createPurchaseOrder, updatePurchaseOrder, deletePurchaseOrder, submitPurchaseOrder, approvePurchaseOrder, quickInstock } from '@/api/purchase'
import { getDeptList, type Dept } from '@/api/dept'
import { getCategoryList, type BookCategory } from '@/api/category'
import { exportPurchasesExcel, downloadBlob } from '@/api/export'

const statusMap: Record<string, { label: string; type: string }> = {
  DRAFT: { label: '草稿', type: 'info' },
  PENDING: { label: '审批中', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' },
  COMPLETED: { label: '已完成', type: 'success' }
}

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref('')

const deptList = ref<Dept[]>([])
const categoryList = ref<BookCategory[]>([])

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const form = reactive({ deptId: null as number | null, deadline: '', remark: '', items: [] as any[] })
const rules = {}

const detailVisible = ref(false)
const currentOrder = ref<any>(null)

const approveDialogVisible = ref(false)
const approveOrderId = ref<number>(0)
const approveAction = ref('APPROVE')
const approveComment = ref('')

onMounted(() => {
  fetchData()
  fetchDepts()
  fetchCategories()
})

async function fetchDepts() {
  try { deptList.value = await getDeptList() || [] } catch {}
}

async function fetchCategories() {
  try { categoryList.value = await getCategoryList() || [] } catch {}
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPurchaseOrders({ page: page.value, size: size.value, status: statusFilter.value })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function addItem() {
  form.items.push({ bookTitle: '', author: '', isbn: '', publisher: '', categoryId: null, quantity: 1, price: 0, remark: '' })
}

function handleAdd() {
  editId.value = null
  Object.assign(form, { deptId: null, deadline: '', remark: '', items: [] })
  addItem()
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  editId.value = row.id
  try {
    const order = await getPurchaseOrderById(row.id)
    Object.assign(form, { deptId: order.deptId, deadline: order.deadline, remark: order.remark, items: order.items || [] })
    dialogVisible.value = true
  } catch (e: any) { ElMessage.error(e.message || '获取详情失败') }
}

async function handleSubmitForm() {
  await formRef.value?.validate()
  try {
    const data = { ...form, id: editId.value }
    if (editId.value) { await updatePurchaseOrder(data); ElMessage.success('更新成功') }
    else { await createPurchaseOrder(data); ElMessage.success('创建成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleView(row: any) {
  try {
    currentOrder.value = await getPurchaseOrderById(row.id)
    detailVisible.value = true
  } catch (e: any) { ElMessage.error(e.message || '获取详情失败') }
}

async function handleSubmit(row: any) {
  try {
    await ElMessageBox.confirm('确定要提交审批吗？', '提示', { type: 'warning' })
    await submitPurchaseOrder(row.id)
    ElMessage.success('已提交审批')
    fetchData()
  } catch {}
}

function handleApprove(row: any) {
  approveOrderId.value = row.id
  approveAction.value = 'APPROVE'
  approveComment.value = ''
  approveDialogVisible.value = true
}

async function handleDoApprove() {
  try {
    await approvePurchaseOrder(approveOrderId.value, { action: approveAction.value, comment: approveComment.value })
    ElMessage.success(approveAction.value === 'APPROVE' ? '已通过' : '已驳回')
    approveDialogVisible.value = false
    fetchData()
  } catch (e: any) { ElMessage.error(e.message || '审批失败') }
}

async function handleInstock(row: any) {
  try {
    await ElMessageBox.confirm('确定要执行快速入库吗？将自动创建图书记录并增加库存。', '提示', { type: 'warning' })
    await quickInstock(row.id)
    ElMessage.success('入库成功')
    fetchData()
  } catch {}
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定要删除该采购单吗？', '提示', { type: 'warning' })
    await deletePurchaseOrder(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleExport() {
  try {
    if (total.value === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    const res = await exportPurchasesExcel({ status: statusFilter.value }) as any
    if (res) {
      downloadBlob(res, '采购单列表.xlsx')
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}
</script>

<style lang="scss" scoped>
.purchase-management {
  .table-toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
  .pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
