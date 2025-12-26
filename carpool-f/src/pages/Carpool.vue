<template>
  <div class="carpool-page">
    <div class="page-header">
      <h1 class="page-title">同城拼车</h1>
      <p class="page-subtitle">寻找附近的拼车伙伴,共享出行,节约成本</p>
    </div>

    <!-- 筛选器 -->
    <div class="filters-section">
      <div class="filter-group">
        <label class="filter-label">
          <span class="label-icon">📍</span>
          <span>出发地</span>
        </label>
        <input
          v-model="filters.startLocation"
          type="text"
          class="filter-input"
          placeholder="输入出发地"
          @input="handleFilterChange"
        >
      </div>

      <div class="filter-group">
        <label class="filter-label">
          <span class="label-icon">🎯</span>
          <span>目的地</span>
        </label>
        <input
          v-model="filters.endLocation"
          type="text"
          class="filter-input"
          placeholder="输入目的地"
          @input="handleFilterChange"
        >
      </div>

      <div class="filter-group">
        <label class="filter-label">
          <span class="label-icon">👥</span>
          <span>类型</span>
        </label>
        <select v-model="filters.hasCar" class="filter-select" @change="handleFilterChange">
          <option :value="null">全部</option>
          <option :value="true">车主</option>
          <option :value="false">乘客</option>
        </select>
      </div>

      <div class="filter-group">
        <label class="filter-label">
          <span class="label-icon">📅</span>
          <span>状态</span>
        </label>
        <select v-model="filters.status" class="filter-select" @change="handleFilterChange">
          <option value="">全部</option>
          <option value="等待匹配">等待匹配</option>
          <option value="已匹配">已匹配</option>
          <option value="已完成">已完成</option>
        </select>
      </div>

      <button class="reset-btn" @click="resetFilters" v-if="hasActiveFilters">
        重置筛选
      </button>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon">🚗</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalRequests }}</div>
          <div class="stat-label">总需求</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingRequests }}</div>
          <div class="stat-label">等待匹配</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.matchedRequests }}</div>
          <div class="stat-label">已匹配</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.displayedRequests }}</div>
          <div class="stat-label">显示中</div>
        </div>
      </div>
    </div>

    <!-- 拼车卡片网格 -->
    <CarpoolCardGrid
      :requests="filteredRequests"
      :loading="loading"
      @contact="handleContact"
    />
  </div>
</template>

<script>
import CarpoolCardGrid from '@/components/CarpoolCardGrid.vue'
import trafficService from '@/services/trafficService'

export default {
  name: 'Carpool',
  components: {
    CarpoolCardGrid
  },
  data() {
    return {
      loading: false,
      requests: [],
      filters: {
        startLocation: '',
        endLocation: '',
        hasCar: null,
        status: ''
      }
    }
  },
  computed: {
    filteredRequests() {
      let filtered = [...this.requests]

      if (this.filters.startLocation) {
        filtered = filtered.filter(req =>
          req.startLocation && req.startLocation.includes(this.filters.startLocation)
        )
      }

      if (this.filters.endLocation) {
        filtered = filtered.filter(req =>
          req.endLocation && req.endLocation.includes(this.filters.endLocation)
        )
      }

      if (this.filters.hasCar !== null) {
        filtered = filtered.filter(req => req.hasCar === this.filters.hasCar)
      }

      if (this.filters.status) {
        filtered = filtered.filter(req =>
          req.statusDesc && req.statusDesc.includes(this.filters.status)
        )
      }

      return filtered
    },

    stats() {
      const total = this.requests.length
      const pending = this.requests.filter(r =>
        r.statusDesc && r.statusDesc.includes('等待匹配')
      ).length
      const matched = this.requests.filter(r =>
        r.statusDesc && r.statusDesc.includes('已匹配')
      ).length

      return {
        totalRequests: total,
        pendingRequests: pending,
        matchedRequests: matched,
        displayedRequests: this.filteredRequests.length
      }
    },

    hasActiveFilters() {
      return this.filters.startLocation ||
             this.filters.endLocation ||
             this.filters.hasCar !== null ||
             this.filters.status
    }
  },
  mounted() {
    this.loadCarpoolRequests()
  },
  methods: {
    async loadCarpoolRequests() {
      this.loading = true
      try {
        // 调用实际的API获取拼车需求
        const response = await trafficService.getCarpoolRequests(this.filters)
        this.requests = response
      } catch (error) {
        console.error('加载拼车需求失败:', error)
        this.$message?.error('加载拼车需求失败,请稍后重试')
      } finally {
        this.loading = false
      }
    },

    handleFilterChange() {
      // 筛选条件变化时重新调用API获取数据
      this.loadCarpoolRequests()
    },

    resetFilters() {
      this.filters = {
        startLocation: '',
        endLocation: '',
        hasCar: null,
        status: ''
      }
    },

    async handleContact(request) {
      try {
        // 调用实际的API联系拼车用户
        await trafficService.contactCarpoolUser(request.id, { phoneNumber: request.phoneNumber })
        this.$message?.success(`正在联系: ${request.phoneNumber}`)
      } catch (error) {
        console.error('联系拼车用户失败:', error)
        this.$message?.error('联系拼车用户失败,请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.carpool-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 80px 0 40px;
}

/* 页面头部 */
.page-header {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

 .page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1.2rem;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 筛选器区域 */
.filters-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
}

.label-icon {
  font-size: 1.1rem;
}

.filter-input,
.filter-select {
  padding: 10px 16px;
  border: 2px solid #e8eaed;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #2c3e50;
  transition: all 0.3s ease;
  outline: none;
  width: 100%;
}

.filter-input:focus,
.filter-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-input::placeholder {
  color: #999;
}

.reset-btn {
  padding: 10px 20px;
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e8eaed;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 42px;
}

.reset-btn:hover {
  background: #e9ecef;
  border-color: #dee2e6;
  color: #2c3e50;
}

/* 统计信息区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  color: #7f8c8d;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .carpool-page {
    padding: 24px;
  }

  .filters-section {
    padding: 20px;
  }

  .filter-group {
    min-width: 180px;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .carpool-page {
    padding: 16px;
  }

  .page-header h2 {
    font-size: 1.5rem;
  }

  .page-header p {
    font-size: 0.9rem;
  }

  .filters-section {
    padding: 16px;
    gap: 16px;
  }

  .filter-group {
    min-width: 100%;
  }

  .stats-section {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .reset-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .page-header {
    margin-bottom: 24px;
  }

  .filters-section {
    margin-bottom: 16px;
  }

  .stats-section {
    margin-bottom: 24px;
  }
}
</style>
