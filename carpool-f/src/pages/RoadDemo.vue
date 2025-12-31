<template>
  <div class="road-demo-page">
    <div class="demo-header">
      <h1>路况卡片演示</h1>
      <p>展示灵活的路况信息卡片布局</p>
    </div>

    <RoadCardGrid
      :roads="demoRoads"
      :show-stats="true"
      :load-more="true"
      :loading="loading"
      @view-details="handleViewDetails"
      @refresh="handleRefresh"
      @share="handleShare"
      @refresh-all="handleRefreshAll"
      @load-more="handleLoadMore"
    />
  </div>
</template>

<script>
import RoadCardGrid from '../components/RoadCardGrid.vue'

export default {
  name: 'RoadDemo',
  components: {
    RoadCardGrid
  },
  data() {
    return {
      loading: false,
      demoRoads: [
        {
          road_name: '京藏高速',
          city: '北京',
          evaluation_status: 4,
          request_time: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
          congestion_distance: 8.5,
          speed: 15,
          description: '严重拥堵，建议绕行其他道路'
        },
        {
          road_name: '上海延安高架',
          city: '上海',
          evaluation_status: 3,
          request_time: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
          congestion_distance: 4.2,
          speed: 25,
          description: '高峰期车流量大，注意保持车距'
        },
        {
          road_name: '广州天河路',
          city: '广州',
          evaluation_status: 2,
          request_time: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
          congestion_distance: 2.1,
          speed: 35,
          description: '正常拥堵，预计15分钟后缓解'
        },
        {
          road_name: '深圳深南大道',
          city: '深圳',
          evaluation_status: 1,
          request_time: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
          congestion_distance: 1.5,
          speed: 45,
          description: '车流量正常，行驶顺畅'
        },
        {
          road_name: '杭州西湖大道',
          city: '杭州',
          evaluation_status: 0,
          request_time: new Date(Date.now() - 1 * 60 * 1000).toISOString(),
          congestion_distance: 0,
          speed: 60,
          description: '道路畅通，可正常通行'
        },
        {
          road_name: '成都天府大道',
          city: '成都',
          evaluation_status: 2,
          request_time: new Date(Date.now() - 8 * 60 * 1000).toISOString(),
          congestion_distance: 3.0,
          speed: 30,
          description: '轻微拥堵，注意慢行'
        },
        {
          road_name: '南京长江路',
          city: '南京',
          evaluation_status: 1,
          request_time: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
          congestion_distance: 0.8,
          speed: 50,
          description: '交通状况良好'
        },
        {
          road_name: '武汉光谷大道',
          city: '武汉',
          evaluation_status: 3,
          request_time: new Date(Date.now() - 7 * 60 * 1000).toISOString(),
          congestion_distance: 5.5,
          speed: 20,
          description: '施工路段，请减速慢行'
        }
      ]
    }
  },
  methods: {
    handleViewDetails(roadData) {
      console.log('查看详情:', roadData)
      this.$message?.info(`查看 ${roadData.road_name} 详情`)
    },

    handleRefresh(roadData) {
      console.log('刷新数据:', roadData)
      this.$message?.success(`${roadData.road_name} 数据已刷新`)
    },

    handleShare(roadData) {
      console.log('分享路况:', roadData)
      this.$message?.success(`${roadData.road_name} 路况已分享`)
    },

    handleRefreshAll() {
      console.log('刷新全部数据')
      this.loading = true

      // 模拟加载过程
      setTimeout(() => {
        this.loading = false
        this.$message?.success('所有路况数据已刷新')

        // 更新时间戳模拟数据刷新
        this.demoRoads = this.demoRoads.map(road => ({
          ...road,
          request_time: new Date().toISOString()
        }))
      }, 2000)
    },

    handleLoadMore() {
      console.log('加载更多数据')
      this.loading = true

      // 模拟加载更多数据
      setTimeout(() => {
        this.loading = false
        const additionalRoads = [
          {
            road_name: '西安钟楼商圈',
            city: '西安',
            evaluation_status: 1,
            request_time: new Date().toISOString(),
            congestion_distance: 1.0,
            speed: 48,
            description: '商业区交通良好'
          },
          {
            road_name: '重庆解放碑商圈',
            city: '重庆',
            evaluation_status: 2,
            request_time: new Date().toISOString(),
            congestion_distance: 2.8,
            speed: 32,
            description: '山城地形，请注意行车安全'
          }
        ]
        this.demoRoads = [...this.demoRoads, ...additionalRoads]
        this.$message?.success('已加载更多路况数据')
      }, 1500)
    }
  }
}
</script>

<style scoped>
.road-demo-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 80px 0 40px;
}

.demo-header {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

.demo-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.demo-header p {
  font-size: 1.2rem;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .road-demo-page {
    padding: 60px 0 30px;
  }

  .demo-header {
    margin-bottom: 30px;
  }

  .demo-header h1 {
    font-size: 2rem;
  }

  .demo-header p {
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .road-demo-page {
    padding: 50px 0 20px;
  }

  .demo-header h1 {
    font-size: 1.8rem;
  }

  .demo-header p {
    font-size: 1rem;
  }
}
</style>