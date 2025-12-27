<template>
  <div class="user-page">
    <!-- 未登录状态 -->
    <div v-if="!userStore.isAuthenticated" class="login-section">
      <div class="login-card">
        <h2>欢迎来到拼车系统</h2>
        <p class="subtitle">请登录或注册以使用完整功能</p>

        <div class="button-group">
          <router-link to="/login" class="btn btn-primary">登录</router-link>
          <router-link to="/register" class="btn btn-secondary">注册</router-link>
        </div>

        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">🚗</div>
            <div class="feature-text">发布拼车信息</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📍</div>
            <div class="feature-text">实时路况查询</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">历史数据分析</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 已登录状态 -->
    <div v-else class="user-dashboard">
      <div class="user-header">
        <div class="avatar">
          {{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <div class="user-info">
          <h2>{{ userStore.username }}</h2>
          <p class="user-id">用户ID: {{ userStore.userId }}</p>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <span class="icon">🚪</span>
          <span>退出登录</span>
        </button>
      </div>

      <!-- 标签页导航 -->
      <div class="tab-navigation">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'overview' }"
          @click="activeTab = 'overview'"
        >
          <span class="tab-icon">📊</span>
          <span>概览</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'trips' }"
          @click="activeTab = 'trips'"
        >
          <span class="tab-icon">🛣️</span>
          <span>我的行程</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'invitations' }"
          @click="activeTab = 'invitations'"
        >
          <span class="tab-icon">📬</span>
          <span>收到的邀请</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'my-requests' }"
          @click="activeTab = 'my-requests'"
        >
          <span class="tab-icon">🚗</span>
          <span>我的发布</span>
        </button>
      </div>

      <div class="user-content">
        <!-- 概览标签页 -->
        <div v-show="activeTab === 'overview'" class="tab-content">
          <div class="info-section">
          <h3>个人信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>用户名</label>
              <span>{{ userStore.username }}</span>
            </div>
            <div class="info-item">
              <label>用户ID</label>
              <span>{{ userStore.userId }}</span>
            </div>
            <div class="info-item" v-if="userStore.user?.phoneNumber">
              <label>手机号</label>
              <span>{{ userStore.user.phoneNumber }}</span>
            </div>
            <div class="info-item" v-if="userStore.user?.email">
              <label>邮箱</label>
              <span>{{ userStore.user.email }}</span>
            </div>
            <div class="info-item" v-if="userStore.user?.realName">
              <label>真实姓名</label>
              <span>{{ userStore.user.realName }}</span>
            </div>
            <div class="info-item">
              <label>注册时间</label>
              <span>{{ formatDate(userStore.user?.createdAt) }}</span>
            </div>
          </div>
        </div>

        <div class="stats-section">
          <h3>我的拼车</h3>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-number">0</div>
              <div class="stat-label">发布的拼车</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">0</div>
              <div class="stat-label">成功的拼车</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">0</div>
              <div class="stat-label">待处理请求</div>
            </div>
          </div>
        </div>

        <div class="actions-section">
          <h3>快捷操作</h3>
          <div class="action-buttons">
            <router-link to="/carpool" class="action-btn">
              <span class="icon">➕</span>
              <span>发布拼车</span>
            </router-link>
            <button class="action-btn">
              <span class="icon">📝</span>
              <span>我的发布</span>
            </button>
            <button class="action-btn">
              <span class="icon">⚙️</span>
              <span>账号设置</span>
            </button>
          </div>
        </div>
        </div>

        <!-- 我的行程标签页 -->
        <div v-show="activeTab === 'trips'" class="tab-content">
          <TripCardGrid
            :trips="trips"
            :loading="loadingTrips"
            :show-actions="true"
            @refresh="fetchTrips"
          />
        </div>

        <!-- 收到的邀请标签页 -->
        <div v-show="activeTab === 'invitations'" class="tab-content">
          <InvitationList
            :user-id="userStore.userId"
            :received="true"
            @view="handleViewInvitation"
          />
        </div>

        <!-- 我的发布标签页 -->
        <div v-show="activeTab === 'my-requests'" class="tab-content">
          <div v-if="loadingRequests" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <div v-else-if="myRequests.length === 0" class="empty-state">
            <div class="empty-icon">🚗</div>
            <h3>暂无发布记录</h3>
            <p>您还没有发布过拼车需求</p>
            <router-link to="/carpool" class="btn btn-primary">
              立即发布
            </router-link>
          </div>

          <div v-else class="my-requests-grid">
            <CarpoolCard
              v-for="request in myRequests"
              :key="request.id"
              :request="request"
              @contact="handleContactRequest"
              @invite="handleInviteRequest"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import axios from 'axios';
import InvitationList from '../components/InvitationList.vue';
import TripCardGrid from '../components/TripCardGrid.vue';
import CarpoolCard from '../components/CarpoolCard.vue';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('overview');

// 行程数据
const trips = ref([]);
const loadingTrips = ref(false);

// 我发布的需求数据
const myRequests = ref([]);
const loadingRequests = ref(false);

// 获取用户行程
const fetchTrips = async () => {
  if (!userStore.isAuthenticated) return;

  loadingTrips.value = true;
  try {
    const response = await axios.get('http://localhost:8080/api/trip/user', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    });
    trips.value = response.data || [];
  } catch (error) {
    console.error('获取行程列表失败:', error);
    if (error.response?.status === 401) {
      alert('登录已过期，请重新登录');
      userStore.logout();
      router.push('/login');
    } else {
      trips.value = [];
    }
  } finally {
    loadingTrips.value = false;
  }
};

// 获取用户发布的拼车需求
const fetchMyRequests = async () => {
  if (!userStore.isAuthenticated) return;

  loadingRequests.value = true;
  try {
    const response = await axios.get('http://localhost:8080/api/carpool/my-requests', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    });
    myRequests.value = response.data || [];
  } catch (error) {
    console.error('获取我的发布失败:', error);
    if (error.response?.status === 401) {
      alert('登录已过期，请重新登录');
      userStore.logout();
      router.push('/login');
    } else {
      myRequests.value = [];
    }
  } finally {
    loadingRequests.value = false;
  }
};

// 监听标签页切换，加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'trips' && trips.value.length === 0) {
    fetchTrips();
  }
  if (newTab === 'my-requests' && myRequests.value.length === 0) {
    fetchMyRequests();
  }
});

// 组件挂载时如果默认选中某个标签，则加载数据
onMounted(() => {
  if (activeTab.value === 'trips') {
    fetchTrips();
  }
  if (activeTab.value === 'my-requests') {
    fetchMyRequests();
  }
});

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout();
    router.push('/');
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '未知';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const handleViewInvitation = (invitation) => {
  console.log('查看邀请详情:', invitation);
  // 可以添加详情查看逻辑
};

const handleContactRequest = (request) => {
  console.log('联系车主:', request);
  alert(`联系车主: ${request.realName || request.username}\n电话: ${request.phoneNumber}`);
};

const handleInviteRequest = (request) => {
  console.log('发起邀请:', request);
  alert(`向 ${request.realName || request.username} 发起拼车邀请`);
};
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  padding: 80px 20px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 未登录状态 */
.login-section {
  max-width: 600px;
  margin: 0 auto;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.login-card h2 {
  font-size: 28px;
  color: #333;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666;
  margin: 0 0 30px 0;
}

.button-group {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
}

.btn {
  padding: 12px 32px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #f8f9ff;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  font-size: 36px;
}

.feature-text {
  color: #666;
  font-size: 14px;
}

/* 已登录状态 */
.user-dashboard {
  max-width: 1000px;
  margin: 0 auto;
}

.user-header {
  background: white;
  border-radius: 16px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: white;
}

.user-info {
  flex: 1;
}

.user-info h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
}

.user-id {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.logout-btn {
  padding: 10px 20px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: #e8e8e8;
}

/* 标签页导航 */
.tab-navigation {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.tab-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: #f8f9ff;
  border: 2px solid #e8ebff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 100px;
}

.tab-btn:hover {
  background: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-icon {
  font-size: 24px;
}

.tab-btn span:last-child {
  font-size: 14px;
  font-weight: 600;
}

.tab-content {
  animation: fadeIn 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.placeholder-section {
  background: white;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.placeholder-section h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
}

.placeholder-section p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.user-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-section,
.stats-section,
.actions-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.info-section h3,
.stats-section h3,
.actions-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item label {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

.info-item span {
  font-size: 15px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  color: white;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: #f8f9ff;
  border: 2px solid #e8ebff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: #333;
  font-size: 14px;
}

.action-btn:hover {
  background: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.action-btn .icon {
  font-size: 28px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .user-page {
    padding: 70px 16px 16px;
  }

  .login-card {
    padding: 30px 20px;
  }

  .features {
    grid-template-columns: 1fr;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    grid-template-columns: 1fr;
  }
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  font-size: 1rem;
  color: #666;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
}

.empty-state p {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px 0;
}

.empty-state .btn {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 24px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.empty-state .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 我的发布网格 */
.my-requests-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 0;
}

@media (min-width: 768px) {
  .my-requests-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    padding: 20px 0;
  }
}

@media (min-width: 1024px) {
  .my-requests-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    padding: 24px 0;
  }
}
</style>
