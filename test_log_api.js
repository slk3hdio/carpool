// 测试后端 /log 相关接口
// 运行方式: node test_log_api.js

const BASE_URL = 'http://localhost:8080/api';

// 测试 /log/level-count 接口
async function testLogLevelCount() {
    console.log('\n========== 测试 GET /log/level-count ==========');
    
    try {
        const response = await fetch(`${BASE_URL}/log/level-count`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        console.log('状态码:', response.status);
        console.log('状态文本:', response.statusText);
        
        const data = await response.json();
        console.log('响应数据:', JSON.stringify(data, null, 2));
        
        if (response.ok) {
            console.log('✅ 测试通过');
            if (data.content) {
                console.log('\n日志统计内容:');
                console.log(data.content);
            }
        } else {
            console.log('❌ 测试失败');
            if (data.message) {
                console.log('错误信息:', data.message);
            }
        }
        
        return response.ok;
    } catch (error) {
        console.log('❌ 请求失败:', error.message);
        return false;
    }
}

// 测试所有接口
async function runAllTests() {
    console.log('开始测试后端 /log 接口...');
    console.log('后端地址:', BASE_URL);
    console.log('请确保后端服务已启动在端口 8080');
    
    const results = [];
    
    // 测试 log-level-count 接口
    results.push({
        name: 'GET /log/level-count',
        passed: await testLogLevelCount()
    });
    
    // 显示测试摘要
    console.log('\n========== 测试摘要 ==========');
    let passedCount = 0;
    results.forEach(result => {
        const status = result.passed ? '✅ 通过' : '❌ 失败';
        console.log(`${status} - ${result.name}`);
        if (result.passed) passedCount++;
    });
    
    console.log(`\n总计: ${passedCount}/${results.length} 个测试通过`);
}

// 运行测试
runAllTests().catch(error => {
    console.error('测试执行出错:', error);
    process.exit(1);
});
