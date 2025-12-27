<template>
  <div class="trip-card" :class="statusClass">
    <div class="card-header">
      <div class="trip-info">
        <div class="trip-avatar">
          <span class="avatar-icon">🚗</span>
        </div>
        <div class="trip-details">
          <div class="trip-title">行程信息</div>
          <div class="trip-id">行程 #{{ trip.id }}</div>
        </div>
      </div>
      <div class="status-badge" :class="`status-${tripStatus}`">
        {{ trip.statusDesc || '进行中' }}
      </div>
    </div>

    <div class="route-info">
      <div class="route-point start-point">
        <div class="point-icon start-icon">
          <span class="icon-text">起</span>
        </div>
        <div class="point-info">
          <div class="point-label">起点</div>
          <div class="point-location">{{ trip.startLocation }}</div>
        </div>
      </div>

      <div class="route-line">
        <div class="line-arrow"></div>
      </div>

      <div class="route-point end-point">
        <div class="point-icon end-icon">
          <span class="icon-text">终</span>
        </div>
        <div class="point-info">
          <div class="point-label">终点</div>
          <div class="point-location">{{ trip.endLocation }}</div>
        </div>
      </div>
    </div>

    <div class="trip-details">
      <div class="detail-item">
        <span class="detail-icon">🕐</span>
        <span class="detail-label">出发时间:</span>
        <span class="detail-value">{{ formatTime(trip.departureAt) }}</span>
      </div>

      <div class="detail-item">
        <span class="detail-icon">👥</span>
        <span class="detail-label">乘客人数:</span>
        <span class="detail-value">{{ trip.passengerCount }}人</span>
      </div>

      <div class="detail-item" v-if="trip.matchAt">
        <span class="detail-icon">🤝</span>
        <span class="detail-label">匹配时间:</span>
        <span class="detail-value">{{ formatTime(trip.matchAt) }}</span>
      </div>

      <div class="detail-item">
        <span class="detail-icon">📅</span>
        <span class="detail-label">创建时间:</span>
        <span class="detail-value">{{ formatTime(trip.createdAt) }}</span>
      </div>
    </div>

    <div class="card-footer" v-if="showActions">
      <div class="footer-info">
        <span class="footer-text">行程状态: {{ trip.statusDesc || '进行中' }}</span>
      </div>
      <div class="action-buttons">
        <button class="action-btn view-btn" @click="showDetailModal = true">
          查看详情
        </button>
        <button class="action-btn cancel-btn" @click="handleCancel" v-if="canCancel">
          取消行程
        </button>
      </div>
    </div>

    <!-- 行程详情模态框 -->
    <Teleport to="body">
      <div v-if="showDetailModal" class="trip-modal" @click.self="showDetailModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>行程详情</h3>
            <button class="close-btn" @click="showDetailModal = false">✕</button>
          </div>
          <div class="modal-body">
            <div class="detail-row">
              <span class="label">行程ID:</span>
              <span class="value">{{ trip.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">起点:</span>
              <span class="value">{{ trip.startLocation }}</span>
            </div>
            <div class="detail-row">
              <span class="label">终点:</span>
              <span class="value">{{ trip.endLocation }}</span>
            </div>
            <div class="detail-row">
              <span class="label">出发时间:</span>
              <span class="value">{{ formatFullTime(trip.departureAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">状态:</span>
              <span class="value status-badge" :class="`status-${tripStatus}`">
                {{ trip.statusDesc || '进行中' }}
              </span>
            </div>
            <div class="detail-row">
              <span class="label">乘客数:</span>
              <span class="value">{{ trip.passengerCount }} 人</span>
            </div>
            <div class="detail-row" v-if="trip.matchAt">
              <span class="label">匹配时间:</span>
              <span class="value">{{ formatFullTime(trip.matchAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatFullTime(trip.createdAt) }}</span>
            </div>
            <div class="detail-row" v-if="trip.requestIds && trip.requestIds.length">
              <span class="label">关联需求:</span>
              <span class="value">{{ trip.requestIds.join(', ') }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showDetailModal = false">关闭</button>
            <button
              v-if="canCancel"
              class="btn btn-danger"
              @click="handleCancelFromModal"
            >
              取消行程
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script>
import { useUserStore } from '../stores/user';
import axios from 'axios';

export default {
  name: 'TripCard',
  props: {
    trip: {
      type: Object,
      required: true,
      default: () => ({})
    },
    showActions: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      showDetailModal: false,
      userStore: useUserStore()
    };
  },
  computed: {
    tripStatus() {
      const status = this.trip.statusDesc || ''
      if (status.includes('已完成') || status.includes('完成') || status.includes('已到达')) return 'completed'
      if (status.includes('取消')) return 'cancelled'
      if (status.includes('进行中') || status.includes('出发') || status.includes('已创建')) return 'ongoing'
      return 'default'
    },

    statusClass() {
      return `status-${this.tripStatus}`
    },

    canCancel() {
      const status = this.trip.statusDesc || ''
      // 只有进行中或已创建的行程可以取消
      return status.includes('已创建') || status.includes('进行中') || !status
    }
  },
  methods: {
    formatTime(timeString) {
      if (!timeString) return '待定'
      const date = new Date(timeString)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    },

    formatFullTime(timeString) {
      if (!timeString) return '未知'
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    async handleCancel() {
      const confirmMsg = `确定要取消从 ${this.trip.startLocation} 到 ${this.trip.endLocation} 的行程吗？`
      if (!confirm(confirmMsg)) {
        return
      }

      try {
        await axios.put(`http://localhost:8080/api/trip/${this.trip.id}/cancel`, {}, {
          headers: {
            'Authorization': `Bearer ${this.userStore.token}`
          }
        })
        alert('行程已取消')
        // 触发刷新事件
        this.$emit('cancelled')
      } catch (error) {
        console.error('取消行程失败:', error)
        if (error.response?.status === 403) {
          alert('您没有权限操作此行程')
        } else if (error.response?.status === 401) {
          alert('登录已过期，请重新登录')
          this.userStore.logout()
          window.location.href = '/login'
        } else {
          const errorMsg = error.response?.data?.message || error.message
          alert('取消行程失败: ' + errorMsg)
        }
      }
    },

    async handleCancelFromModal() {
      await this.handleCancel()
      // 取消成功后关闭模态框
      this.showDetailModal = false
    }
  }
}
</script>

<style scoped>
.trip-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.trip-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #2196F3, #4CAF50);
}

.trip-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 状态样式 */
.trip-card.status-completed::before {
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
}

.trip-card.status-cancelled::before {
  background: linear-gradient(90deg, #9E9E9E, #BDBDBD);
}

.trip-card.status-ongoing::before {
  background: linear-gradient(90deg, #2196F3, #03A9F4);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.trip-info {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.trip-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2196F3, #4CAF50);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 24px;
}

.trip-details {
  flex: 1;
  min-width: 0;
}

.trip-title {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.trip-id {
  font-size: 0.85rem;
  color: #666;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.status-completed {
  background: #E8F5E9;
  color: #388E3C;
}

.status-badge.status-cancelled {
  background: #FFEBEE;
  color: #D32F2F;
}

.status-badge.status-ongoing {
  background: #E3F2FD;
  color: #1976D2;
}

.status-badge.status-default {
  background: #F5F5F5;
  color: #666;
}

/* 路线信息 */
.route-info {
  margin-bottom: 20px;
  position: relative;
}

.route-point {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.point-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.start-icon {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
}

.end-icon {
  background: linear-gradient(135deg, #F44336, #FF9800);
}

.icon-text {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.point-info {
  flex: 1;
  padding-top: 4px;
}

.point-label {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 2px;
}

.point-location {
  font-size: 0.95rem;
  color: #2c3e50;
  font-weight: 500;
  line-height: 1.4;
}

.route-line {
  height: 40px;
  margin-left: 16px;
  position: relative;
}

.line-arrow {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #4CAF50 0%, #F44336 100%);
}

.line-arrow::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: -4px;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 8px solid #F44336;
}

/* 行程详情 */
.trip-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px;
  background: #F8F9FA;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.detail-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.detail-label {
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
  flex: 1;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
  padding-top: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-info {
  flex: 1;
  min-width: 120px;
}

.footer-text {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.view-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.cancel-btn {
  background: linear-gradient(135deg, #F44336, #FF5722);
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

.action-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .trip-card {
    padding: 16px;
  }

  .card-header {
    margin-bottom: 16px;
  }

  .trip-avatar {
    width: 40px;
    height: 40px;
  }

  .avatar-icon {
    font-size: 20px;
  }

  .route-info {
    margin-bottom: 16px;
  }

  .trip-details {
    padding: 10px;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .action-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .trip-card {
    padding: 12px;
    border-radius: 12px;
  }

  .point-icon {
    width: 28px;
    height: 28px;
  }

  .icon-text {
    font-size: 12px;
  }

  .point-location {
    font-size: 0.9rem;
  }

  .detail-item {
    font-size: 0.85rem;
  }
}

/* 行程详情模态框样式 */
.trip-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 30px;
  border-bottom: 1px solid #e8eaed;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #f5f5f5;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #e8e8e8;
  transform: rotate(90deg);
}

.modal-body {
  padding: 30px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-row .value {
  font-size: 15px;
  color: #333;
  text-align: right;
  word-break: break-all;
}

.detail-row .value.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 30px;
  border-top: 1px solid #e8eaed;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.btn-secondary:hover {
  background: #e8e8e8;
}

.btn-danger {
  background: linear-gradient(135deg, #F44336, #FF5722);
  color: white;
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

/* 模态框响应式 */
@media (max-width: 768px) {
  .trip-modal {
    padding: 16px;
  }

  .modal-content {
    max-height: 95vh;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 20px;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .detail-row .value {
    text-align: left;
  }

  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
