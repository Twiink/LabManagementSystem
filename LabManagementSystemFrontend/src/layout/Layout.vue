<template>
  <div class="app-wrapper">
    <!-- Sidebar -->
    <div class="sidebar-container" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <div class="logo-box">
          <el-icon :size="20"><Cpu /></el-icon>
        </div>
        <span v-if="!isCollapse">Lab System</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        router
      >
        <template v-for="item in menuRoutes" :key="item.path">
           <el-menu-item :index="'/' + item.path">
            <el-icon><component :is="item.meta?.icon" /></el-icon>
            <template #title>{{ item.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- Main Container -->
    <div class="main-container">
      <!-- Header -->
      <div class="header-container">
        <div class="left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-dropdown>
            <span class="el-dropdown-link" style="color: var(--text-main); cursor: pointer; display: flex; align-items: center;">
              <el-avatar :size="32" style="margin-right: 8px; background-color: var(--primary-color); color: var(--text-main); font-weight: bold;">{{ userStore.userInfo?.name?.[0]?.toUpperCase() || 'U' }}</el-avatar>
              {{ userStore.userInfo?.name || 'User' }}
              <el-tag size="small" style="margin-left: 8px; border-color: #8C6B5D; color: #4A403A;" effect="plain" type="info">{{ userStore.userInfo?.role || '未知' }}</el-tag>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- Content -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '')

// Filter routes for sidebar based on role
const menuRoutes = computed(() => {
  const userRole = userStore.userInfo?.role || 'GUEST'
  const mainRoute = router.options.routes.find(r => r.path === '/')
  if (!mainRoute || !mainRoute.children) return []

  return mainRoute.children.filter((child: any) => {
    if (child.meta && child.meta.roles) {
      return child.meta.roles.includes(userRole)
    }
    return true
  })
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
/* Variables mirroring glass.scss */
$bg-color: #f8f8f8;
$sidebar-bg: #FFF7F0;
$border-color: #8C6B5D;
$text-main: #4A403A;
$primary-color: #FFC085;

.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: $bg-color;
}

.sidebar-container {
  width: 240px;
  height: 100%;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  z-index: 1001;
  background: $sidebar-bg;
  border-right: 1px solid $border-color;
  
  &.collapsed {
    width: 64px;
    
    .logo span {
      opacity: 0;
      display: none;
    }
  }
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-main;
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.5px;
  border-bottom: 1px solid rgba($border-color, 0.2);
  background: $sidebar-bg;
  
  .logo-box {
    width: 32px;
    height: 32px;
    background: $primary-color;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid $border-color;
    color: #fff;
    margin-right: 10px;
  }
  
  span {
    white-space: nowrap;
    color: $text-main;
    font-family: 'JetBrains Mono', monospace;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.header-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 1000;
  background: #fff;
  border-bottom: 1px solid $border-color;
}

.left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: $text-main;
  transition: color 0.3s;
  
  &:hover {
    color: $primary-color;
  }
}

.app-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: #f8f8f8;
  background-image: radial-gradient(#e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
}

/* Transitions */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>