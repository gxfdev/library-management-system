<template>
  <div class="user-management">
    <div class="search-form">
      <el-form :model="queryParams" inline size="default">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="用户名/姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="全部角色" clearable style="width: 140px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="图书管理员" value="LIBRARIAN" />
            <el-option label="读者" value="READER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
        <div class="toolbar-right">
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="userList" stripe border size="default">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime?.split('T')[0] || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="info" link size="small" @click="handleChangePwd(row)">修改密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchUserList"
        />
      </div>
    </div>

    <UserDialog
      v-model:visible="dialogVisible"
      :user-id="editUserId"
      @success="fetchUserList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserDialog from './components/UserDialog.vue'
import { getUserPage, deleteUser, updateUserStatus, resetUserPassword, adminChangePassword } from '@/api/user'
import type { User } from '@/types'

const loading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editUserId = ref<number | null>(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  role: undefined as string | undefined,
  status: undefined as number | undefined,
})

onMounted(() => fetchUserList())

async function fetchUserList() {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res?.records || []
    total.value = res?.total || 0
  } catch {
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchUserList()
}

function resetQuery() {
  Object.assign(queryParams, { page: 1, keyword: '', role: undefined, status: undefined })
  fetchUserList()
}

function handleAdd() {
  editUserId.value = null
  dialogVisible.value = true
}

function handleEdit(row: User) {
  editUserId.value = row.id
  dialogVisible.value = true
}

async function handleStatusChange(row: User, status: number) {
  try {
    await ElMessageBox.confirm(`确定要${status ? '启用' : '禁用'}该用户吗？`, '提示')
    await updateUserStatus(row.id, status)
    ElMessage.success(status ? '已启用' : '已禁用')
  } catch {
    row.status = status ? 0 : 1
  }
}

async function handleResetPwd(row: User) {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.realName}"的密码吗？系统将生成随机密码。`, '确认重置密码')
    const res = await resetUserPassword(row.id)
    const newPassword = res?.password || '未知'
    ElMessageBox.alert(
      `用户"${row.realName}"的密码已重置为：\n\n${newPassword}\n\n请通知用户尽快登录并修改密码。`,
      '密码重置成功',
      { confirmButtonText: '我知道了', type: 'success' }
    )
  } catch {}
}

async function handleChangePwd(row: User) {
  try {
    const { value: newPassword } = await ElMessageBox.prompt(
      `为用户"${row.realName}"设置新密码（至少6位，需包含字母和数字）`,
      '修改用户密码',
      {
        confirmButtonText: '确认修改',
        cancelButtonText: '取消',
        inputType: 'password',
        inputPattern: /^(?=.*[a-zA-Z])(?=.*\d).{6,}$/,
        inputErrorMessage: '密码至少6位，需包含字母和数字',
      }
    )
    if (newPassword) {
      await adminChangePassword(row.id, newPassword)
      ElMessage.success('密码修改成功')
    }
  } catch {}
}

async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.realName}"吗？此操作不可恢复！`, '警告', { type: 'error' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  } catch {}
}

function getRoleType(role: string): string {
  return { ADMIN: 'danger', LIBRARIAN: 'warning', READER: '' }[role] || ''
}

function getRoleText(role: string): string {
  return { ADMIN: '管理员', LIBRARIAN: '图书管理员', READER: '读者' }[role] || role
}
</script>

<style lang="scss" scoped>
.user-management .toolbar-left { display: flex; gap: 8px; }
</style>
