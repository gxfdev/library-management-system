<template>
  <div class="permission-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="权限矩阵" name="matrix">
        <div class="card-container">
          <div class="section-header">
            <h3>角色权限对照表</h3>
            <p class="sub-text">系统各角色对应的功能模块访问权限</p>
          </div>
          <el-table :data="matrixData" border stripe style="width: 100%">
            <el-table-column prop="name" label="功能模块" width="160" />
            <el-table-column label="管理员 (ADMIN)" width="160" align="center">
              <template #default="{ row }">
                <el-tag :type="row.ADMIN ? 'success' : 'info'" size="small">
                  {{ row.ADMIN ? '✓ 允许' : '✗ 禁止' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="图书管理员 (LIBRARIAN)" width="200" align="center">
              <template #default="{ row }">
                <el-tag :type="row.LIBRARIAN ? 'success' : 'info'" size="small">
                  {{ row.LIBRARIAN ? '✓ 允许' : '✗ 禁止' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="读者 (READER)" width="160" align="center">
              <template #default="{ row }">
                <el-tag :type="row.READER ? 'success' : 'info'" size="small">
                  {{ row.READER ? '✓ 允许' : '✗ 禁止' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="perm-note">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                管理员(ADMIN)具备所有权限，包括借阅图书权限。图书管理员(LIBRARIAN)可管理图书和借阅，但不具备用户管理权限。读者(READER)仅可自助借阅、归还和续借。
              </template>
            </el-alert>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="变更记录" name="logs">
        <div class="card-container">
          <div class="section-header">
            <div class="header-row">
              <h3>权限变更记录</h3>
              <el-input
                v-model="logKeyword"
                placeholder="搜索操作人/目标用户"
                style="width: 240px"
                clearable
                @clear="loadLogs"
                @keyup.enter="loadLogs"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
          </div>
          <el-table :data="logData" border stripe v-loading="logLoading">
            <el-table-column prop="createTime" label="时间" width="180" />
            <el-table-column prop="operatorName" label="操作人" width="120" />
            <el-table-column prop="targetUserName" label="目标用户" width="120" />
            <el-table-column prop="action" label="操作类型" width="140">
              <template #default="{ row }">
                <el-tag :type="actionTagType(row.action)" size="small">{{ actionLabel(row.action) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="oldRole" label="原角色" width="120">
              <template #default="{ row }">
                <span v-if="row.oldRole">{{ roleLabel(row.oldRole) }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="newRole" label="新角色" width="120">
              <template #default="{ row }">
                <span v-if="row.newRole">{{ roleLabel(row.newRole) }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="详情" min-width="200" />
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="logPage"
              v-model:page-size="logSize"
              :total="logTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadLogs"
              @size-change="loadLogs"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getPermissionMatrix, getPermissionLogs } from '@/api/permission'

const activeTab = ref('matrix')
const matrixData = ref<any[]>([])

const logData = ref<any[]>([])
const logLoading = ref(false)
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)
const logKeyword = ref('')

onMounted(() => {
  loadMatrix()
  loadLogs()
})

async function loadMatrix() {
  try {
    matrixData.value = await getPermissionMatrix() || []
  } catch {}
}

async function loadLogs() {
  logLoading.value = true
  try {
    const res = await getPermissionLogs({ page: logPage.value, size: logSize.value, keyword: logKeyword.value || undefined })
    logData.value = res?.records || []
    logTotal.value = res?.total || 0
  } catch {} finally {
    logLoading.value = false
  }
}

function actionLabel(action: string) {
  const map: Record<string, string> = {
    CREATE_USER: '创建用户',
    UPDATE_USER: '更新用户',
    CHANGE_ROLE: '角色变更',
    DELETE_USER: '删除用户',
    ENABLE_USER: '启用用户',
    DISABLE_USER: '禁用用户',
    RESET_PASSWORD: '重置密码',
  }
  return map[action] || action
}

function actionTagType(action: string) {
  if (action === 'CHANGE_ROLE') return 'warning'
  if (action === 'DELETE_USER' || action === 'DISABLE_USER') return 'danger'
  if (action === 'CREATE_USER' || action === 'ENABLE_USER') return 'success'
  return 'info'
}

function roleLabel(role: string) {
  const map: Record<string, string> = { ADMIN: '管理员', LIBRARIAN: '图书管理员', READER: '读者' }
  return map[role] || role
}
</script>

<style lang="scss" scoped>
.permission-page {
  .section-header {
    margin-bottom: 16px;
    h3 { margin: 0 0 4px; font-size: 16px; }
    .sub-text { color: #8c8c8c; font-size: 13px; margin: 0; }
    .header-row { display: flex; align-items: center; justify-content: space-between; }
  }
  .perm-note { margin-top: 16px; }
  .pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
  .text-muted { color: var(--text-placeholder); }
}
</style>
