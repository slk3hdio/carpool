<template>
  <div class="carpool-grid-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="requests.length === 0" class="empty-state">
      <div class="empty-icon">🚗</div>
      <h3>暂无拼车需求</h3>
      <p>当前没有符合条件的拼车需求</p>
    </div>

    <div v-else class="carpool-grid">
      <CarpoolCard
        v-for="request in requests"
        :key="request.id"
        :request="request"
        @contact="handleContact"
      />
    </div>
  </div>
</template>

<script>
import CarpoolCard from './CarpoolCard.vue'

export default {
  name: 'CarpoolCardGrid',
  components: {
    CarpoolCard
  },
  props: {
    requests: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleContact(request) {
      this.$emit('contact', request)
    }
  }
}
</script>

<style scoped>
.carpool-grid-container {
  width: 100%;
  min-height: 200px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 1.25rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 0.95rem;
  color: #7f8c8d;
  margin: 0;
}

/* 卡片网格布局 - 支持灵活堆叠 */
.carpool-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 0;
}

/* 平板设备 - 两列布局 */
@media (min-width: 768px) {
  .carpool-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    padding: 20px 0;
  }
}

/* 桌面设备 - 三列布局 */
@media (min-width: 1024px) {
  .carpool-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    padding: 24px 0;
  }
}

/* 大屏幕 - 四列布局 */
@media (min-width: 1400px) {
  .carpool-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* 响应式调整 */
@media (max-width: 480px) {
  .carpool-grid {
    gap: 12px;
    padding: 12px 0;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-state h3 {
    font-size: 1.1rem;
  }

  .empty-state p {
    font-size: 0.85rem;
  }
}
</style>
