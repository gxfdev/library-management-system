<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onErrorCaptured } from 'vue'
import { ElMessage } from 'element-plus'

onErrorCaptured((err) => {
  console.error('全局错误捕获:', err)
  const msg = err?.message || ''
  const businessErrors = [
    '验证码',
    '密码',
    '用户名',
    '权限',
    '登录',
    '已过期',
    '已存在',
    '不能为空',
    '格式不正确',
    '不足',
    '已达上限',
  ]
  const isBusinessError = businessErrors.some((k) => msg.includes(k))
  if (!isBusinessError) {
    ElMessage.error('系统发生异常，请刷新页面重试')
  }
  return false
})
</script>

<style lang="scss">
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
