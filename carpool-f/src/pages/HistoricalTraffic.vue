<template>
  <div class="historical-traffic-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">历史路况分析</h1>
      <p class="page-subtitle">分析指定道路在一段时间内的路况变化趋势，为出行规划提供数据支持</p>
    </div>

    <!-- 主要内容区域 -->
    <div class="page-content">
      <HistoricalTrafficCard
        :initial-city="initialCity"
        :initial-road="initialRoad"
        :default-time-range="defaultTimeRange"
        :chart-height="400"
      />
    </div>

    <!-- 使用说明 -->
    <div class="usage-tips">
      <h3>使用说明</h3>
      <div class="tips-grid">
        <div class="tip-item">
          <div class="tip-icon">📍</div>
          <div class="tip-content">
            <h4>选择道路</h4>
            <p>先选择城市，然后选择要分析的具体道路</p>
          </div>
        </div>
        <div class="tip-item">
          <div class="tip-icon">⏰</div>
          <div class="tip-content">
            <h4>时间范围</h4>
            <p>支持1小时到7天的历史数据分析</p>
          </div>
        </div>
        <div class="tip-item">
          <div class="tip-icon">📊</div>
          <div class="tip-content">
            <h4>趋势图表</h4>
            <p>显示拥堵指数和平均速度的变化趋势</p>
          </div>
        </div>
        <div class="tip-item">
          <div class="tip-icon">📈</div>
          <div class="tip-content">
            <h4>统计分析</h4>
            <p>提供平均拥堵指数、拥堵时长占比等统计信息</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import HistoricalTrafficCard from '../components/HistoricalTrafficCard.vue'

export default {
  name: 'HistoricalTraffic',
  components: {
    HistoricalTrafficCard
  },
  setup() {
    // 可以从URL参数或路由获取初始值
    const initialCity = ref('') // 留空，让组件自动选择第一个可用城市
    const initialRoad = ref('')
    const defaultTimeRange = ref('1h')

    return {
      initialCity,
      initialRoad,
      defaultTimeRange
    }
  }
}
</script>

<style scoped>
.historical-traffic-page {
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

/* 主要内容区域 */
.page-content {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

/* 使用说明 */
.usage-tips {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.usage-tips h3 {
  text-align: center;
  font-size: 1.8rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 32px 0;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.tip-item {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.tip-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.tip-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.tip-content h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.tip-content p {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .historical-traffic-page {
    padding: 60px 0 30px;
  }

  .page-title {
    font-size: 2rem;
  }

  .page-subtitle {
    font-size: 1.1rem;
  }

  .tips-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .tip-item {
    padding: 20px;
  }

  .tip-icon {
    font-size: 1.5rem;
  }

  .usage-tips {
    padding: 30px 20px;
  }
}

@media (max-width: 480px) {
  .page-header {
    margin-bottom: 30px;
  }

  .usage-tips h3 {
    font-size: 1.5rem;
  }

  .tip-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}
</style>