// 简单的API模拟脚本，用于测试前端功能
(function() {
  // 模拟数据
  const mockData = {
    'traffic/overview': {
      stats: {
        roadCount: 156,
        avgCongestion: 1.8,
        congestionCount: 23,
        avgSpeed: 42
      },
      recentData: [
        {
          road_name: '建国路',
          description: '建国路西向东方向车流量较大，行驶缓慢',
          evaluation_status: 2,
          evaluation_status_desc: '缓行',
          request_time: new Date().toISOString()
        },
        {
          road_name: '长安街',
          description: '长安街东向西方向畅通',
          evaluation_status: 1,
          evaluation_status_desc: '畅通',
          request_time: new Date().toISOString()
        },
        {
          road_name: '三环路',
          description: '三环路北段拥堵严重，建议绕行',
          evaluation_status: 3,
          evaluation_status_desc: '拥堵',
          request_time: new Date().toISOString()
        }
      ]
    },
    'traffic/charts': {
      congestionDistribution: [
        { value: 45, name: '畅通' },
        { value: 32, name: '缓行' },
        { value: 18, name: '拥堵' },
        { value: 5, name: '严重拥堵' }
      ],
      trendLegend: ['畅通指数', '拥堵指数'],
      trendLabels: ['00:00', '06:00', '12:00', '18:00', '24:00'],
      trendSeries: [
        {
          name: '畅通指数',
          type: 'line',
          data: [1.8, 1.2, 2.1, 2.8, 2.0]
        },
        {
          name: '拥堵指数',
          type: 'line',
          data: [0.6, 0.4, 1.1, 1.9, 1.3]
        }
      ],
      speedLabels: ['建国路', '长安街', '三环路', '四环路', '五环路'],
      speedData: [38, 45, 25, 41, 52]
    },
    'traffic/realtime': [
      {
        road_name: '建国路',
        description: '车流量较大',
        evaluation_status: 2,
        evaluation_status_desc: '缓行',
        lng: 116.407428,
        lat: 39.90923,
        request_time: new Date().toISOString()
      },
      {
        road_name: '长安街',
        description: '道路畅通',
        evaluation_status: 1,
        evaluation_status_desc: '畅通',
        lng: 116.397428,
        lat: 39.90923,
        request_time: new Date().toISOString()
      }
    ]
  };

  // 拦截fetch请求
  const originalFetch = window.fetch;
  window.fetch = function(url, options) {
    console.log('Mock API called:', url);

    // 检查是否是我们的API请求
    if (url.includes('/api/')) {
      return new Promise((resolve) => {
        // 模拟网络延迟
        setTimeout(() => {
          const path = url.replace('/api/', '').replace(/\?.*$/, '');
          const data = mockData[path];

          if (data) {
            resolve({
              ok: true,
              status: 200,
              json: () => Promise.resolve(data)
            });
          } else {
            resolve({
              ok: false,
              status: 404,
              json: () => Promise.resolve({ error: 'API endpoint not found' })
            });
          }
        }, 500); // 模拟500ms延迟
      });
    }

    // 其他请求使用原始fetch
    return originalFetch.apply(this, arguments);
  };

  console.log('API Mock initialized');
})();