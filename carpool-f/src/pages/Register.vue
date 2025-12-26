<template>
  <div class="register-container">
    <div class="register-card">
      <h2>注册</h2>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label for="username">用户名 *</label>
          <input
            id="username"
            v-model="registerForm.username"
            type="text"
            placeholder="请输入用户名（至少3个字符）"
            required
            minlength="3"
          />
        </div>

        <div class="form-group">
          <label for="password">密码 *</label>
          <input
            id="password"
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6个字符）"
            required
            minlength="6"
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码 *</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            required
          />
        </div>

        <div class="form-group">
          <label for="realName">真实姓名</label>
          <input
            id="realName"
            v-model="registerForm.realName"
            type="text"
            placeholder="请输入真实姓名"
          />
        </div>

        <div class="form-group">
          <label for="phoneNumber">手机号</label>
          <input
            id="phoneNumber"
            v-model="registerForm.phoneNumber"
            type="tel"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱"
          />
        </div>

        <button type="submit" class="register-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();

const registerForm = ref({
  username: '',
  password: '',
  realName: '',
  phoneNumber: '',
  email: ''
});

const confirmPassword = ref('');
const loading = ref(false);

const handleRegister = async () => {
  // 验证密码
  if (registerForm.value.password !== confirmPassword.value) {
    alert('两次输入的密码不一致');
    return;
  }

  // 验证手机号格式
  if (registerForm.value.phoneNumber && !/^1[3-9]\d{9}$/.test(registerForm.value.phoneNumber)) {
    alert('请输入正确的手机号');
    return;
  }

  loading.value = true;

  try {
    await userStore.register(registerForm.value);
    alert('注册成功！即将跳转到登录页面...');
    setTimeout(() => {
      router.push('/login');
    }, 1500);
  } catch (error) {
    console.error('注册失败:', error);
    alert(error.response?.data || '注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-card h2 {
  text-align: center;
  color: #333;
  margin: 0 0 30px 0;
  font-size: 28px;
  font-weight: 600;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

.form-group input {
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.register-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.footer .link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.footer .link:hover {
  text-decoration: underline;
}
</style>
