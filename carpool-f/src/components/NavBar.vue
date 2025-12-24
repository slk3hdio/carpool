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
    </div>
    <button class="mobile-menu-btn" @click="toggleMobileMenu" v-show="isMobile">
      <span></span>
      <span></span>
      <span></span>
    </button>
  </nav>
</template>

<script>
export default {
  data() {
    return {
      mobileMenuOpen: false,
      isMobile: false
    }
  },
  mounted() {
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth <= 768
      if (!this.isMobile) {
        this.mobileMenuOpen = false
      }
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen
    },
    closeMobileMenu() {
      this.mobileMenuOpen = false
    }
  }
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
