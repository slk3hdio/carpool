<template>
  <div class="carpool-card">
    <div class="card-header">
      <div class="user-info">
        <div class="user-avatar">
          <span class="avatar-icon">{{ hasCarIcon }}</span>
        </div>
        <div class="user-details">
          <div class="user-type">{{ userTypeText }}</div>
          <div class="passenger-count" v-if="!request.hasCar">
            乘客人数: {{ request.passengerCount }}人
          </div>
          <div class="seat-info" v-else>
            可载{{ request.maxPassengerCount - request.passengerCount }}人
          </div>
        </div>
      </div>
      <div class="status-badge" :class="statusClass">
        {{ request.statusDesc || '等待匹配' }}
      </div>
    </div>

    <div class="route-info">
      <div class="route-point start-point">
        <div class="point-icon start-icon">
          <span class="icon-text">起</span>
        </div>
        <div class="point-info">
          <div class="point-label">起点</div>
          <div class="point-location">{{ request.startLocation }}</div>
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
          <div class="point-location">{{ request.endLocation }}</div>
        </div>
      </div>
    </div>

    <div class="time-info">
      <div class="time-item">
        <span class="time-label">最早出发</span>
        <span class="time-value">{{ formatTime(request.earliestDepartureTime) }}</span>
      </div>
      <div class="time-divider">~</div>
      <div class="time-item">
        <span class="time-label">最晚出发</span>
        <span class="time-value">{{ formatTime(request.latestDepartureTime) }}</span>
      </div>
    </div>

    <div class="card-footer">
      <div class="contact-info">
        <span class="contact-icon">📞</span>
        <span class="contact-phone">{{ maskPhone(request.phoneNumber) }}</span>
      </div>
      <button class="action-btn" @click="handleContact">
        联系车主
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CarpoolCard',
  props: {
    request: {
      type: Object,
      required: true,
      default: () => ({})
    }
  },
  computed: {
    hasCarIcon() {
      return this.request.hasCar ? '🚗' : '👤'
    },

    userTypeText() {
      return this.request.hasCar ? '车主' : '乘客'
    },

    statusClass() {
      const status = this.request.statusDesc || ''
      if (status.includes('等待匹配')) return 'status-pending'
      if (status.includes('已匹配')) return 'status-matched'
      if (status.includes('完成')) return 'status-completed'
      if (status.includes('取消')) return 'status-cancelled'
      return 'status-default'
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

    maskPhone(phone) {
      if (!phone) return '未提供'
      if (phone.length < 7) return phone
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    },

    handleContact() {
      this.$emit('contact', this.request)
    }
  }
}
</script>

<style scoped>
.carpool-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.carpool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4CAF50, #2196F3);
}

.carpool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 24px;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-type {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.passenger-count,
.seat-info {
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

.status-badge.status-pending {
  background: #FFF3E0;
  color: #F57C00;
}

.status-badge.status-matched {
  background: #E8F5E9;
  color: #388E3C;
}

.status-badge.status-completed {
  background: #E3F2FD;
  color: #1976D2;
}

.status-badge.status-cancelled {
  background: #FFEBEE;
  color: #D32F2F;
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

/* 时间信息 */
.time-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #F8F9FA;
  border-radius: 8px;
  margin-bottom: 16px;
}

.time-item {
  flex: 1;
  text-align: center;
}

.time-label {
  display: block;
  font-size: 0.75rem;
  color: #666;
  margin-bottom: 4px;
}

.time-value {
  display: block;
  font-size: 0.9rem;
  color: #2c3e50;
  font-weight: 600;
}

.time-divider {
  padding: 0 8px;
  color: #999;
  font-size: 0.8rem;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-icon {
  font-size: 20px;
}

.contact-phone {
  font-size: 0.95rem;
  color: #666;
  font-weight: 500;
}

.action-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.action-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carpool-card {
    padding: 16px;
  }

  .card-header {
    margin-bottom: 16px;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
  }

  .avatar-icon {
    font-size: 20px;
  }

  .route-info {
    margin-bottom: 16px;
  }

  .time-info {
    padding: 10px;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
  }

  .action-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .carpool-card {
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

  .time-value {
    font-size: 0.85rem;
  }
}
</style>
