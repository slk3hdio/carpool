<template>
	<div class="log-dashboard">
		<!-- 顶部统计卡片区 -->
		<div class="top-cards">
			<div class="stat-card info">
				<div class="card-title">INFO</div>
				<div class="card-value">{{ logLevels.INFO }}</div>
			</div>
			<div class="stat-card warn">
				<div class="card-title">WARN</div>
				<div class="card-value">{{ logLevels.WARN }}</div>
			</div>
			<div class="stat-card error">
				<div class="card-title">ERROR</div>
				<div class="card-value">{{ logLevels.ERROR }}</div>
			</div>
			<div class="stat-card debug">
				<div class="card-title">DEBUG</div>
				<div class="card-value">{{ logLevels.DEBUG }}</div>
			</div>
		</div>

		<div class="main-content">
			<!-- 左侧运维指标区 -->
			<div class="side-panel">
				<div class="panel-title">运维指标</div>
				<div class="panel-section">
					<div>服务启动总耗时: <span class="highlight">{{ metrics.startupTime }}</span></div>
					<div>Root WebAppContext初始化: <span class="highlight">{{ metrics.rootInitTime }}</span></div>
				</div>
				<div class="panel-title">数据库连接池</div>
				<div ref="dbChart" class="chart-box small"></div>
			</div>

			<!-- 中间日志等级统计图 -->
			<div class="center-panel">
				<div class="chart-title">日志等级统计</div>
				<div ref="levelChart" class="chart-box"></div>
				<div class="qps-panel">
					<div class="chart-title" style="margin-bottom:0;">QPS (每秒查询率)</div>
					<div ref="qpsChart" class="chart-box small"></div>
				</div>
				<div class="qps-panel">
					<div class="chart-title" style="margin-bottom:0;">数据流量 (B/s)</div>
					<div ref="trafficChart" class="chart-box small"></div>
				</div>
			</div>

			<!-- 右侧包名日志排行和异常统计 -->
			<div class="side-panel right">
				<div class="panel-title">包名日志数量排行</div>
				<div ref="pkgChart" class="chart-box small"></div>
				<div class="panel-title">异常类型统计</div>
				<div ref="exChart" class="chart-box small"></div>
				<div class="panel-title">公告</div>
				<div class="panel-section">
					<div v-if="exceptionTotal === 0">暂无异常，系统运行正常</div>
					<div v-else>请关注异常类型统计</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';

const logLevels = reactive({ INFO: 0, WARN: 0, ERROR: 0, DEBUG: 0, TRACE: 0 });
const metrics = reactive({
  startupTime: '',
  rootInitTime: '',
  db: { create: 0, close: 0, poolStart: 0, poolClose: 0, error: 0 },
});

const qps = reactive({ INFO: 0, WARN: 0, ERROR: 0, DEBUG: 0, TRACE: 0 });
const traffic = ref(0);
const pkgLogs = ref([]);
const exceptions = ref([]);
const exceptionTotal = ref(0);

// 历史数据存储（最多保留60秒的数据）
const MAX_DATA_POINTS = 10;
const timeData = ref([]);
const qpsHistoryData = reactive({
  INFO: [],
  WARN: [],
  ERROR: [],
  DEBUG: [],
  TRACE: []
});
const trafficHistoryData = ref([]);

const levelChart = ref(null);
const pkgChart = ref(null);
const exChart = ref(null);
const dbChart = ref(null);
const qpsChart = ref(null);
const trafficChart = ref(null);

let levelChartInst = null;
let pkgChartInst = null;
let exChartInst = null;
let dbChartInst = null;
let qpsChartInst = null;
let trafficChartInst = null;
let refreshTimer = null;

function parseLogData(data) {
  // 清空之前的数据
  pkgLogs.value = [];
  exceptions.value = [];
  
  // === 1. 日志等级统计Map ===
  const logLevelMatch = data.match(/--- 日志等级统计Map ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (logLevelMatch) {
    const sectionText = logLevelMatch[1];
    const infoMatch = sectionText.match(/INFO:\s*(\d+)/);
    const warnMatch = sectionText.match(/WARN:\s*(\d+)/);
    const errorMatch = sectionText.match(/ERROR:\s*(\d+)/);
    const debugMatch = sectionText.match(/DEBUG:\s*(\d+)/);
    const traceMatch = sectionText.match(/TRACE:\s*(\d+)/);
    
    if (infoMatch) logLevels.INFO = parseInt(infoMatch[1]);
    if (warnMatch) logLevels.WARN = parseInt(warnMatch[1]);
    if (errorMatch) logLevels.ERROR = parseInt(errorMatch[1]);
    if (debugMatch) logLevels.DEBUG = parseInt(debugMatch[1]);
    if (traceMatch) logLevels.TRACE = parseInt(traceMatch[1]);
  }
  
  // === 2. 启动耗时与关键阶段耗时 ===
  const startupMatch = data.match(/--- 启动耗时与关键阶段耗时 ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (startupMatch) {
    const sectionText = startupMatch[1];
    const totalMatch = sectionText.match(/服务启动总耗时:\s*([\d.]+)\s*秒/);
    const rootMatch = sectionText.match(/Root WebApplicationContext initialization:\s*(\d+)\s*ms/);
    
    if (totalMatch) metrics.startupTime = `${totalMatch[1]} 秒`;
    if (rootMatch) metrics.rootInitTime = `${rootMatch[1]} ms`;
  }
  
  // === 3. 数据库连接池（HikariCP）状态统计 ===
  const dbMatch = data.match(/--- 数据库连接池（HikariCP）状态统计 ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (dbMatch) {
    const sectionText = dbMatch[1];
    const createdMatch = sectionText.match(/连接创建次数:\s*(\d+)/);
    const closedMatch = sectionText.match(/连接关闭次数:\s*(\d+)/);
    const startedMatch = sectionText.match(/连接池启动次数:\s*(\d+)/);
    const poolClosedMatch = sectionText.match(/连接池关闭次数:\s*(\d+)/);
    const errorsMatch = sectionText.match(/连接相关异常\/错误:\s*(\d+)/);
    
    if (createdMatch) metrics.db.create = parseInt(createdMatch[1]);
    if (closedMatch) metrics.db.close = parseInt(closedMatch[1]);
    if (startedMatch) metrics.db.poolStart = parseInt(startedMatch[1]);
    if (poolClosedMatch) metrics.db.poolClose = parseInt(poolClosedMatch[1]);
    if (errorsMatch) metrics.db.error = parseInt(errorsMatch[1]);
  }
  
  // === 4. QPS (每个Source) ===
  const qpsMatch = data.match(/--- QPS \(每个Source\) ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (qpsMatch) {
    const sectionText = qpsMatch[1];
    const qpsInfoMatch = sectionText.match(/INFO:\s*([\d.]+)/);
    const qpsWarnMatch = sectionText.match(/WARN:\s*([\d.]+)/);
    const qpsErrorMatch = sectionText.match(/ERROR:\s*([\d.]+)/);
    const qpsDebugMatch = sectionText.match(/DEBUG:\s*([\d.]+)/);
    const qpsTraceMatch = sectionText.match(/TRACE:\s*([\d.]+)/);
    const throughputMatch = sectionText.match(/数据流量（B\/s）[：:]\s*([\d.]+)/);
    
    if (qpsInfoMatch) qps.INFO = parseFloat(qpsInfoMatch[1]);
    if (qpsWarnMatch) qps.WARN = parseFloat(qpsWarnMatch[1]);
    if (qpsErrorMatch) qps.ERROR = parseFloat(qpsErrorMatch[1]);
    if (qpsDebugMatch) qps.DEBUG = parseFloat(qpsDebugMatch[1]);
    if (qpsTraceMatch) qps.TRACE = parseFloat(qpsTraceMatch[1]);
    if (throughputMatch) traffic.value = parseFloat(throughputMatch[1]);
  }
  
  // === 5. 各包名日志数量 ===
  const packageMatch = data.match(/--- 各包名日志数量 ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (packageMatch) {
    const sectionText = packageMatch[1];
    const lines = sectionText.trim().split('\n');
    lines.forEach(line => {
      const lineMatch = line.trim().match(/^(.+?):\s*(\d+)$/);
      if (lineMatch) {
        const packageName = lineMatch[1];
        const count = parseInt(lineMatch[2]);
        pkgLogs.value.push({ name: packageName, value: count });
      }
    });
  }
  
  // === 6. 异常类型统计 ===
  const exceptionMatch = data.match(/--- 异常类型统计 ---\s*\n([\s\S]*?)(?=\n---|$)/);
  if (exceptionMatch) {
    const sectionText = exceptionMatch[1];
    const lines = sectionText.trim().split('\n');
    lines.forEach(line => {
      const lineMatch = line.trim().match(/^(.+?):\s*(\d+)$/);
      if (lineMatch) {
        const exceptionType = lineMatch[1];
        const count = parseInt(lineMatch[2]);
        exceptions.value.push({ name: exceptionType, value: count });
      }
    });
  }
  
  // 计算异常总数
  exceptionTotal.value = exceptions.value.reduce((sum, e) => sum + e.value, 0);
}

async function fetchDataAndUpdateCharts() {
	try {
		const get_url = 'http://localhost:8080/api/log/level-count';
		const res = await axios.get(get_url);
		const logData = res.data.content;
		
		console.log('=== 读取的文件数据 ===');
		console.log(logData);
		
		parseLogData(logData);
		console.log('=== 解析后的数据 ===');
		console.log('logLevels:', logLevels);
		console.log('metrics:', metrics);
		console.log('qps:', qps);
		console.log('traffic:', traffic.value);
		console.log('pkgLogs:', pkgLogs.value);
		console.log('exceptions:', exceptions.value);

		// 添加当前时间点的数据到历史记录
		const now = new Date();
		const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
		
		timeData.value.push(timeStr);
		qpsHistoryData.INFO.push(qps.INFO);
		qpsHistoryData.WARN.push(qps.WARN);
		qpsHistoryData.ERROR.push(qps.ERROR);
		qpsHistoryData.DEBUG.push(qps.DEBUG);
		qpsHistoryData.TRACE.push(qps.TRACE);
		trafficHistoryData.value.push(traffic.value);

		// 保持数据点数量不超过MAX_DATA_POINTS
		if (timeData.value.length > MAX_DATA_POINTS) {
			timeData.value.shift();
			qpsHistoryData.INFO.shift();
			qpsHistoryData.WARN.shift();
			qpsHistoryData.ERROR.shift();
			qpsHistoryData.DEBUG.shift();
			qpsHistoryData.TRACE.shift();
			trafficHistoryData.value.shift();
		}

		// 更新所有图表
		updateCharts();
	} catch (e) {
		console.error('=== 读取文件失败 ===');
		console.error(e);
	}
}

function updateCharts() {
	// QPS折线图 - 时间序列图
	if (qpsChartInst) {
		qpsChartInst.setOption({
			xAxis: {
				data: timeData.value
			},
			series: [
				{
					name: 'INFO QPS',
					data: qpsHistoryData.INFO
				},
				{
					name: 'WARN QPS',
					data: qpsHistoryData.WARN
				},
				{
					name: 'ERROR QPS',
					data: qpsHistoryData.ERROR
				},
				{
					name: 'DEBUG QPS',
					data: qpsHistoryData.DEBUG
				},
				{
					name: 'TRACE QPS',
					data: qpsHistoryData.TRACE
				}
			]
		});
	}

	// 数据流量折线图 - 时间序列图
	if (trafficChartInst) {
		trafficChartInst.setOption({
			xAxis: {
				data: timeData.value
			},
			series: [
				{
					data: trafficHistoryData.value
				}
			]
		});
	}

	// 数据库连接池纵向柱状图
	if (dbChartInst) {
		dbChartInst.setOption({
			series: [
				{
					data: [metrics.db.create, metrics.db.close, metrics.db.poolStart, metrics.db.poolClose, metrics.db.error]
				}
			]
		});
	}

	// 日志等级统计图
	if (levelChartInst) {
		levelChartInst.setOption({
			xAxis: { data: Object.keys(logLevels) },
			series: [
				{
					data: Object.values(logLevels)
				}
			]
		});
	}

	// 包名日志数量排行
	if (pkgChartInst) {
		const pkgTop = pkgLogs.value.slice(0, 10).sort((a, b) => b.value - a.value);
		pkgChartInst.setOption({
			yAxis: {
				data: pkgTop.map(i => i.name)
			},
			series: [
				{
					data: pkgTop.map(i => i.value)
				}
			]
		});
	}

	// 异常类型统计
	if (exChartInst) {
		exChartInst.setOption({
			series: [
				{
					data: exceptions.value.filter(e => e.value > 0)
				}
			]
		});
	}
}

onMounted(async () => {
// 初始化QPS图表实例
	qpsChartInst = echarts.init(qpsChart.value);
	qpsChartInst.setOption({
		tooltip: { 
			trigger: 'axis',
			axisPointer: {
				type: 'cross',
				label: {
					backgroundColor: '#6a7985'
				}
			}
		},
		legend: { 
			data: ['INFO QPS', 'WARN QPS', 'ERROR QPS', 'DEBUG QPS', 'TRACE QPS'], 
			top: 0, 
			textStyle: { color: '#b0b8d0', fontSize: 12 }
		},
		grid: { left: 50, right: 50, top: 40, bottom: 30 },
		xAxis: {
			type: 'category',
			boundaryGap: false,
			data: timeData.value,
			axisLabel: { 
				color: '#b0b8d0', 
				fontSize: 11,
				rotate: 30
			},
			axisLine: { lineStyle: { color: '#3a4a6a' } }
		},
		yAxis: {
			type: 'value',
			name: 'QPS',
			axisLabel: { color: '#b0b8d0', fontSize: 12 },
			axisLine: { lineStyle: { color: '#3a4a6a' } },
			splitLine: { lineStyle: { color: '#2a3a5a', type: 'dashed' } }
		},
		series: [
			{
				name: 'INFO QPS',
				type: 'line',
				data: qpsHistoryData.INFO,
				smooth: true,
				symbol: 'circle',
				symbolSize: 6,
				lineStyle: { color: '#4fd7ff', width: 2 },
				itemStyle: { color: '#4fd7ff' },
				areaStyle: {
					color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
						{ offset: 0, color: 'rgba(79, 215, 255, 0.3)' },
						{ offset: 1, color: 'rgba(79, 215, 255, 0.05)' }
					])
				}
			},
			{
				name: 'WARN QPS',
				type: 'line',
				data: qpsHistoryData.WARN,
				smooth: true,
				symbol: 'circle',
				symbolSize: 6,
				lineStyle: { color: '#ffb74d', width: 2 },
				itemStyle: { color: '#ffb74d' }
			},
			{
				name: 'ERROR QPS',
				type: 'line',
				data: qpsHistoryData.ERROR,
				smooth: true,
				symbol: 'circle',
				symbolSize: 6,
				lineStyle: { color: '#ff4d4f', width: 2 },
				itemStyle: { color: '#ff4d4f' }
			},
			{
				name: 'DEBUG QPS',
				type: 'line',
				data: qpsHistoryData.DEBUG,
				smooth: true,
				symbol: 'circle',
				symbolSize: 6,
				lineStyle: { color: '#81c784', width: 2 },
				itemStyle: { color: '#81c784' }
			},
			{
				name: 'TRACE QPS',
				type: 'line',
				data: qpsHistoryData.TRACE,
				smooth: true,
				symbol: 'circle',
				symbolSize: 6,
				lineStyle: { color: '#9575cd', width: 2 },
				itemStyle: { color: '#9575cd' }
			}
		]
		});

	// 初始化数据流量图表实例
	trafficChartInst = echarts.init(trafficChart.value);
	trafficChartInst.setOption({
		tooltip: { 
			trigger: 'axis',
			axisPointer: {
				type: 'cross',
				label: {
					backgroundColor: '#6a7985'
				}
			}
		},
		legend: { 
			data: ['数据流量'], 
			top: 0, 
			textStyle: { color: '#b0b8d0', fontSize: 12 }
		},
		grid: { left: 50, right: 50, top: 40, bottom: 30 },
		xAxis: {
			type: 'category',
			boundaryGap: false,
			data: timeData.value,
			axisLabel: { 
				color: '#b0b8d0', 
				fontSize: 11,
				rotate: 30
			},
			axisLine: { lineStyle: { color: '#3a4a6a' } }
		},
		yAxis: {
			type: 'value',
			name: 'B/s',
			axisLabel: { color: '#b0b8d0', fontSize: 12 },
			axisLine: { lineStyle: { color: '#3a4a6a' } },
			splitLine: { lineStyle: { color: '#2a3a5a', type: 'dashed' } }
		},
		series: [
			{
				name: '数据流量',
				type: 'line',
				data: trafficHistoryData.value,
				smooth: true,
				symbol: 'diamond',
				symbolSize: 8,
				lineStyle: { color: '#ffd700', width: 3 },
				itemStyle: { color: '#ffd700' },
				areaStyle: {
					color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
						{ offset: 0, color: 'rgba(255, 215, 0, 0.3)' },
						{ offset: 1, color: 'rgba(255, 215, 0, 0.05)' }
					])
				}
			}
		]
	});

	// 数据库连接池纵向柱状图
	dbChartInst = echarts.init(dbChart.value);
	dbChartInst.setOption({
		tooltip: {
			trigger: 'axis',
			axisPointer: { type: 'shadow' },
			formatter: params => {
				return params.map(p => `${p.name}: <b>${p.value}</b>`).join('<br/>');
			}
		},
		grid: { left: 20, right: 20, top: 20, bottom: 30 },
		xAxis: {
			type: 'category',
			data: ['连接创建', '连接关闭', '池启动', '池关闭', '异常'],
			axisLabel: { color: '#b0b8d0', fontSize: 13 }
		},
		yAxis: {
			type: 'value',
			axisLabel: { color: '#b0b8d0', fontSize: 13 },
			splitLine: { show: false }
		},
		series: [
			{
				name: '次数',
				type: 'bar',
				data: [metrics.db.create, metrics.db.close, metrics.db.poolStart, metrics.db.poolClose, metrics.db.error],
				barWidth: 22,
				itemStyle: {
					color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
						{ offset: 0, color: '#4fd7ff' },
						{ offset: 1, color: '#1e88e5' }
					])
				},
				label: {
					show: true,
					position: 'top',
					color: '#fff',
					fontWeight: 'bold',
					fontSize: 14
				},
				emphasis: {
					itemStyle: { color: '#ffb74d' }
				}
			}
		]
	});

	// 日志等级统计图
	levelChartInst = echarts.init(levelChart.value);
	levelChartInst.setOption({
		tooltip: {},
		xAxis: { type: 'category', data: Object.keys(logLevels) },
		yAxis: { type: 'value' },
		series: [
			{
				name: '日志数量',
				type: 'bar',
				data: Object.values(logLevels),
				itemStyle: { color: '#4fd7ff' },
			},
		],
		grid: { top: 40, right: 20, left: 40, bottom: 40 },
	});

	// 包名日志数量排行（横向美观优化）
	pkgChartInst = echarts.init(pkgChart.value);
	const pkgTop = pkgLogs.value.slice(0, 10).sort((a, b) => b.value - a.value);
	pkgChartInst.setOption({
		tooltip: {
			trigger: 'axis',
			axisPointer: { type: 'shadow' },
			formatter: params => {
				const p = params[0];
				return `${p.name}<br/>日志数量: <b>${p.value}</b>`;
			}
		},
		grid: { left: 120, right: 30, top: 20, bottom: 20 },
		xAxis: {
			type: 'value',
			axisLine: { show: false },
			splitLine: { show: false },
			axisLabel: { color: '#b0b8d0', fontSize: 13 }
		},
		yAxis: {
			type: 'category',
			data: pkgTop.map(i => i.name),
			inverse: true,
			axisTick: { show: false },
			axisLine: { show: false },
			axisLabel: {
				color: '#b0b8d0',
				fontSize: 14,
				overflow: 'truncate',
				width: 100,
				formatter: value => value.length > 22 ? value.slice(0, 22) + '...' : value
			}
		},
		series: [
			{
				name: '日志数量',
				type: 'bar',
				data: pkgTop.map(i => i.value),
				barWidth: 18,
				itemStyle: {
					color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
						{ offset: 0, color: '#4fd7ff' },
						{ offset: 1, color: '#1e88e5' }
					])
				},
				label: {
					show: true,
					position: 'right',
					color: '#fff',
					fontWeight: 'bold',
					fontSize: 15
				},
				emphasis: {
					itemStyle: { color: '#ffb74d' }
				}
			}
		]
	});

	// 异常类型统计
	exChartInst = echarts.init(exChart.value);
	exChartInst.setOption({
		tooltip: {},
		series: [
			{
				name: '异常类型',
				type: 'pie',
				radius: '70%',
				data: exceptions.value.filter(e => e.value > 0),
				label: { show: true, formatter: '{b}: {c}' },
				color: ['#ff4d4f', '#ffb74d', '#4fd7ff', '#81c784', '#9575cd'],
			},
		],
	});

	// 首次加载数据
	await fetchDataAndUpdateCharts();

	// 设置定时器每秒刷新
	refreshTimer = setInterval(() => {
		fetchDataAndUpdateCharts();
	}, 1000);
});

onUnmounted(() => {
	// 清除定时器
	if (refreshTimer) {
		clearInterval(refreshTimer);
		refreshTimer = null;
	}

	// 销毁图表实例
	if (levelChartInst) levelChartInst.dispose();
	if (pkgChartInst) pkgChartInst.dispose();
	if (exChartInst) exChartInst.dispose();
	if (dbChartInst) dbChartInst.dispose();
	if (qpsChartInst) qpsChartInst.dispose();
	if (trafficChartInst) trafficChartInst.dispose();
});
</script>

<style scoped>
.log-dashboard {
	background: #181c2a;
	min-height: 100vh;
	color: #fff;
	font-family: 'Microsoft YaHei', Arial, sans-serif;
	padding: 24px;
}
.top-cards {
	display: flex;
	gap: 24px;
	margin-bottom: 24px;
}
.stat-card {
	flex: 1;
	background: #232949;
	border-radius: 12px;
	padding: 24px 0 16px 0;
	text-align: center;
	box-shadow: 0 2px 8px #0002;
}
.stat-card.info { border-top: 4px solid #4fd7ff; }
.stat-card.warn { border-top: 4px solid #ffb74d; }
.stat-card.error { border-top: 4px solid #ff4d4f; }
.stat-card.debug { border-top: 4px solid #81c784; }
.card-title {
	font-size: 18px;
	margin-bottom: 8px;
	letter-spacing: 2px;
}
.card-value {
	font-size: 32px;
	font-weight: bold;
}
.main-content {
	display: flex;
	gap: 24px;
}
.side-panel {
	width: 260px;
	background: #232949;
	border-radius: 12px;
	padding: 20px 16px;
	box-shadow: 0 2px 8px #0002;
	display: flex;
	flex-direction: column;
	gap: 16px;
}
.side-panel.right {
	width: 320px;
}
.panel-title {
	font-size: 16px;
	font-weight: bold;
	margin-bottom: 8px;
	color: #4fd7ff;
}
.panel-section {
	font-size: 14px;
	margin-bottom: 8px;
}
.highlight {
	color: #ffb74d;
	font-weight: bold;
}
.center-panel {
	flex: 1;
	background: #232949;
	border-radius: 12px;
	padding: 20px 24px 16px 24px;
	box-shadow: 0 2px 8px #0002;
	display: flex;
	flex-direction: column;
	align-items: stretch;
}
.chart-title {
	font-size: 18px;
	font-weight: bold;
	margin-bottom: 8px;
	color: #4fd7ff;
}
.chart-box {
	width: 100%;
	height: 280px;
	margin-bottom: 16px;
}
.chart-box.small {
	height: 180px;
}
.qps-panel {
	background: #181c2a;
	border-radius: 8px;
	padding: 12px 16px;
	font-size: 14px;
	color: #81c784;
	margin-top: 8px;
}
</style>
