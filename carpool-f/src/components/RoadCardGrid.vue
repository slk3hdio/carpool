<template>
  <div class="road-card-grid">
    <div class="grid-header">
      <h2 class="grid-title">实时路况监控</h2>
      <div class="grid-controls">
        <div class="view-toggle">
          <button
            class="toggle-btn"
            :class="{ active: viewMode === 'grid' }"
            @click="viewMode = 'grid'"
          >
            <span class="icon">⚏</span>
          </button>
          <button
            class="toggle-btn"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
          >
            <span class="icon">☰</span>
          </button>
        </div>
        <div class="sort-select">
          <select v-model="sortBy" class="sort-dropdown">
            <option value="congestion">按拥挤程度</option>
            <option value="name">按道路名称</option>
            <option value="update_time">按更新时间</option>
          </select>
        </div>
        <button class="refresh-all-btn" @click="refreshAll">
          <span class="icon">🔄</span>
          刷新全部
        </button>
      </div>
    </div>

    <div class="stats-summary" v-if="showStats">
      <div class="stat-item">
        <span class="stat-value畅通-text">{{ stats.smooth }}</span>
        <span class="stat-label">畅通</span>
      </div>
      <div class="stat-item">
        <span class="stat-value缓行-text">{{ stats.slow }}</span>
        <span class="stat-label">缓行</span>
      </div>
      <div class="stat-item">
        <span class="stat-value拥堵-text">{{ stats.congested }}</span>
        <span class="stat-label">拥堵</span>
      </div>
      <div class="stat-item">
        <span class="stat-value严重-text">{{ stats.heavy }}</span>
        <span class="stat-label">严重</span>
      </div>
    </div>

    <div class="cards-container" :class="containerClass">
      <RoadCard
        v-for="(road, index) in sortedRoads"
        :key="road.road_name || index"
        :road-data="road"
        @view-details="handleViewDetails"
        @refresh="handleRefresh"
        @share="handleShare"
      />
    </div>

    <div class="grid-footer" v-if="loadMore">
      <button class="load-more-btn" @click="handleLoadMore">
        加载更多
      </button>
    </div>

    <!-- 空状态 -->
    <div v-if="sortedRoads.length === 0" class="empty-state">
      <div class="empty-icon">🚗</div>
      <h3>暂无路况数据</h3>
      <p>请稍后再试或检查网络连接</p>
      <button class="retry-btn" @click="refreshAll">
        重新加载
      </button>
    </div>
  </div>
</template>

<script>
import RoadCard from './RoadCard.vue'

export default {
  name: 'RoadCardGrid',
  components: {
    RoadCard
  },
  props: {
    roads: {
      type: Array,
      default: () => []
    },
    showStats: {
      type: Boolean,
      default: true
    },
    loadMore: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      viewMode: 'grid',
      sortBy: 'congestion'
    }
  },
  computed: {
    sortedRoads() {
      const roads = [...this.roads]

      switch (this.sortBy) {
        case 'congestion':
          return roads.sort((a, b) => (b.evaluation_status || 0) - (a.evaluation_status || 0))
        case 'name':
          return roads.sort((a, b) => (a.road_name || '').localeCompare(b.road_name || ''))
        case 'update_time':
          return roads.sort((a, b) => {
            const timeA = new Date(a.request_time || 0).getTime()
            const timeB = new Date(b.request_time || 0).getTime()
            return timeB - timeA
          })
        default:
          return roads
      }
    },

    containerClass() {
      return `view-${this.viewMode}`
    },

    stats() {
      const roads = this.roads
      return {
        smooth: roads.filter(r => (r.evaluation_status || 0) <= 1).length,
        slow: roads.filter(r => (r.evaluation_status || 0) === 2).length,
        congested: roads.filter(r => (r.evaluation_status || 0) === 3).length,
        heavy: roads.filter(r => (r.evaluation_status || 0) === 4).length
      }
    }
  },
  methods: {
    handleViewDetails(roadData) {
      this.$emit('view-details', roadData)
    },

    handleRefresh(roadData) {
      this.$emit('refresh', roadData)
    },

    handleShare(roadData) {
      this.$emit('share', roadData)
    },

    refreshAll() {
      this.$emit('refresh-all')
    },

    handleLoadMore() {
      this.$emit('load-more')
    }
  }
}
</script>

<style scoped>
.road-card-grid {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 网格头部 */
.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.grid-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.grid-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-toggle {
  display: flex;
  background: #f1f3f4;
  border-radius: 8px;
  padding: 4px;
}

.toggle-btn {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.toggle-btn.active {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sort-dropdown {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
}

.refresh-all-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.refresh-all-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 统计摘要 */
.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9ff, #f0f2ff);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-value.畅通-text { color: #4CAF50; }
.stat-value.缓行-text { color: #8BC34A; }
.stat-value.拥堵-text { color: #FF9800; }
.stat-value.严重-text { color: #F44336; }

.stat-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

/* 卡片容器 */
.cards-container {
  display: grid;
  gap: 24px;
}

.cards-container.view-grid {
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
}

.cards-container.view-list {
  grid-template-columns: 1fr;
}

/* 网格底部 */
.grid-footer {
  text-align: center;
  margin-top: 32px;
}

.load-more-btn {
  background: white;
  border: 2px solid #667eea;
  color: #667eea;
  padding: 12px 32px;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 1rem;
  color: #666;
  margin: 0 0 24px 0;
}

.retry-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .road-card-grid {
    padding: 0 16px;
  }

  .grid-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .grid-title {
    font-size: 1.5rem;
    text-align: center;
  }

  .grid-controls {
    flex-wrap: wrap;
    justify-content: center;
  }

  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 16px;
  }

  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .cards-container.view-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .road-card-grid {
    padding: 0 12px;
  }

  .grid-title {
    font-size: 1.3rem;
  }

  .grid-controls {
    gap: 8px;
  }

  .refresh-all-btn {
    padding: 8px 12px;
    font-size: 0.85rem;
  }

  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    padding: 12px;
  }

  .stat-item {
    padding: 8px;
  }

  .stat-value {
    font-size: 1.3rem;
  }

  .stat-label {
    font-size: 0.8rem;
  }
}

/* 小屏幕优化 */
@media (max-width: 360px) {
  .road-card-grid {
    padding: 0 8px;
  }

  .stats-summary {
    grid-template-columns: 1fr 1fr;
  }

  .view-toggle {
    padding: 2px;
  }

  .toggle-btn {
    padding: 6px;
  }
}
</style>