<template>
  <div class="traffic-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">实时路况监控</h1>
      <p class="page-subtitle">智能分析城市交通状况，为您的出行提供参考</p>
    </div>

    <!-- 筛选和控制区域 -->
    <div class="filter-section">
      <div class="filter-left">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索道路或城市..."
            class="search-input"
            @input="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <span class="icon">🔍</span>
          </button>
        </div>

        <div class="filter-group">
          <label class="filter-label">城市筛选:</label>
          <select v-model="selectedCity" class="filter-select" @change="handleCityFilter">
            <option value="">全部城市</option>
            <option value="北京">北京</option>
            <option value="上海">上海</option>
            <option value="广州">广州</option>
            <option value="深圳">深圳</option>
            <option value="杭州">杭州</option>
            <option value="成都">成都</option>
            <option value="南京">南京</option>
            <option value="武汉">武汉</option>
          </select>
        </div>

        <div class="filter-group">
          <label class="filter-label">拥堵状态:</label>
          <select v-model="selectedStatus" class="filter-select" @change="handleStatusFilter">
            <option value="">全部状态</option>
            <option value="1">畅通</option>
            <option value="2">缓行</option>
            <option value="3">拥堵</option>
            <option value="4">严重拥堵</option>
          </select>
        </div>
      </div>

      <div class="filter-right">
        <button class="refresh-btn" @click="refreshData" :disabled="loading">
          <span class="icon" :class="{ 'rotating': loading }">🔄</span>
          刷新数据
        </button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-section" v-if="trafficStats">
      <div class="stat-card畅通">
        <div class="stat-number">{{ trafficStats.smoothRoads }}</div>
        <div class="stat-label">畅通</div>
      </div>
      <div class="stat-card缓行">
        <div class="stat-number">{{ trafficStats.slowRoads }}</div>
        <div class="stat-label">缓行</div>
      </div>
      <div class="stat-card拥堵">
        <div class="stat-number">{{ trafficStats.congestedRoads }}</div>
        <div class="stat-label">拥堵</div>
      </div>
      <div class="stat-card严重">
        <div class="stat-number">{{ trafficStats.heavyRoads }}</div>
        <div class="stat-label">严重拥堵</div>
      </div>
      <div class="stat-card-total">
        <div class="stat-number">{{ trafficStats.totalRoads }}</div>
        <div class="stat-label">总计道路</div>
      </div>
    </div>

    <!-- 路况卡片网格 -->
    <div class="traffic-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载路况数据...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">⚠️</div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button class="retry-btn" @click="refreshData">重新加载</button>
      </div>

      <!-- 路况数据展示 -->
      <div v-else>
        <RoadCardGrid
          :roads="formattedTrafficData"
          :show-stats="false"
          :load-more="hasMoreData"
          :loading="loadingMore"
          @view-details="handleViewDetails"
          @refresh="handleRefresh"
          @share="handleShare"
          @refresh-all="refreshData"
          @load-more="loadMoreData"
        />

        <!-- 空数据状态 -->
        <div v-if="!loading && formattedTrafficData.length === 0" class="empty-container">
          <div class="empty-icon">🚗</div>
          <h3>暂无路况数据</h3>
          <p>当前筛选条件下没有找到相关数据</p>
          <button class="clear-filter-btn" @click="clearFilters">清除筛选条件</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import RoadCardGrid from '../components/RoadCardGrid.vue'
import trafficService from '../services/trafficService.js'

export default {
  name: 'Traffic',
  components: {
    RoadCardGrid
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const loadingMore = ref(false)
    const error = ref('')
    const trafficData = ref([])
    const trafficStats = ref(null)
    const currentPage = ref(0)
    const pageSize = ref(12)
    const totalPages = ref(0)
    const hasMoreData = ref(false)

    // 筛选条件
    const searchKeyword = ref('')
    const selectedCity = ref('')
    const selectedStatus = ref('')

    // 格式化后的路况数据，适配前端组件
    const formattedTrafficData = computed(() => {
      return trafficData.value.map(item => ({
        ...item,
        // 确保字段名与前端组件期望的一致
        road_name: item.road_name || item.roadName,
        evaluation_status: item.evaluation_status || item.evaluationStatus,
        request_time: item.request_time || item.requestTime,
        congestion_distance: item.congestion_distance || item.congestionDistance
      }))
    })

    // 获取路况数据
    const fetchTrafficData = async (page = 0, isLoadMore = false) => {
      try {
        if (isLoadMore) {
          loadingMore.value = true
        } else {
          loading.value = true
          error.value = ''
        }

        let response
        const params = { page, size: pageSize.value }

        // 根据筛选条件调用不同的API
        if (selectedStatus.value) {
          response = await trafficService.getTrafficByStatus(parseInt(selectedStatus.value))
          // 状态查询返回的是数组，需要转换为分页格式
          response = {
            content: response.slice(page * pageSize.value, (page + 1) * pageSize.value),
            totalElements: response.length,
            totalPages: Math.ceil(response.length / pageSize.value),
            size: pageSize.value,
            number: page
          }
        } else if (selectedCity.value) {
          response = await trafficService.getTrafficByCity(selectedCity.value, params)
        } else if (searchKeyword.value.trim()) {
          const searchResults = await trafficService.searchTraffic(searchKeyword.value.trim())
          response = {
            content: searchResults.slice(page * pageSize.value, (page + 1) * pageSize.value),
            totalElements: searchResults.length,
            totalPages: Math.ceil(searchResults.length / pageSize.value),
            size: pageSize.value,
            number: page
          }
        } else {
          response = await trafficService.getAllTraffic(params)
        }

        // 处理响应数据
        const newTrafficData = response.content || []

        if (isLoadMore) {
          trafficData.value = [...trafficData.value, ...newTrafficData]
        } else {
          trafficData.value = newTrafficData
        }

        currentPage.value = page
        totalPages.value = response.totalPages || 0
        hasMoreData.value = page < (response.totalPages || 0) - 1

        console.log('路况数据加载成功:', response)

      } catch (err) {
        console.error('获取路况数据失败:', err)
        error.value = err.message || '获取路况数据失败'
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    // 获取统计数据
    const fetchTrafficStats = async () => {
      try {
        const stats = await trafficService.getTrafficStats()
        trafficStats.value = stats
        console.log('路况统计数据:', stats)
      } catch (err) {
        console.error('获取路况统计失败:', err)
      }
    }

    // 刷新数据
    const refreshData = () => {
      trafficData.value = []
      currentPage.value = 0
      fetchTrafficData(0, false)
      fetchTrafficStats()
    }

    // 加载更多数据
    const loadMoreData = () => {
      if (!loadingMore.value && hasMoreData.value) {
        fetchTrafficData(currentPage.value + 1, true)
      }
    }

    // 处理搜索
    const handleSearch = () => {
      trafficData.value = []
      currentPage.value = 0
      fetchTrafficData(0, false)
    }

    // 处理城市筛选
    const handleCityFilter = () => {
      trafficData.value = []
      currentPage.value = 0
      fetchTrafficData(0, false)
    }

    // 处理状态筛选
    const handleStatusFilter = () => {
      trafficData.value = []
      currentPage.value = 0
      fetchTrafficData(0, false)
    }

    // 清除筛选条件
    const clearFilters = () => {
      searchKeyword.value = ''
      selectedCity.value = ''
      selectedStatus.value = ''
      refreshData()
    }

    // 处理查看详情
    const handleViewDetails = (roadData) => {
      console.log('查看路况详情:', roadData)
      // 这里可以打开详情模态框或跳转到详情页
      alert(`查看 ${roadData.road_name} 的详细信息`)
    }

    // 处理刷新单个路况
    const handleRefresh = (roadData) => {
      console.log('刷新单个路况:', roadData)
      // 这里可以实现单个数据的刷新逻辑
      alert(`刷新 ${roadData.road_name} 的路况数据`)
    }

    // 处理分享
    const handleShare = (roadData) => {
      console.log('分享路况:', roadData)
      // 这里可以实现分享功能
      alert(`分享 ${roadData.road_name} 的路况信息`)
    }

    // 监听筛选条件变化
    watch([searchKeyword, selectedCity, selectedStatus], () => {
      // 防抖处理，避免频繁请求
      clearTimeout(searchDebounce.value)
      searchDebounce.value = setTimeout(() => {
        handleSearch()
      }, 500)
    })

    const searchDebounce = ref(null)

    // 组件挂载时获取数据
    onMounted(() => {
      refreshData()
    })

    return {
      loading,
      loadingMore,
      error,
      trafficData,
      formattedTrafficData,
      trafficStats,
      hasMoreData,
      searchKeyword,
      selectedCity,
      selectedStatus,
      refreshData,
      loadMoreData,
      handleSearch,
      handleCityFilter,
      handleStatusFilter,
      clearFilters,
      handleViewDetails,
      handleRefresh,
      handleShare
    }
  }
}
</script>

<style scoped>
.traffic-page {
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

/* 筛选区域 */
.filter-section {
  max-width: 1200px;
  margin: 0 auto 32px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  flex: 1;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 12px 45px 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 25px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-btn {
  position: absolute;
  right: 5px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: scale(1.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.filter-select {
  padding: 10px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 20px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-right {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 统计区域 */
.stats-section {
  max-width: 1200px;
  margin: 0 auto 32px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-top: 4px solid;
}

.stat-card畅通 { border-top-color: #4CAF50; }
.stat-card缓行 { border-top-color: #8BC34A; }
.stat-card拥堵 { border-top-color: #FF9800; }
.stat-card严重 { border-top-color: #F44336; }
.stat-card-total { border-top-color: #667eea; }

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-card畅通 .stat-number { color: #4CAF50; }
.stat-card缓行 .stat-number { color: #8BC34A; }
.stat-card拥堵 .stat-number { color: #FF9800; }
.stat-card严重 .stat-number { color: #F44336; }
.stat-card-total .stat-number { color: #667eea; }

.stat-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

/* 主要内容区域 */
.traffic-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.error-container h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.error-container p {
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

/* 空数据状态 */
.empty-container {
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

.empty-container h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-container p {
  font-size: 1rem;
  color: #666;
  margin: 0 0 24px 0;
}

.clear-filter-btn {
  background: #f1f3f4;
  color: #666;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-filter-btn:hover {
  background: #e8eaed;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .traffic-page {
    padding: 60px 0 30px;
  }

  .page-title {
    font-size: 2rem;
  }

  .page-subtitle {
    font-size: 1.1rem;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .filter-left {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-box {
    min-width: auto;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }

  .filter-select {
    min-width: auto;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-number {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .filter-section {
    padding: 0 12px;
  }

  .stats-section {
    padding: 0 12px;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .stat-card {
    padding: 16px 12px;
  }

  .stat-number {
    font-size: 1.3rem;
  }

  .stat-label {
    font-size: 0.8rem;
  }

  .traffic-content {
    padding: 0 12px;
  }
}
</style>