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
					<div class="chart-title" style="margin-bottom:0;">QPS & 数据流量</div>
					<div ref="qpsChart" class="chart-box small"></div>
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
import { reactive, ref, onMounted } from 'vue';
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

const levelChart = ref(null);
const pkgChart = ref(null);
const exChart = ref(null);
const dbChart = ref(null);
const qpsChart = ref(null);

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

onMounted(async () => {
	try {
		const get_url = 'http://localhost:8080/api/log/level-count';
		const res = await axios.get(get_url);
	  // 直接读取本地文件
    // const res = await fetch('../../../carpool-b/log_level_count.txt');
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
  } catch (e) {
    console.error('=== 读取文件失败 ===');
	console.error(e);
  }

	// QPS与数据流量折线图
			const qpsChartInst = echarts.init(qpsChart.value);
			qpsChartInst.setOption({
				tooltip: { trigger: 'axis' },
				legend: { data: ['QPS', '数据流量(B/s)'], top: 0, textStyle: { color: '#b0b8d0' } },
				grid: { left: 40, right: 20, top: 30, bottom: 30 },
				xAxis: {
					type: 'category',
					data: ['INFO', 'WARN', 'ERROR', 'DEBUG', 'TRACE'],
					axisLabel: { color: '#b0b8d0', fontSize: 13 }
				},
				yAxis: [
					{
						type: 'value',
						name: 'QPS',
						min: 0,
						axisLabel: { color: '#b0b8d0', fontSize: 13 },
						splitLine: { show: false }
					},
					{
						type: 'value',
						name: '数据流量(B/s)',
						min: 0,
						axisLabel: { color: '#b0b8d0', fontSize: 13 },
						splitLine: { show: false }
					}
				],
				series: [
					{
						name: 'QPS',
						type: 'line',
						data: [qps.INFO, qps.WARN, qps.ERROR, qps.DEBUG, qps.TRACE],
						yAxisIndex: 0,
						smooth: true,
						symbol: 'circle',
						symbolSize: 10,
						lineStyle: { color: '#4fd7ff', width: 3 },
						itemStyle: { color: '#4fd7ff' },
						label: { show: true, color: '#fff', fontWeight: 'bold' }
					},
					{
						name: '数据流量(B/s)',
						type: 'line',
						data: [traffic.value, 0, 0, 0, 0],
						yAxisIndex: 1,
						smooth: true,
						symbol: 'rect',
						symbolSize: 10,
						lineStyle: { color: '#ffb74d', width: 3, type: 'dashed' },
						itemStyle: { color: '#ffb74d' },
						label: { show: true, color: '#fff', fontWeight: 'bold' }
					}
				]
			});
		// 数据库连接池纵向柱状图
		const dbChartInst = echarts.init(dbChart.value);
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
	const levelChartInst = echarts.init(levelChart.value);
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
			{
				name: '日志数量趋势',
				type: 'line',
				data: Object.values(logLevels),
				smooth: true,
				lineStyle: { color: '#ffb74d' },
			},
		],
		grid: { top: 40, right: 20, left: 40, bottom: 40 },
	});

	// 包名日志数量排行（横向美观优化）
	const pkgChartInst = echarts.init(pkgChart.value);
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
	const exChartInst = echarts.init(exChart.value);
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
