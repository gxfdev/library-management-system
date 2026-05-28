<template>
  <div class="error-page">
    <div class="error-content animate-scale-in">
      <div class="error-illustration">
        <svg width="160" height="120" viewBox="0 0 160 120" fill="none">
          <rect x="20" y="30" width="120" height="80" rx="8" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="2"/>
          <rect x="30" y="45" width="100" height="6" rx="3" fill="#cbd5e0"/>
          <rect x="30" y="60" width="70" height="6" rx="3" fill="#e2e8f0"/>
          <rect x="30" y="75" width="85" height="6" rx="3" fill="#e2e8f0"/>
          <circle cx="120" cy="50" r="22" fill="#fff5f5" stroke="#fed7d7" stroke-width="2"/>
          <path d="M112 48l4 4 8-8" stroke="#e53e3e" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1>访问受限</h1>
      <p>抱歉，您没有权限访问该页面</p>
      <div class="action-buttons">
        <el-button type="primary" round @click="goHome">返回首页</el-button>
        <el-button round @click="goLogin">重新登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

function goHome() {
  const role = userStore.userInfo?.role
  if (role === 'ADMIN' || role === 'LIBRARIAN') {
    router.push('/dashboard')
  } else if (role === 'READER') {
    router.push('/book-catalog')
  } else {
    router.push('/login')
  }
}

function goLogin() {
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #edf2f7;

  .error-content {
    text-align: center;
    padding: 40px;

    .error-illustration {
      margin-bottom: 24px;
    }

    h1 { font-size: 22px; font-weight: 700; color: #1a202c; margin-bottom: 8px; }
    p { font-size: 14px; color: #a0aec0; }

    .action-buttons {
      margin-top: 28px;
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }
}
</style>
