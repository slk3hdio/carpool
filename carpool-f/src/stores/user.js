import { defineStore } from 'pinia';
import axios from 'axios';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: false
  }),

  getters: {
    currentUser: (state) => state.user,
    userId: (state) => state.user?.id,
    username: (state) => state.user?.username
  },

  actions: {
    // 设置用户信息和token
    setUser(user, token) {
      this.user = user;
      this.token = token;
      this.isAuthenticated = true;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));

      // 设置axios默认请求头
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    },

    // 清除用户信息
    clearUser() {
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      delete axios.defaults.headers.common['Authorization'];
    },

    // 登录
    async login(credentials) {
      try {
        const response = await axios.post('http://47.100.65.234/api/auth/login', credentials);

        if (response.data.token) {
          this.setUser(response.data.user, response.data.token);
          return response.data;
        } else {
          throw new Error(response.data || '登录失败');
        }
      } catch (error) {
        console.error('Login error:', error);
        throw error;
      }
    },

    // 注册
    async register(userData) {
      try {
        const response = await axios.post('http://47.100.65.234/api/auth/register', userData);
        return response.data;
      } catch (error) {
        console.error('Register error:', error);
        throw error;
      }
    },

    // 获取当前用户信息
    async fetchCurrentUser() {
      if (!this.token) {
        return;
      }

      try {
        const response = await axios.get('http://47.100.65.234/api/auth/me');

        if (response.data) {
          this.user = response.data;
          this.isAuthenticated = true;
          localStorage.setItem('user', JSON.stringify(response.data));
        }
      } catch (error) {
        console.error('Fetch current user error:', error);
        this.clearUser();
      }
    },

    // 登出
    logout() {
      this.clearUser();
    },

    // 初始化：从localStorage恢复用户信息
    initialize() {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');

      if (token && userStr) {
        try {
          this.user = JSON.parse(userStr);
          this.token = token;
          this.isAuthenticated = true;
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

          // 验证token是否仍然有效
          this.fetchCurrentUser();
        } catch (error) {
          console.error('Initialize user error:', error);
          this.clearUser();
        }
      }
    }
  }
});
