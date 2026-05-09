<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="profile-card">
          <div class="profile-header text-center" style="padding: 20px 0;">
            <div class="avatar-wrapper" @click="triggerAvatarUpload">
              <el-avatar :size="80" class="user-avatar" :src="avatarUrl">
                <template v-if="!avatarUrl">{{ userInfo?.realName?.charAt(0) || 'U' }}</template>
              </el-avatar>
              <div class="avatar-overlay"><span>更换头像</span></div>
            </div>
            <input ref="avatarInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" style="display: none" @change="handleAvatarChange" />
            <h2 style="margin-top: 12px; font-size: 22px;">{{ userInfo?.realName || '用户' }}</h2>
            <p style="color: var(--text-secondary); margin-top: 4px;">{{ getRoleText(userInfo?.role || '') }}</p>
          </div>

          <el-divider />

          <div class="profile-stats" style="display: flex; justify-content: space-around; text-align: center;">
            <div><div style="font-size: 24px; font-weight: 700; color: var(--primary-color);">{{ profileStats.totalBorrows || 0 }}</div><div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">累计借阅</div></div>
            <div><div style="font-size: 24px; font-weight: 700; color: var(--primary-color);">{{ profileStats.activeBorrows || 0 }}</div><div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">当前借阅</div></div>
            <div><div style="font-size: 24px; font-weight: 700; color: var(--primary-color);">{{ profileStats.overdueBorrows || 0 }}</div><div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">逾期记录</div></div>
          </div>

          <el-divider />
          
          <div>
            <p style="padding: 8px 0; color: var(--text-regular); font-size: 14px;"><span style="color: var(--text-secondary); margin-right: 8px;">用户名:</span>{{ userInfo?.username }}</p>
            <p style="padding: 8px 0; color: var(--text-regular); font-size: 14px;"><span style="color: var(--text-secondary); margin-right: 8px;">手机号:</span>{{ userInfo?.phone || '未设置' }}</p>
            <p style="padding: 8px 0; color: var(--text-regular); font-size: 14px;"><span style="color: var(--text-secondary); margin-right: 8px;">邮箱:</span>{{ userInfo?.email || '未设置' }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-tabs type="border-card">
          <el-tab-pane label="基本信息">
            <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" size="default" style="max-width: 560px; padding-top: 16px;">
              <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="3-20位字母、数字或下划线" /></el-form-item>
              <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" placeholder="请输入真实姓名" /></el-form-item>
              <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
              <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" size="default" style="max-width: 480px; padding-top: 16px;">
              <el-form-item label="当前密码" prop="oldPassword"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
              <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
              <el-form-item><el-button type="primary" :loading="changingPwd" @click="handleChangePwd">确认修改</el-button></el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="我的借阅">
            <el-table v-loading="borrowsLoading" :data="myBorrows" stripe size="default">
              <el-table-column prop="bookTitle" label="图书名称" min-width="200" />
              <el-table-column prop="borrowDate" label="借阅日期" width="120"><template #default="{ row }">{{ row.borrowDate?.split('T')[0] }}</template></el-table-column>
              <el-table-column prop="dueDate" label="应还日期" width="120"><template #default="{ row }">{{ row.dueDate?.split('T')[0] }}</template></el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { getProfileStats, updateProfile, changePassword, uploadAvatar } from '@/api/profile'
import { getMyBorrows } from '@/api/borrow'
import { get as apiGet } from '@/api/request'
import type { BorrowRecord } from '@/types'

const userStore = useUserStore()
const avatarInput = ref<HTMLInputElement | null>(null)
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  let url = avatar.startsWith('/') ? avatar : '/' + avatar
  return url + '?t=' + Date.now()
})

onMounted(async () => {
  await userStore.initUserInfo()
  resetForm()
  fetchProfileStats()
  fetchMyBorrows()
})

const userInfo = computed(() => userStore.userInfo)

const saving = ref(false)
const changingPwd = ref(false)
const borrowsLoading = ref(false)
const pwdRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

const form = reactive({ username: '', realName: '', phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const profileStats = reactive({ totalBorrows: 0, activeBorrows: 0, overdueBorrows: 0 })
const myBorrows = ref<BorrowRecord[]>([])

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  realName: [
    { max: 20, message: '真实姓名不超过20个字符', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^$|1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码需包含字母和数字', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }, trigger: 'blur' }
  ],
}

async function fetchProfileStats() {
  try {
    Object.assign(profileStats, await getProfileStats())
  } catch {}
}

async function fetchMyBorrows() {
  borrowsLoading.value = true
  try {
    const res = await getMyBorrows({ page: 1, size: 10 })
    myBorrows.value = res?.records || []
  } catch {} finally { borrowsLoading.value = false }
}

function getRoleText(role: string): string {
  return { ADMIN: '系统管理员', LIBRARIAN: '图书管理员', READER: '读者' }[role] || role
}

function getStatusType(status: string): string {
  if (status === 'OVERDUE') return 'danger'
  if (status === 'RETURNED') return 'success'
  return ''
}

function getStatusText(status: string): string {
  return { BORROWING: '借阅中', RETURNED: '已归还', OVERDUE: '已逾期' }[status] || status
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) { ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 格式'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片大小不能超过5MB'); return }
  try {
    const newAvatarPath = await uploadAvatar(file)
    ElMessage.success('头像更新成功')
    if (userStore.userInfo && newAvatarPath) {
      userStore.setUserInfo({ ...userStore.userInfo, avatar: newAvatarPath })
    } else {
      await fetchFreshUserInfo()
    }
  } catch { ElMessage.error('头像上传失败') }
  input.value = ''
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await updateProfile({ username: form.username, realName: form.realName, phone: form.phone, email: form.email })
      ElMessage.success('个人信息已更新')
      await fetchFreshUserInfo()
    } catch {} finally { saving.value = false }
  })
}

function resetForm() {
  const info = userStore.userInfo
  Object.assign(form, { username: info?.username || '', realName: info?.realName || '', phone: info?.phone || '', email: info?.email || '' })
}

async function handleChangePwd() {
  if (!pwdRef.value) return
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      ElMessage.success('密码修改成功')
      Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    } catch {} finally { changingPwd.value = false }
  })
}

async function fetchFreshUserInfo() {
  try {
    const data = await apiGet('/auth/me') as any
    if (data) {
      userStore.setUserInfo({
        id: data.id,
        username: data.username,
        realName: data.realName || '',
        role: data.role,
        phone: data.phone || '',
        email: data.email || '',
        avatar: data.avatar || '',
      })
    }
  } catch {}
}
</script>

<style lang="scss" scoped>
.profile-page .user-avatar { background: #78716c; font-size: 32px; box-shadow: 0 4px 14px rgba(87,83,78,0.2); }
.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  .avatar-overlay {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
    span { color: #fff; font-size: 13px; font-weight: 500; }
  }
  &:hover .avatar-overlay { opacity: 1; }
}
</style>
