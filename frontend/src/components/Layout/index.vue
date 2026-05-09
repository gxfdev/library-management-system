<template>
  <div class="app-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-inner">
        <div class="sidebar-logo" @click="router.push('/dashboard')">
          <div class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M21 4H3a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1ZM4 18V6h7v12H4Zm9 0V6h7v12h-7Z" fill="#fff"/>
              <path d="M6 9h3v2H6V9ZM6 13h3v2H6v-2ZM15 9h3v2h-3V9ZM15 13h3v2h-3v-2Z" fill="#fff" opacity="0.55"/>
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!isCollapsed" class="logo-text">智慧图书馆</span>
          </transition>
        </div>

        <nav class="sidebar-nav">
          <div class="nav-group">
            <div class="nav-group-title" v-if="!isCollapsed">导航菜单</div>
            <router-link to="/dashboard" class="nav-item" :class="{ active: route.path === '/dashboard' }">
              <el-icon><Odometer /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">首页看板</span></transition>
              <span v-if="!isCollapsed" class="nav-badge home-badge"></span>
            </router-link>

            <router-link to="/book-catalog" class="nav-item" :class="{ active: route.path === '/book-catalog' }">
              <el-icon><Reading /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">图书借阅</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/books" class="nav-item" :class="{ active: route.path === '/books' }">
              <el-icon><Notebook /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">图书管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin" to="/users" class="nav-item" :class="{ active: route.path === '/users' }">
              <el-icon><User /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">用户管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/categories" class="nav-item" :class="{ active: route.path === '/categories' }">
              <el-icon><Menu /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">分类管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/borrows" class="nav-item" :class="{ active: route.path === '/borrows' }">
              <el-icon><Tickets /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">借阅管理</span></transition>
            </router-link>

            <router-link v-if="isReader" to="/my-borrows" class="nav-item" :class="{ active: route.path === '/my-borrows' }">
              <el-icon><DocumentChecked /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">我的借阅</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/inventory" class="nav-item" :class="{ active: route.path === '/inventory' }">
              <el-icon><Box /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">库存管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/statistics" class="nav-item" :class="{ active: route.path === '/statistics' }">
              <el-icon><DataAnalysis /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">统计报表</span></transition>
            </router-link>

            <div class="nav-group-title" v-if="!isCollapsed && (isAdmin || isLibrarian)">扩展功能</div>

            <router-link v-if="isAdmin || isLibrarian" to="/publishers" class="nav-item" :class="{ active: route.path === '/publishers' }">
              <el-icon><OfficeBuilding /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">出版社管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/bookshelves" class="nav-item" :class="{ active: route.path === '/bookshelves' }">
              <el-icon><Grid /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">书架管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/purchases" class="nav-item" :class="{ active: route.path === '/purchases' }">
              <el-icon><ShoppingCart /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">采购管理</span></transition>
            </router-link>

            <router-link v-if="isAdmin || isLibrarian" to="/book-normalize" class="nav-item" :class="{ active: route.path === '/book-normalize' }">
              <el-icon><EditPen /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">图书规范化</span></transition>
            </router-link>

            <router-link to="/notices" class="nav-item" :class="{ active: route.path === '/notices' }">
              <el-icon><Bell /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">通知公告</span></transition>
            </router-link>

            <router-link to="/borrow-code" class="nav-item" :class="{ active: route.path === '/borrow-code' }">
              <el-icon><Key /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">借阅码</span></transition>
            </router-link>

            <router-link v-if="isAdmin" to="/permissions" class="nav-item" :class="{ active: route.path === '/permissions' }">
              <el-icon><Lock /></el-icon>
              <transition name="fade"><span v-if="!isCollapsed">权限管理</span></transition>
            </router-link>
          </div>
        </nav>

        <div class="sidebar-footer" v-if="!isCollapsed">
          <div class="footer-info">
            <div class="version-tag">v2.0</div>
            <span>Library System</span>
          </div>
        </div>
      </div>
    </aside>

    <div class="main-area">
      <header class="top-header">
        <div class="header-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon :size="18"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title && route.path !== '/dashboard'">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-block">
              <el-avatar :size="36" class="header-avatar" :src="avatarUrl">
                {{ userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-meta">
                <span class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</span>
                <span class="user-role">{{ roleLabel }}{{ userInfo?.phone ? ' · ' + userInfo.phone : '' }}</span>
              </div>
              <el-icon style="color:#95a5a6;font-size:14px;"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Odometer, User, Reading, Menu, Tickets, DocumentChecked,
  Box, DataAnalysis, UserFilled, ArrowDown, SwitchButton,
  Fold, Expand, OfficeBuilding, Grid, ShoppingCart, Bell, Key, Lock, EditPen, Notebook,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)

onMounted(() => {
  userStore.initUserInfo()
})

const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin)
const isLibrarian = computed(() => userStore.isLibrarian)
const isReader = computed(() => userStore.isReader)

const roleLabel = computed(() => {
  const role = userInfo.value?.role
  if (role === 'ADMIN') return '管理员'
  if (role === 'LIBRARIAN') return '图书管理员'
  if (role === 'READER') return '读者'
  return '未知'
})

const avatarUrl = computed(() => {
  const avatar = userInfo.value?.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (!avatar.startsWith('/')) return '/' + avatar
  return avatar
})

async function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch {}
      break
  }
}
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  display: flex;
  flex-direction: column;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 4px 0 24px rgba(0,0,0,0.08);

  &.collapsed {
    width: 72px;

    .sidebar-footer { opacity: 0; pointer-events: none; }
  }

  .sidebar-inner {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .sidebar-logo {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    gap: 12px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255,255,255,0.08);
    transition: background 0.25s;
    flex-shrink: 0;

    &:hover { background: rgba(255,255,255,0.04); }

    .logo-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #16a085 0%, #1abc9c 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 4px 14px rgba(22,160,133,0.3);
    }

    .logo-text {
      font-size: 16px;
      font-weight: 700;
      color: #f8f9fa;
      white-space: nowrap;
      letter-spacing: 0.8px;
    }
  }

  .sidebar-nav {
    flex: 1;
    padding: 10px 0;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar { width: 0; }

    .nav-group-title {
      padding: 20px 22px 10px;
      font-size: 11px;
      color: rgba(255,255,255,0.35);
      text-transform: uppercase;
      letter-spacing: 2.5px;
      white-space: nowrap;
      font-weight: 600;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0 20px;
      height: 48px;
      margin: 2px 12px;
      border-radius: 12px;
      color: rgba(236,240,241,0.7);
      text-decoration: none;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      white-space: nowrap;
      position: relative;

      .el-icon {
        font-size: 19px;
        flex-shrink: 0;
        transition: transform 0.25s ease;
      }

      span:not(.nav-badge) {
        font-size: 14px;
        font-weight: 450;
      }

      .nav-badge {
        margin-left: auto;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #e74c3c;
        animation: pulse-dot 2s infinite;

        &.home-badge {
          background: #2ecc71;
        }
      }

      &:hover {
        color: #fff;
        background: rgba(255,255,255,0.08);

        .el-icon { transform: scale(1.08); }
      }

      &.active {
        color: #fff;
        background: rgba(22,160,133,0.25);
        box-shadow: 0 2px 14px rgba(22,160,133,0.15);
        font-weight: 500;

        &::before {
          content: '';
          position: absolute;
          left: -12px;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 22px;
          background: #16a085;
          border-radius: 0 4px 4px 0;
        }

        .el-icon { transform: scale(1.05); }
      }
    }
  }

  .sidebar-footer {
    padding: 18px 22px;
    border-top: 1px solid rgba(255,255,255,0.06);

    .footer-info {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: rgba(236,240,241,0.4);

      .version-tag {
        padding: 3px 8px;
        border-radius: 6px;
        background: rgba(255,255,255,0.08);
        color: #bdc3c7;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0.5px;
      }
    }
  }
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.85); }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f5f7fa;
}

.top-header {
  height: 64px;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 1px solid #e0e6ed;
  z-index: 10;
  position: sticky;
  top: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 18px;

    .collapse-btn {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #7f8c8d;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: #ecf0f1;
        color: #2c3e50;
      }
    }
  }

  .header-right {
    .user-block {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 8px 16px;
      border-radius: 14px;
      transition: all 0.25s;

      &:hover { background: #f8f9fa; }

      .header-avatar {
        box-shadow: 0 3px 12px rgba(44,62,80,0.2);
        border: 2px solid #fff;
        flex-shrink: 0;
      }

      .user-meta {
        display: flex;
        flex-direction: column;

        .user-name { font-size: 13.5px; color: #2c3e50; font-weight: 600; line-height: 1.3; }
        .user-role { font-size: 11.5px; color: #95a5a6; font-weight: 500; }
      }
    }
  }
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-fade-enter-active { transition: all 0.3s ease-out; }
.slide-fade-leave-active { transition: all 0.2s ease-in; }
.slide-fade-enter-from { transform: translateX(14px); opacity: 0; }
.slide-fade-leave-to { transform: translateX(-14px); opacity: 0; }

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 50;
    transform: translateX(-100%);

    &.collapsed {
      transform: translateX(0);
      width: 260px;
    }
  }

  .top-header {
    padding: 0 16px;
  }

  .content-area {
    padding: 16px;
  }
}
</style>
