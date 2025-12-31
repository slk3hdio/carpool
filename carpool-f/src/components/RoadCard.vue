<template>
  <div class="road-card" :class="congestionClass">
    <div class="card-header">
      <div class="road-info">
        <h3 class="road-name">{{ roadData.road_name || '未知道路' }}</h3>
        <p class="road-location">{{ roadData.city || '未知城市' }}</p>
      </div>
      <div class="status-badge" :class="statusBadgeClass">
        {{ statusText }}
      </div>
    </div>

    <div class="card-content">
      <div class="congestion-visual">
        <div class="congestion-bar">
          <div class="congestion-fill" :style="{ width: congestionPercentage + '%' }"></div>
        </div>
        <div class="congestion-indicators">
          <span
            v-for="(level, index) in congestionLevels"
            :key="index"
            class="indicator"
            :class="{ active: roadData.evaluation_status >= index }"
          ></span>
        </div>
        <div class="congestion-label">
          拥挤程度: {{ roadData.evaluation_status || 0 }}/4
        </div>
      </div>

      <div class="traffic-info" v-if="roadData.request_time">
        <div class="info-item">
          <span class="info-label">更新时间:</span>
          <span class="info-value">{{ formatTime(roadData.request_time) }}</span>
        </div>
        <div class="info-item" v-if="roadData.congestion_distance">
          <span class="info-label">拥堵距离:</span>
          <span class="info-value">{{ roadData.congestion_distance }}km</span>
        </div>
        <div class="info-item" v-if="roadData.speed">
          <span class="info-label">平均速度:</span>
          <span class="info-value">{{ roadData.speed }}km/h</span>
        </div>
      </div>

      <div class="card-description" v-if="roadData.description">
        {{ roadData.description }}
      </div>
    </div>

    <div class="card-footer">
      <button class="detail-btn" @click="viewDetails">
        查看详情
      </button>
      <div class="card-actions">
        <span class="action-btn" @click="refreshData">
          <span class="icon">🔄</span>
        </span>
        <span class="action-btn" @click="shareRoad">
          <span class="icon">📤</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RoadCard',
  props: {
    roadData: {
      type: Object,
      required: true,
      default: () => ({})
    }
  },
  computed: {
    congestionPercentage() {
      return ((this.roadData.evaluation_status || 0) / 4) * 100
    },

    statusText() {
      const status = this.roadData.evaluation_status || 0
      const statusMap = ['畅通', '缓行', '拥堵', '严重拥堵', '瘫痪']
      return statusMap[status] || '未知'
    },

    statusBadgeClass() {
      const status = this.roadData.evaluation_status || 0
      return `status-${status}`
    },

    congestionClass() {
      const status = this.roadData.evaluation_status || 0
      return `congestion-${status}`
    }
  },

  data() {
    return {
      congestionLevels: [0, 1, 2, 3, 4]
    }
  },

  methods: {
    formatTime(timeString) {
      if (!timeString) return '未知'
      const date = new Date(timeString)
      const now = new Date()
      const diff = Math.floor((now - date) / 1000) // 秒数差

      if (diff < 60) return '刚刚'
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
      if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`

      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
    },

    viewDetails() {
      this.$emit('view-details', this.roadData)
    },

    refreshData() {
      this.$emit('refresh', this.roadData)
    },

    shareRoad() {
      if (navigator.share) {
        navigator.share({
          title: `${this.roadData.road_name} 路况信息`,
          text: `${this.roadData.road_name} 当前状态: ${this.statusText}`,
          url: window.location.href
        }).catch(() => {})
      } else {
        // 降级处理：复制到剪贴板
        const text = `${this.roadData.road_name} 当前状态: ${this.statusText}`
        navigator.clipboard.writeText(text).then(() => {
          this.$message?.info('路况信息已复制到剪贴板')
        })
      }
      this.$emit('share', this.roadData)
    }
  }
}
</script>

<style scoped>
.road-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.road-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #FFC107, #FF9800, #F44336, #9C27B0);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.road-card.congestion-0::before { transform: scaleX(0.2); background-color: #4CAF50; }
.road-card.congestion-1::before { transform: scaleX(0.4); background-color: #8BC34A; }
.road-card.congestion-2::before { transform: scaleX(0.6); background-color: #FFC107; }
.road-card.congestion-3::before { transform: scaleX(0.8); background-color: #FF9800; }
.road-card.congestion-4::before { transform: scaleX(1); background-color: #F44336; }

.road-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.road-info {
  flex: 1;
  min-width: 0;
}

.road-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.road-location {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.status-0 { background: #E8F5E8; color: #2E7D32; }
.status-badge.status-1 { background: #F0F8E8; color: #689F38; }
.status-badge.status-2 { background: #FFF8E1; color: #F57C00; }
.status-badge.status-3 { background: #FFF3E0; color: #E64A19; }
.status-badge.status-4 { background: #FFEBEE; color: #C62828; }

/* 拥挤程度可视化 */
.congestion-visual {
  margin-bottom: 16px;
}

.congestion-bar {
  height: 8px;
  background: #f1f3f4;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.congestion-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #FFC107, #F44336);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.congestion-indicators {
  display: flex;
  gap: 4px;
  margin-bottom: 4px;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: all 0.3s ease;
}

.indicator.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  transform: scale(1.2);
}

.congestion-label {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

/* 交通信息 */
.traffic-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.info-label {
  color: #666;
  font-weight: 500;
}

.info-value {
  color: #2c3e50;
  font-weight: 600;
}

/* 卡片描述 */
.card-description {
  font-size: 0.9rem;
  color: #555;
  line-height: 1.4;
  margin-bottom: 16px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
  padding-top: 12px;
}

.detail-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f1f3f4;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.action-btn:hover {
  background: #e8eaed;
  transform: scale(1.1);
}

.icon {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .road-card {
    padding: 16px;
  }

  .road-name {
    font-size: 1rem;
  }

  .card-header {
    flex-direction: column;
    gap: 8px;
  }

  .traffic-info {
    gap: 4px;
  }

  .info-item {
    font-size: 0.85rem;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
  }

  .detail-btn {
    width: 100%;
    order: 2;
  }

  .card-actions {
    order: 1;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .road-card {
    padding: 12px;
    border-radius: 12px;
  }

  .congestion-indicators {
    justify-content: center;
  }

  .card-actions {
    justify-content: flex-end;
  }
}

/* 小屏幕优化 */
@media (max-width: 360px) {
  .road-card {
    padding: 10px;
  }

  .road-name {
    font-size: 0.95rem;
  }

  .status-badge {
    font-size: 0.75rem;
    padding: 3px 10px;
  }
}
</style>