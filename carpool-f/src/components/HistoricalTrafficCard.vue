<template>
  <div class="historical-traffic-card">
    <div class="card-header">
      <h3 class="card-title">历史路况分析</h3>
      <div class="card-actions">
        <button class="refresh-btn" @click="refreshData" :disabled="loading">
          <span class="icon" :class="{ 'rotating': loading }">🔄</span>
        </button>
        <button class="expand-btn" @click="toggleExpanded">
          <span class="icon">{{ expanded ? '▼' : '▲' }}</span>
        </button>
      </div>
    </div>

    <div class="card-content" v-show="expanded">
      <!-- 道路和时间范围选择 -->
      <div class="selection-section">
        <div class="road-selection">
          <label class="selection-label">选择道路:</label>
          <div class="road-input-group">
            <select v-model="selectedCity" class="city-select" @change="onCityChange">
              <option value="">选择城市</option>
              <option v-for="city in availableCities" :key="city" :value="city">
                {{ city }}
              </option>
            </select>
            <select
              v-model="selectedRoad"
              class="road-select"
              :disabled="!selectedCity || loadingRoads"
              @change="onRoadChange"
            >
              <option value="">选择道路</option>
              <option v-for="road in availableRoads" :key="road" :value="road">
                {{ road }}
              </option>
            </select>
          </div>
        </div>

        <div class="time-range-selection">
          <label class="selection-label">时间范围:</label>
          <div class="time-range-buttons">
            <button
              v-for="range in timeRanges"
              :key="range.value"
              class="time-range-btn"
              :class="{ active: selectedTimeRange === range.value }"
              @click="selectTimeRange(range.value)"
            >
              {{ range.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 图表显示区域 -->
      <div class="chart-section" v-if="selectedRoad && selectedCity">
        <div class="chart-header">
          <h4 class="chart-title">
            {{ selectedRoad }} - {{ selectedCity }} 路况趋势
          </h4>
          <div class="chart-legend">
            <span class="legend-item畅通">畅通</span>
            <span class="legend-item缓行">缓行</span>
            <span class="legend-item拥堵">拥堵</span>
            <span class="legend-item严重拥堵">严重拥堵</span>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="chart-loading">
          <div class="loading-spinner"></div>
          <p>正在加载历史数据...</p>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="chart-error">
          <div class="error-icon">⚠️</div>
          <p>{{ error }}</p>
          <button class="retry-btn" @click="fetchHistoricalData">重新加载</button>
        </div>

        <!-- ECharts 图表 -->
        <div v-else-if="historicalData.length > 0" class="chart-container">
          <v-chart
            :option="chartOption"
            :style="{ height: chartHeight + 'px' }"
            autoresize
          />
        </div>

        <!-- 无数据状态 -->
        <div v-else class="chart-no-data">
          <div class="no-data-icon">📊</div>
          <p>暂无历史数据</p>
        </div>
      </div>

      <!-- 提示信息 -->
      <div v-if="!selectedRoad || !selectedCity" class="selection-prompt">
        <div class="prompt-icon">📍</div>
        <p>请选择要分析的道路和城市</p>
      </div>

      <!-- 统计信息 -->
      <div v-if="historicalData.length > 0" class="statistics-section">
        <div class="stat-card">
          <div class="stat-label">平均拥堵指数</div>
          <div class="stat-value" :class="getCongestionClass(averageCongestion)">
            {{ averageCongestion.toFixed(1) }}
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">拥堵时长占比</div>
          <div class="stat-value">{{ congestionPercentage }}%</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">数据点数量</div>
          <div class="stat-value">{{ historicalData.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均速度</div>
          <div class="stat-value">{{ averageSpeed }} km/h</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import VChart from 'vue-echarts'
import 'echarts/lib/chart/line'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/grid'
import trafficService from '../services/trafficService.js'

export default {
  name: 'HistoricalTrafficCard',
  components: {
    VChart
  },
  props: {
    initialCity: {
      type: String,
      default: ''
    },
    initialRoad: {
      type: String,
      default: ''
    },
    defaultTimeRange: {
      type: String,
      default: '1h'
    },
    chartHeight: {
      type: Number,
      default: 300
    }
  },
  setup(props) {
    // 响应式数据
    const loading = ref(false)
    const loadingRoads = ref(false)
    const error = ref('')
    const expanded = ref(true)
    const historicalData = ref([])

    // 选择状态
    const selectedCity = ref(props.initialCity)
    const selectedRoad = ref(props.initialRoad)
    const selectedTimeRange = ref(props.defaultTimeRange)

    // 可用选项
    const availableCities = ref([])
    const availableRoads = ref([])

    // 时间范围选项
    const timeRanges = [
      { label: '1小时', value: '1h' },
      { label: '6小时', value: '6h' },
      { label: '12小时', value: '12h' },
      { label: '1天', value: '1d' },
      { label: '3天', value: '3d' },
      { label: '7天', value: '7d' }
    ]

    // 统计数据
    const statistics = reactive({
      averageCongestion: 0,
      congestionPercentage: 0,
      averageSpeed: 0
    })

    // 计算属性
    const averageCongestion = computed(() => {
      if (historicalData.value.length === 0) return 0
      const sum = historicalData.value.reduce((acc, item) => acc + item.evaluation_status, 0)
      return sum / historicalData.value.length
    })

    const congestionPercentage = computed(() => {
      if (historicalData.value.length === 0) return 0
      const congestedCount = historicalData.value.filter(
        item => item.evaluation_status >= 2
      ).length
      return Math.round((congestedCount / historicalData.value.length) * 100)
    })

    const averageSpeed = computed(() => {
      const speedData = historicalData.value.filter(item => item.speed)
      if (speedData.length === 0) return 0
      const sum = speedData.reduce((acc, item) => acc + item.speed, 0)
      return Math.round(sum / speedData.length)
    })

    // ECharts 配置
    const chartOption = computed(() => {
      if (historicalData.value.length === 0) return {}

      const timeData = historicalData.value.map(item =>
        new Date(item.request_time).toLocaleString('zh-CN', {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      )

      const statusData = historicalData.value.map(item => item.evaluation_status)
      const speedData = historicalData.value.map(item => item.speed || null)

      return {
        title: {
          show: false
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            let result = `<div style="font-weight: bold; margin-bottom: 8px;">${params[0].axisValue}</div>`

            params.forEach(param => {
              if (param.seriesName === '拥堵指数') {
                const statusText = ['未知', '畅通', '缓行', '拥堵', '严重拥堵'][param.value] || '未知'
                result += `<div style="margin: 4px 0;">
                  <span style="display: inline-block; width: 12px; height: 12px; background: ${param.color}; border-radius: 2px; margin-right: 8px;"></span>
                  ${param.seriesName}: ${param.value} (${statusText})
                </div>`
              } else if (param.seriesName === '平均速度' && param.value !== null) {
                result += `<div style="margin: 4px 0;">
                  <span style="display: inline-block; width: 12px; height: 12px; background: ${param.color}; border-radius: 2px; margin-right: 8px;"></span>
                  ${param.seriesName}: ${param.value} km/h
                </div>`
              }
            })

            return result
          }
        },
        legend: {
          show: false
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: timeData,
          axisLabel: {
            rotate: 45,
            fontSize: 11
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '拥堵指数',
            min: 0,
            max: 4,
            interval: 1,
            axisLabel: {
              formatter: function(value) {
                return ['', '畅通', '缓行', '拥堵', '严重拥堵'][value] || value
              }
            }
          },
          {
            type: 'value',
            name: '速度 (km/h)',
            min: 0,
            axisLabel: {
              formatter: '{value} km/h'
            }
          }
        ],
        series: [
          {
            name: '拥堵指数',
            type: 'line',
            yAxisIndex: 0,
            data: statusData,
            smooth: true,
            lineStyle: {
              width: 3,
              color: '#667eea'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
                  { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
                ]
              }
            },
            itemStyle: {
              color: '#667eea'
            }
          },
          {
            name: '平均速度',
            type: 'line',
            yAxisIndex: 1,
            data: speedData,
            smooth: true,
            lineStyle: {
              width: 2,
              color: '#4CAF50'
            },
            itemStyle: {
              color: '#4CAF50'
            }
          }
        ]
      }
    })

    // 方法
    const getCongestionClass = (value) => {
      if (value <= 1) return 'status-smooth'
      if (value <= 2) return 'status-slow'
      if (value <= 3) return 'status-congested'
      return 'status-heavy'
    }

    const toggleExpanded = () => {
      expanded.value = !expanded.value
    }

    const selectTimeRange = (range) => {
      selectedTimeRange.value = range
      if (selectedRoad.value && selectedCity.value) {
        fetchHistoricalData()
      }
    }

    const onCityChange = async () => {
      selectedRoad.value = ''
      availableRoads.value = []
      historicalData.value = []

      if (selectedCity.value) {
        await fetchRoadsForCity()
      }
    }

    const onRoadChange = () => {
      if (selectedRoad.value && selectedCity.value) {
        fetchHistoricalData()
      }
    }

    const fetchCities = async () => {
      try {
        const cities = await trafficService.getSupportedCities()
        availableCities.value = cities
        console.log('获取城市列表成功:', cities)
      } catch (err) {
        console.error('获取城市列表失败:', err)
        availableCities.value = []
        // 如果API失败，提供一些默认的城市选项
        availableCities.value = ['上海', '北京', '广州', '深圳', '杭州', '成都', '南京', '武汉']
      }
    }

    const fetchRoadsForCity = async () => {
      if (!selectedCity.value) return

      try {
        loadingRoads.value = true
        const roads = await trafficService.getRoadsByCity(selectedCity.value)
        availableRoads.value = roads
        console.log(`获取${selectedCity.value}道路列表成功:`, roads)
      } catch (err) {
        console.error('获取道路列表失败:', err)
        availableRoads.value = []
        error.value = err.message || '获取道路列表失败'
      } finally {
        loadingRoads.value = false
      }
    }

    const fetchHistoricalData = async () => {
      if (!selectedRoad.value || !selectedCity.value) return

      try {
        loading.value = true
        error.value = ''

        // 计算时间范围
        const now = new Date()
        const timeRangeMap = {
          '1h': 1 * 60 * 60 * 1000,
          '6h': 6 * 60 * 60 * 1000,
          '12h': 12 * 60 * 60 * 1000,
          '1d': 24 * 60 * 60 * 1000,
          '3d': 3 * 24 * 60 * 60 * 1000,
          '7d': 7 * 24 * 60 * 60 * 1000
        }

        const timeRange = timeRangeMap[selectedTimeRange.value] || timeRangeMap['1h']
        const startTime = new Date(now.getTime() - timeRange).toISOString()

        // 调用历史数据API（需要后端实现）
        const response = await trafficService.getHistoricalTraffic({
          roadName: selectedRoad.value,
          city: selectedCity.value,
          startTime: startTime,
          endTime: now.toISOString()
        })

        historicalData.value = response || []
        console.log('历史数据加载成功:', historicalData.value)

      } catch (err) {
        console.error('获取历史数据失败:', err)
        error.value = err.message || '获取历史数据失败'
        historicalData.value = []
      } finally {
        loading.value = false
      }
    }

    const refreshData = () => {
      if (selectedRoad.value && selectedCity.value) {
        fetchHistoricalData()
      }
    }

    // 监听器
    watch([selectedRoad, selectedCity], () => {
      if (selectedRoad.value && selectedCity.value) {
        fetchHistoricalData()
      }
    })

    // 生命周期
    onMounted(async () => {
      // 先获取城市列表
      await fetchCities()

      // 如果有初始城市，获取该城市的道路列表
      if (props.initialCity) {
        fetchRoadsForCity()
      }
    })

    return {
      loading,
      loadingRoads,
      error,
      expanded,
      historicalData,
      selectedCity,
      selectedRoad,
      selectedTimeRange,
      availableCities,
      availableRoads,
      timeRanges,
      averageCongestion,
      congestionPercentage,
      averageSpeed,
      chartOption,
      getCongestionClass,
      toggleExpanded,
      selectTimeRange,
      onCityChange,
      onRoadChange,
      refreshData,
      fetchCities
    }
  }
}
</script>

<style scoped>
.historical-traffic-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.historical-traffic-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.card-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: white;
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn,
.expand-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.refresh-btn:hover:not(:disabled),
.expand-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
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

/* 卡片内容 */
.card-content {
  padding: 24px;
}

/* 选择区域 */
.selection-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.road-selection,
.time-range-selection {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selection-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
}

.road-input-group {
  display: flex;
  gap: 12px;
}

.city-select,
.road-select {
  flex: 1;
  padding: 10px 14px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.city-select:focus,
.road-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.road-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.time-range-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-range-btn {
  padding: 8px 16px;
  border: 2px solid #e1e5e9;
  background: white;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.time-range-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.time-range-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
}

/* 图表区域 */
.chart-section {
  margin-bottom: 24px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.legend-item {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.legend-item畅通 { background: #E8F5E8; color: #2E7D32; }
.legend-item缓行 { background: #F0F8E8; color: #689F38; }
.legend-item拥堵 { background: #FFF8E1; color: #F57C00; }
.legend-item严重拥堵 { background: #FFEBEE; color: #C62828; }

/* 加载、错误、无数据状态 */
.chart-loading,
.chart-error,
.chart-no-data {
  text-align: center;
  padding: 40px 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #e1e5e9;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon,
.no-data-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}

.retry-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
}

.retry-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.chart-container {
  background: white;
  border-radius: 12px;
  border: 1px solid #e1e5e9;
  overflow: hidden;
}

/* 选择提示 */
.selection-prompt {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8f9ff, #f0f2ff);
  border-radius: 12px;
  border: 2px dashed #667eea;
}

.prompt-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.selection-prompt p {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

/* 统计信息 */
.statistics-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, #f8f9ff, #ffffff);
  padding: 16px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid #e1e5e9;
}

.stat-label {
  font-size: 0.75rem;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.stat-value.status-smooth { color: #4CAF50; }
.stat-value.status-slow { color: #8BC34A; }
.stat-value.status-congested { color: #FF9800; }
.stat-value.status-heavy { color: #F44336; }

/* 响应式设计 */
@media (max-width: 768px) {
  .selection-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .road-input-group {
    flex-direction: column;
  }

  .time-range-buttons {
    justify-content: center;
  }

  .chart-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .statistics-section {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 12px;
  }

  .card-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .time-range-buttons {
    gap: 6px;
  }

  .time-range-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }

  .statistics-section {
    grid-template-columns: 1fr;
  }
}
</style>