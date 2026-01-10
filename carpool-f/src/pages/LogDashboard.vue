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
				
				<!-- 异常告警区域 -->
				<div v-if="condition > 0" class="exception-alert">
					<div class="alert-header">
						<span class="alert-icon">⚠️</span>
						<span class="alert-title">异常告警</span>
					</div>
					<div class="alert-body">
						<div v-for="(ex, index) in exceptionData" :key="index" class="exception-item">
							<div class="exception-header" @click="toggleException(index)">
								<span class="exception-type" :class="getExceptionSeverity(ex.type)">{{ ex.type }}</span>
								<span v-if="ex.count !== undefined" class="exception-count">{{ ex.count }}</span>
								<span class="toggle-icon">{{ expandedExceptions[index] ? '▼' : '▶' }}</span>
							</div>
							<div v-if="expandedExceptions[index]" class="exception-stack">
								<pre>{{ ex.stack }}</pre>
							</div>
						</div>
					</div>
				</div>
				
				<!-- AI智能分析对话框 -->
				<div class="ai-analysis-panel">
					<div class="ai-panel-header">
						<div class="ai-icon-pro"></div>
						<span class="ai-title">AI智能分析</span>
					</div>
					<div class="ai-chat-container" ref="chatContainer">
						<div v-for="(msg, index) in chatMessages" :key="index" 
							 :class="['chat-message', msg.role]"
							 :style="{ animationDelay: `${index * 0.1}s` }">
							<div class="message-avatar" v-if="msg.role === 'ai'">
								<div class="avatar-ai-icon"></div>
							</div>
							<div class="message-bubble">
								<div class="message-content" v-html="msg.content"></div>
								<div class="message-time">{{ msg.time }}</div>
							</div>
							<div class="message-avatar" v-if="msg.role === 'user'">
								<span class="avatar-icon">👤</span>
							</div>
						</div>
					</div>
					<div class="ai-action-bar" v-if="showAnalyzeButton">
						<button class="analyze-btn" @click="sendAlertAnalysis" :disabled="isAnalyzing">
							<span v-if="!isAnalyzing">告警分析</span>
							<span v-else>分析中...</span>
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';

// 硬编码的测试结果文件内容
const testResult1 = `--- 异常堆栈摘要（每种类型首例） ---
NullPointerException:
java.lang.NullPointerException: Something is null
	at com.example.TestApp.main(TestApp.java:10)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)

IllegalArgumentException:
java.lang.IllegalArgumentException: Bad argument
	at com.example.TestApp.doSomething(TestApp.java:20)
	at com.example.TestApp.main(TestApp.java:11)

RuntimeException:
java.lang.RuntimeException: Top level error
	at com.example.TestApp.run(TestApp.java:30)
	at com.example.TestApp.main(TestApp.java:12)`;

const testResult2 = `--- 异常类型统计 ---
IOException: 1
SQLException: 2
IndexOutOfBoundsException: 1
NullPointerException: 0
IllegalArgumentException: 0
RuntimeException: 0
StackOverflowError: 0
OutOfMemoryError: 0

--- 异常堆栈摘要（每种类型首例） ---
IOException:
java.io.IOException: Disk not found
	at com.example.TestApp.readFile(TestApp.java:50)
	at com.example.TestApp.main(TestApp.java:15)

SQLException:
java.sql.SQLException: DB error
	at com.example.TestApp.queryDb(TestApp.java:60)
	at com.example.TestApp.main(TestApp.java:16)

IndexOutOfBoundsException:
java.lang.IndexOutOfBoundsException: Index 5 out of bounds
	at com.example.TestApp.getList(TestApp.java:70)
	at com.example.TestApp.main(TestApp.java:17)`;

const testResult3 = `--- 异常类型统计 ---
ArithmeticException: 1
StackOverflowError: 1
OutOfMemoryError: 1
NullPointerException: 0
IllegalArgumentException: 0
IOException: 0
SQLException: 0
RuntimeException: 0

--- 异常堆栈摘要（每种类型首例） ---
ArithmeticException:
java.lang.ArithmeticException: / by zero
	at com.example.TestApp.divide(TestApp.java:80)
	at com.example.TestApp.main(TestApp.java:18)

StackOverflowError:
java.lang.StackOverflowError
	at com.example.TestApp.recursive(TestApp.java:90)
	at com.example.TestApp.recursive(TestApp.java:90)
	at com.example.TestApp.recursive(TestApp.java:90)

OutOfMemoryError:
java.lang.OutOfMemoryError: Java heap space
	at com.example.TestApp.allocate(TestApp.java:100)
	at com.example.TestApp.main(TestApp.java:19)`;

// 告警条件：0=正常，1=testResult1告警，2=testResult2告警，3=testResult3告警
const condition = ref(3); // 可以修改这个值来测试不同的告警

const logLevels = reactive({ INFO: 0, WARN: 0, ERROR: 0, DEBUG: 0, TRACE: 0 });
const metrics = reactive({
	startupTime: '',
	rootInitTime: '',
	db: { create: 0, close: 0, poolStart: 0, poolClose: 0, error: 0 },
});

const qps = reactive({ INFO: 0, WARN: 0, ERROR: 0, DEBUG: 0, TRACE: 0 });
const traffic = ref(0);
const pkgLogs = ref([]);

// 异常数据
const exceptionData = ref([]);
const expandedExceptions = ref({});

// AI对话相关
const chatMessages = ref([]);
const showAnalyzeButton = ref(true);
const isAnalyzing = ref(false);
const chatContainer = ref(null);

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
const dbChart = ref(null);
const qpsChart = ref(null);
const trafficChart = ref(null);

let levelChartInst = null;
let pkgChartInst = null;
let dbChartInst = null;
let qpsChartInst = null;
let trafficChartInst = null;
let refreshTimer = null;

// 解析异常数据
function parseExceptionData(testResultContent) {
	const exceptions = [];
	
	// 提取异常类型统计
	const statsMatch = testResultContent.match(/--- 异常类型统计 ---\s*\n([\s\S]*?)(?=\n---|$)/);
	const statsMap = {};
	if (statsMatch) {
		const lines = statsMatch[1].trim().split('\n');
		lines.forEach(line => {
			const match = line.match(/^([^:]+):\s*(\d+)/);
			if (match) {
				statsMap[match[1]] = parseInt(match[2]);
			}
		});
	}
	
	// 提取异常堆栈
	const stackMatch = testResultContent.match(/--- 异常堆栈摘要（每种类型首例） ---\s*\n([\s\S]+)$/);
	if (stackMatch) {
		const stackText = stackMatch[1];
		// 按异常类型分割
		const exceptionBlocks = stackText.split(/\n(?=[A-Z]\w+:)/);
		
		exceptionBlocks.forEach(block => {
			const lines = block.trim().split('\n');
			if (lines.length > 0) {
				const typeMatch = lines[0].match(/^([^:]+):/);
				if (typeMatch) {
					const type = typeMatch[1];
					const stack = lines.slice(1).join('\n');
					const count = statsMap[type];
					
					exceptions.push({
						type: type,
						count: count,
						stack: stack
					});
				}
			}
		});
	}
	
	return exceptions;
}

// 切换异常详情展开/折叠
function toggleException(index) {
	expandedExceptions.value[index] = !expandedExceptions.value[index];
}

// 根据异常类型获取严重程度样式
function getExceptionSeverity(type) {
	const criticalErrors = ['OutOfMemoryError', 'StackOverflowError', 'NullPointerException'];
	const highErrors = ['SQLException', 'IOException', 'RuntimeException'];
	
	if (criticalErrors.includes(type)) return 'severity-critical';
	if (highErrors.includes(type)) return 'severity-high';
	return 'severity-medium';
}

// AI对话功能
function getCurrentTime() {
	const now = new Date();
	return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
}

function addMessage(role, content) {
	chatMessages.value.push({
		role: role, // 'ai' or 'user'
		content: content,
		time: getCurrentTime()
	});
	
	// 滚动到底部
	setTimeout(() => {
		if (chatContainer.value) {
			chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
		}
	}, 100);
}

// 初始化AI对话
function initAIChat() {
	chatMessages.value = [];
	addMessage('ai', '您好，我是日志智能分析助手LogAnalyzer v2.0。<br/>已就绪，可以开始异常告警分析。');
	showAnalyzeButton.value = true;
}

// 发送告警分析请求
function sendAlertAnalysis() {
	if (isAnalyzing.value) return;
	
	isAnalyzing.value = true;
	showAnalyzeButton.value = false;
	
	// 用户发送告警信息
	let alertMsg = '';
	if (condition.value === 1) {
		alertMsg = '检测到异常告警：<br/>• NullPointerException (1次)<br/>• IllegalArgumentException (1次)<br/>• RuntimeException (1次)';
	} else if (condition.value === 2) {
		alertMsg = '检测到异常告警：<br/>• IOException (1次)<br/>• SQLException (2次)<br/>• IndexOutOfBoundsException (1次)';
	} else if (condition.value === 3) {
		alertMsg = '检测到异常告警：<br/>• ArithmeticException (1次)<br/>• StackOverflowError (1次)<br/>• OutOfMemoryError (1次)';
	}
	
	addMessage('user', alertMsg);
	
	// AI生成分析报告（延迟模拟思考时间）
	setTimeout(() => {
		const report = generateAIReport(condition.value);
		addMessage('ai', report);
		isAnalyzing.value = false;
	}, 1500);
}

// 生成AI分析报告
function generateAIReport(conditionValue) {
	if (conditionValue === 1) {
		return `<div class="report-content">
<div class="report-title">【异常分析报告 #${Date.now().toString().slice(-6)}】</div>
<div class="report-section">
<strong>一、异常概况</strong><br/>
时间段：${new Date().toLocaleString()}<br/>
异常总数：3起<br/>
涉及类型：NullPointerException、IllegalArgumentException、RuntimeException
</div>
<div class="report-section">
<strong>二、根因分析</strong><br/>
1. NullPointerException发生在TestApp.java:10，主方法调用时对象未初始化。<br/>
2. IllegalArgumentException源于参数校验失败，doSomething方法传入非法参数。<br/>
3. RuntimeException为顶层错误捕获，run方法内部逻辑异常。
</div>
<div class="report-section">
<strong>三、处理建议</strong><br/>
• 在对象使用前增加null检查或使用Optional模式<br/>
• 完善参数校验逻辑，提前拦截非法输入<br/>
• 细化异常处理，避免笼统抛出RuntimeException<br/>
• 建议增加单元测试覆盖边界情况
</div>
<div class="report-footer">风险等级：<span class="risk-medium">中等</span> | 建议优先级：P2</div>
</div>`;
	} else if (conditionValue === 2) {
		return `<div class="report-content">
<div class="report-title">【异常分析报告 #${Date.now().toString().slice(-6)}】</div>
<div class="report-section">
<strong>一、异常概况</strong><br/>
时间段：${new Date().toLocaleString()}<br/>
异常总数：4起<br/>
涉及类型：IOException、SQLException（2次）、IndexOutOfBoundsException
</div>
<div class="report-section">
<strong>二、根因分析</strong><br/>
1. IOException异常位于readFile方法，磁盘读取失败，可能是文件不存在或权限不足。<br/>
2. SQLException出现2次，queryDb方法执行失败，初步判断为数据库连接池配置问题或SQL语法错误。<br/>
3. IndexOutOfBoundsException发生在getList方法，数组越界访问，索引为5超出实际长度。
</div>
<div class="report-section">
<strong>三、处理建议</strong><br/>
• 检查文件路径配置和磁盘空间，确保文件访问权限正常<br/>
• 排查数据库连接池状态（当前创建${metrics.db.create}次，关闭${metrics.db.close}次），必要时调整连接参数<br/>
• 审查SQL语句正确性，开启慢查询日志<br/>
• 数组访问前验证索引范围，使用集合的安全访问方法
</div>
<div class="report-footer">风险等级：<span class="risk-high">较高</span> | 建议优先级：P1</div>
</div>`;
	} else if (conditionValue === 3) {
		return `<div class="report-content">
<div class="report-title">【异常分析报告 #${Date.now().toString().slice(-6)}】</div>
<div class="report-section">
<strong>一、异常概况</strong><br/>
时间段：${new Date().toLocaleString()}<br/>
异常总数：3起<br/>
涉及类型：ArithmeticException、StackOverflowError、OutOfMemoryError<br/>
<span style="color:#ff4d4f;">⚠️ 警告：检测到严重系统级错误</span>
</div>
<div class="report-section">
<strong>二、根因分析</strong><br/>
1. ArithmeticException由除零操作引起，divide方法中分母为0。<br/>
2. StackOverflowError表明存在无限递归调用，recursive方法未设置正确的终止条件。<br/>
3. OutOfMemoryError显示Java堆内存溢出，allocate方法中可能存在内存泄漏或一次性分配过大对象。
</div>
<div class="report-section">
<strong>三、处理建议</strong><br/>
<span style="color:#ff4d4f;">【紧急】</span>建议立即采取以下措施：<br/>
• 除法运算前必须校验分母非零<br/>
• 检查recursive方法递归逻辑，添加深度限制或改用迭代实现<br/>
• 使用jmap、jvisualvm等工具排查内存泄漏点<br/>
• 调整JVM堆内存参数（-Xms -Xmx），当前可能配置过小<br/>
• 优化大对象分配策略，考虑分批处理或使用对象池
</div>
<div class="report-footer">风险等级：<span class="risk-critical">严重</span> | 建议优先级：P0（立即处理）</div>
</div>`;
	}
	return '分析完成。';
}

function parseLogData(data) {
	// 清空之前的数据
	pkgLogs.value = [];
  
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
	// 已移除异常类型统计相关处理
}

async function fetchDataAndUpdateCharts() {
	try {
		const get_url = 'http://47.100.65.234/api/log/level-count';
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
		
		// 根据 condition 更新异常数据
		if (condition.value === 1) {
			exceptionData.value = parseExceptionData(testResult1);
		} else if (condition.value === 2) {
			exceptionData.value = parseExceptionData(testResult2);
		} else if (condition.value === 3) {
			exceptionData.value = parseExceptionData(testResult3);
		} else {
			exceptionData.value = [];
		}
		
		// 初始化展开状态（默认全部折叠）
		expandedExceptions.value = {};
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

	// 异常类型统计相关已移除
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

	// 异常类型统计相关已移除

	// 首次加载数据
	await fetchDataAndUpdateCharts();

	// 初始化AI对话
	initAIChat();

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
	// exChartInst 已移除
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

/* 异常告警样式 */
.exception-alert {
	margin-top: 20px;
	background: linear-gradient(135deg, #2d1b1b 0%, #1a1a2e 100%);
	border-radius: 12px;
	border: 2px solid #ff4d4f;
	overflow: hidden;
	box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
	animation: pulse-border 2s infinite;
}

@keyframes pulse-border {
	0%, 100% { border-color: #ff4d4f; box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3); }
	50% { border-color: #ff7875; box-shadow: 0 4px 20px rgba(255, 77, 79, 0.6); }
}

.alert-header {
	background: linear-gradient(90deg, #ff4d4f 0%, #ff7875 100%);
	padding: 12px 16px;
	display: flex;
	align-items: center;
	gap: 8px;
}

.alert-icon {
	font-size: 20px;
	animation: shake 0.5s infinite;
}

@keyframes shake {
	0%, 100% { transform: translateX(0); }
	25% { transform: translateX(-3px); }
	75% { transform: translateX(3px); }
}

.alert-title {
	font-size: 16px;
	font-weight: bold;
	color: #fff;
	letter-spacing: 1px;
}

.alert-body {
	padding: 12px;
	max-height: 400px;
	overflow-y: auto;
}

.alert-body::-webkit-scrollbar {
	width: 6px;
}

.alert-body::-webkit-scrollbar-track {
	background: #1a1a2e;
}

.alert-body::-webkit-scrollbar-thumb {
	background: #ff4d4f;
	border-radius: 3px;
}

.exception-item {
	margin-bottom: 12px;
	background: #232949;
	border-radius: 8px;
	overflow: hidden;
	transition: all 0.3s ease;
}

.exception-item:hover {
	box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
	transform: translateY(-2px);
}

.exception-header {
	padding: 10px 12px;
	display: flex;
	align-items: center;
	gap: 8px;
	cursor: pointer;
	user-select: none;
	transition: background 0.2s;
}

.exception-header:hover {
	background: #2d3856;
}

.exception-type {
	flex: 1;
	font-weight: bold;
	font-size: 13px;
	padding: 4px 8px;
	border-radius: 4px;
}

.severity-critical {
	background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
	color: #fff;
	box-shadow: 0 2px 4px rgba(255, 77, 79, 0.5);
}

.severity-high {
	background: linear-gradient(135deg, #ffb74d 0%, #ff9800 100%);
	color: #fff;
	box-shadow: 0 2px 4px rgba(255, 183, 77, 0.5);
}

.severity-medium {
	background: linear-gradient(135deg, #ffd666 0%, #faad14 100%);
	color: #333;
	box-shadow: 0 2px 4px rgba(255, 214, 102, 0.5);
}

.exception-count {
	background: #ff4d4f;
	color: #fff;
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: bold;
	min-width: 24px;
	text-align: center;
}

.toggle-icon {
	color: #b0b8d0;
	font-size: 10px;
	transition: transform 0.2s;
}

.exception-stack {
	padding: 12px;
	background: #1a1f30;
	border-top: 1px solid #3a4a6a;
	animation: slideDown 0.3s ease;
}

@keyframes slideDown {
	from {
		opacity: 0;
		max-height: 0;
	}
	to {
		opacity: 1;
		max-height: 500px;
	}
}

.exception-stack pre {
	margin: 0;
	font-family: 'Consolas', 'Monaco', monospace;
	font-size: 11px;
	line-height: 1.6;
	color: #e06c75;
	white-space: pre-wrap;
	word-break: break-all;
}

/* AI智能分析面板样式 */
.ai-analysis-panel {
	margin-top: 20px;
	background: #1a1a1a;
	border-radius: 12px;
	overflow: hidden;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6);
	border: 1px solid #333;
}

.ai-panel-header {
	background: linear-gradient(90deg, #2a2a2a 0%, #1a1a1a 100%);
	padding: 10px 16px;
	display: flex;
	align-items: center;
	gap: 8px;
	border-bottom: 1px solid #444;
}

.ai-icon-pro {
	width: 24px;
	height: 24px;
	background: #fff;
	border-radius: 6px;
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 2px 6px rgba(255, 255, 255, 0.3);
}

.ai-icon-pro::before {
	content: '';
	width: 16px;
	height: 16px;
	background: linear-gradient(135deg, #333 0%, #000 100%);
	border-radius: 3px;
	position: absolute;
	animation: pulse 2s ease-in-out infinite;
}

.ai-icon-pro::after {
	content: 'AI';
	position: absolute;
	font-size: 10px;
	font-weight: bold;
	color: #fff;
	z-index: 1;
	letter-spacing: 0.5px;
}

@keyframes pulse {
	0%, 100% {
		transform: scale(1);
		opacity: 1;
	}
	50% {
		transform: scale(1.1);
		opacity: 0.8;
	}
}

.ai-title {
	font-size: 14px;
	font-weight: bold;
	color: #fff;
}

.ai-chat-container {
	padding: 16px;
	max-height: 500px;
	min-height: 250px;
	overflow-y: auto;
	display: flex;
	flex-direction: column;
	gap: 16px;
	background: #0a0a0a;
}

.ai-chat-container::-webkit-scrollbar {
	width: 6px;
}

.ai-chat-container::-webkit-scrollbar-track {
	background: #1a1a1a;
}

.ai-chat-container::-webkit-scrollbar-thumb {
	background: #444;
	border-radius: 3px;
}

.ai-chat-container::-webkit-scrollbar-thumb:hover {
	background: #666;
}

.chat-message {
	display: flex;
	align-items: flex-start;
	gap: 10px;
	animation: messageSlideIn 0.4s ease-out;
}

@keyframes messageSlideIn {
	from {
		opacity: 0;
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.chat-message.ai {
	justify-content: flex-start;
}

.chat-message.user {
	justify-content: flex-end;
	flex-direction: row-reverse;
}

.message-avatar {
	width: 36px;
	height: 36px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	font-size: 20px;
}

.chat-message.ai .message-avatar {
	background: linear-gradient(135deg, #333 0%, #000 100%);
	box-shadow: 0 2px 6px rgba(255, 255, 255, 0.2);
	border: 1px solid #444;
}

.chat-message.user .message-avatar {
	background: linear-gradient(135deg, #fff 0%, #e0e0e0 100%);
	border: 1px solid #ccc;
}

.avatar-ai-icon {
	width: 20px;
	height: 20px;
	background: #fff;
	border-radius: 4px;
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

.avatar-ai-icon::before {
	content: 'AI';
	font-size: 9px;
	font-weight: bold;
	color: #000;
	letter-spacing: 0.5px;
}

.avatar-icon {
	filter: brightness(1.2);
}

.message-bubble {
	max-width: 75%;
	padding: 10px 14px;
	border-radius: 12px;
	position: relative;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.5);
}

.chat-message.ai .message-bubble {
	background: #1a1a1a;
	border: 1px solid #333;
	color: #e0e0e0;
	border-bottom-left-radius: 4px;
}

.chat-message.user .message-bubble {
	background: #fff;
	color: #000;
	border-bottom-right-radius: 4px;
	border: 1px solid #ddd;
}

.message-content {
	font-size: 13px;
	line-height: 1.6;
	word-wrap: break-word;
}

.message-time {
	font-size: 11px;
	opacity: 0.6;
	margin-top: 6px;
	text-align: right;
}

.ai-action-bar {
	padding: 12px 16px;
	background: #1a1a1a;
	border-top: 1px solid #333;
	display: flex;
	justify-content: center;
}

.analyze-btn {
	background: linear-gradient(135deg, #fff 0%, #e0e0e0 100%);
	color: #000;
	border: 1px solid #ccc;
	padding: 10px 32px;
	border-radius: 20px;
	font-size: 14px;
	font-weight: 600;
	cursor: pointer;
	transition: all 0.3s;
	box-shadow: 0 2px 8px rgba(255, 255, 255, 0.2);
}

.analyze-btn:hover:not(:disabled) {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(255, 255, 255, 0.4);
}

.analyze-btn:active:not(:disabled) {
	transform: translateY(0);
}

.analyze-btn:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

/* AI报告内容样式 */
.report-content {
	font-size: 13px;
	line-height: 1.8;
	color: #e0e0e0;
}

.report-title {
	font-size: 15px;
	font-weight: bold;
	color: #fff;
	margin-bottom: 10px;
	padding-bottom: 6px;
	border-bottom: 2px solid #666;
}

.report-section {
	margin-bottom: 12px;
	padding: 10px;
	background: #0a0a0a;
	border-radius: 6px;
	border-left: 3px solid #888;
}

.report-section strong {
	color: #fff;
	display: block;
	margin-bottom: 6px;
	font-size: 14px;
}

.report-footer {
	margin-top: 12px;
	padding-top: 8px;
	border-top: 1px dashed #444;
	font-size: 12px;
	text-align: right;
	color: #999;
}

.risk-medium {
	color: #ff9800;
	font-weight: bold;
	padding: 2px 8px;
	background: rgba(255, 152, 0, 0.15);
	border-radius: 4px;
}

.risk-high {
	color: #ff6f00;
	font-weight: bold;
	padding: 2px 8px;
	background: rgba(255, 111, 0, 0.15);
	border-radius: 4px;
}

.risk-critical {
	color: #d32f2f;
	font-weight: bold;
	padding: 2px 8px;
	background: rgba(211, 47, 47, 0.15);
	border-radius: 4px;
}
</style>
