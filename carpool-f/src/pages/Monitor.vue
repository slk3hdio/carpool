<template>
  <div class="monitor-root">
    <div class="monitor-header">
      <span class="monitor-title">智能交通监控大屏</span>
      <div class="monitor-city-switch">
        <button v-for="city in cities" :key="city" :class="{active: city === currentCity}" @click="switchCity(city)">{{ city }}</button>
      </div>
    </div>
    <div class="monitor-main">
      <div class="monitor-map-panel">
        <AMapContaioner :city="currentCity" />
        <div class="monitor-legend">
          <span><span class="legend-dot green"></span>畅通</span>
          <span><span class="legend-dot yellow"></span>缓行</span>
          <span><span class="legend-dot orange"></span>拥堵</span>
          <span><span class="legend-dot red"></span>严重拥堵</span>
        </div>
      </div>
      <div class="monitor-side-panel">
        <div class="monitor-card">Flink实时数据流 <span class="monitor-rate">{{ rate }} 条/秒</span></div>
        <div class="monitor-card">实时指标统计</div>
        <div class="monitor-card">拥堵路段Top5</div>
      </div>
    </div>
    <div class="monitor-bottom-panel">
      <div class="monitor-card">实时趋势分析（Flink滑动窗口）</div>
      <div class="monitor-time-range">
        <button v-for="t in timeRanges" :key="t" :class="{active: t === currentRange}" @click="switchRange(t)">{{ t }}</button>
      </div>
      <!-- 这里可插入趋势图表组件 -->
    </div>
  </div>
</template>

<script>
import AMapContaioner from '../components/AMapContaioner.vue';
import { ref } from 'vue';

export default {
  name: 'Monitor',
  components: { AMapContaioner },
  setup() {
    const cities = ['上海', '北京'];
    const currentCity = ref('上海');
    const rate = ref(0);
    const timeRanges = ['5分钟', '15分钟', '30分钟', '1小时'];
    const currentRange = ref('15分钟');
    const switchCity = (city) => { currentCity.value = city; };
    const switchRange = (t) => { currentRange.value = t; };
    return { cities, currentCity, rate, timeRanges, currentRange, switchCity, switchRange };
  }
};
</script>

<style scoped>
.monitor-root {
  background: #181c2a;
  color: #fff;
  min-height: 100vh;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}
.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 40px 12px 40px;
  background: #23263a;
}
.monitor-title {
  font-size: 2.2rem;
  font-weight: bold;
  letter-spacing: 2px;
}
.monitor-city-switch button {
  background: #23263a;
  color: #fff;
  border: 1px solid #444;
  border-radius: 6px;
  margin-left: 12px;
  padding: 6px 18px;
  font-size: 1rem;
  cursor: pointer;
}
.monitor-city-switch .active {
  background: #3a7afe;
  color: #fff;
  border-color: #3a7afe;
}
.monitor-main {
  display: flex;
  padding: 24px 40px;
}
.monitor-map-panel {
  flex: 3;
  background: #22263a;
  border-radius: 12px;
  margin-right: 24px;
  padding: 18px 18px 0 18px;
  position: relative;
}
.monitor-legend {
  position: absolute;
  left: 24px;
  bottom: 18px;
  display: flex;
  gap: 18px;
  background: rgba(24,28,42,0.8);
  padding: 6px 18px;
  border-radius: 8px;
  font-size: 1rem;
}
.legend-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  margin-right: 6px;
}
.green { background: #00FF00; }
.yellow { background: #FFFF00; }
.orange { background: #FFA500; }
.red { background: #FF0000; }
.monitor-side-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.monitor-card {
  background: #23263a;
  border-radius: 10px;
  padding: 18px 20px;
  margin-bottom: 8px;
  font-size: 1.1rem;
  min-height: 60px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.monitor-rate {
  float: right;
  color: #3a7afe;
  font-weight: bold;
}
.monitor-bottom-panel {
  margin: 24px 40px 0 40px;
  background: #23263a;
  border-radius: 12px;
  padding: 18px 24px;
}
.monitor-time-range {
  margin-top: 12px;
}
.monitor-time-range button {
  background: #23263a;
  color: #fff;
  border: 1px solid #444;
  border-radius: 6px;
  margin-right: 12px;
  padding: 6px 18px;
  font-size: 1rem;
  cursor: pointer;
}
.monitor-time-range .active {
  background: #3a7afe;
  color: #fff;
  border-color: #3a7afe;
}
</style>
