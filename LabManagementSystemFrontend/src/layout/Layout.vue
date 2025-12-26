<template>
  <div class="app-wrapper">
    <!-- Sidebar -->
    <div class="sidebar-container" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <el-icon :size="24" color="var(--primary-color)" style="margin-right: 10px"><ElementPlus /></el-icon>
        <span v-if="!isCollapse">Lab System</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
           <el-menu-item :index="route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <template #title>{{ route.meta?.title }}</template>
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
              <el-avatar :size="32" style="margin-right: 8px; background-color: var(--primary-color)">{{ userStore.userInfo.name?.[0]?.toUpperCase() }}</el-avatar>
              {{ userStore.userInfo.name || 'User' }}
              <el-tag size="small" style="margin-left: 8px" effect="plain">{{ userStore.userInfo.role }}</el-tag>
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
  const userRole = userStore.userInfo.role || 'GUEST'
  // Get the children of the main layout route (which is at index 2 in our router definition)
  // A robust app would have a recursive generator, but for this structure:
  const mainRoute = router.options.routes.find(r => r.path === '/')
  if (!mainRoute || !mainRoute.children) return []

  return mainRoute.children.filter((child: any) => {
    if (child.meta && child.meta.roles) {
      return child.meta.roles.includes(userRole)
    }
    return true // Default visible if no roles defined
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
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.sidebar-container {
  width: 220px;
  height: 100%;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  
  &.collapsed {
    width: 64px;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-main);
  font-weight: bold;
  font-size: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-main);
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>