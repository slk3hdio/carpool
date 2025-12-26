<template>
  <nav class="nav">
    <a href="/" class="logo">拼车系统</a>
    <div class="menu" :class="{ 'mobile-open': mobileMenuOpen }">
      <router-link to="/" @click="closeMobileMenu">首页</router-link>
      <router-link to="/traffic" @click="closeMobileMenu">实时路况</router-link>
      <router-link to="/historical" @click="closeMobileMenu">历史分析</router-link>
      <router-link to="/carpool" @click="closeMobileMenu">同城拼车</router-link>
      <!-- <router-link to="/demo" @click="closeMobileMenu">路况演示</router-link> -->
      <router-link to="/user" @click="closeMobileMenu">用户中心</router-link>

      <!-- 登录/登出按钮 -->
      <div class="auth-section">
        <template v-if="userStore.isAuthenticated">
          <span class="welcome-text">欢迎, {{ userStore.username }}</span>
          <button class="auth-btn logout-btn" @click="handleLogout">登出</button>
        </template>
        <template v-else>
          <router-link to="/login" @click="closeMobileMenu" class="auth-btn login-btn">登录</router-link>
          <router-link to="/register" @click="closeMobileMenu" class="auth-btn register-btn">注册</router-link>
        </template>
      </div>
    </div>
    <button class="mobile-menu-btn" @click="toggleMobileMenu" v-show="isMobile">
      <span></span>
      <span></span>
      <span></span>
    </button>
  </nav>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();

const mobileMenuOpen = ref(false);
const isMobile = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;
  if (!isMobile.value) {
    mobileMenuOpen.value = false;
  }
};

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value;
};

const closeMobileMenu = () => {
  mobileMenuOpen.value = false;
};

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
  closeMobileMenu();
};

// 初始化和监听窗口大小变化
if (typeof window !== 'undefined') {
  checkMobile();
  window.addEventListener('resize', checkMobile);
}
</script>

<style>
.nav {
  position: fixed;
  top: 0; left: 0; right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.nav a {
  color: white;
  text-decoration: none;
  margin-left: 20px;
  font-weight: 500;
  transition: opacity 0.3s ease;
  padding: 8px 16px;
  border-radius: 20px;
  display: inline-block;
}

.nav a:hover,
.nav a.router-link-active {
  background: rgba(255, 255, 255, 0.2);
}

.logo {
  font-weight: bold;
  font-size: 1.2rem;
  background: none;
  color: white !important;
  padding: 0 !important;
  margin: 0 !important;
}

/* 认证相关样式 */
.auth-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 20px;
}

.welcome-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.auth-btn {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  border: none;
  cursor: pointer;
}

.login-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.register-btn {
  background: white;
  color: #667eea;
}

.register-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.mobile-menu-btn {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
}

.mobile-menu-btn span {
  width: 25px;
  height: 3px;
  background: white;
  margin: 2px 0;
  transition: 0.3s;
  border-radius: 3px;
}

/* 移动端样式 */
@media (max-width: 768px) {
  .nav {
    padding: 0 15px;
    height: 60px;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .menu {
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-direction: column;
    padding: 20px;
    transform: translateY(-100%);
    transition: transform 0.3s ease;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }

  .menu.mobile-open {
    transform: translateY(0);
  }

  .menu a {
    margin: 8px 0;
    padding: 15px 20px;
    text-align: center;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.1);
  }

  .menu a:hover,
  .menu a.router-link-active {
    background: rgba(255, 255, 255, 0.2);
  }

  .auth-section {
    flex-direction: column;
    width: 100%;
    margin: 16px 0 0 0;
    padding: 16px;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 10px;
  }

  .welcome-text {
    margin-bottom: 8px;
    text-align: center;
  }

  .auth-btn {
    width: 100%;
    text-align: center;
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .nav {
    padding: 0 10px;
  }

  .logo {
    font-size: 1.1rem;
  }
}
</style>
