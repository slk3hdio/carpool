<template>
  <div class="trip-example-page">
    <div class="page-header">
      <h1>行程管理</h1>
      <p>查看和管理您的拼车行程</p>
    </div>

    <div class="page-content">
      <!-- 使用 TripCardGrid 显示行程列表 -->
      <TripCardGrid
        :trips="trips"
        :loading="loading"
        :show-actions="true"
        @view="handleViewTrip"
        @cancel="handleCancelTrip"
      />
    </div>

    <!-- 单独使用 TripCard 的示例 -->
    <div class="single-card-example">
      <h2>单个行程卡片示例</h2>
      <TripCard
        :trip="sampleTrip"
        :show-actions="true"
        @view="handleViewTrip"
        @cancel="handleCancelTrip"
      />
    </div>
  </div>
</template>

<script>
import TripCardGrid from '@/components/TripCardGrid.vue'
import TripCard from '@/components/TripCard.vue'
import axios from 'axios'

export default {
  name: 'TripCardExample',
  components: {
    TripCardGrid,
    TripCard
  },
  data() {
    return {
      trips: [],
      loading: false,
      sampleTrip: {
        id: 1,
        startLocation: '上海市浦东国际机场',
        startLatitude: 31.1443,
        startLongitude: 121.8083,
        endLocation: '上海市虹桥火车站',
        endLatitude: 31.1979,
        endLongitude: 121.3206,
        departureAt: '2025-12-28 14:30:00',
        statusDesc: '进行中',
        passengerCount: 3,
        matchAt: '2025-12-27 10:15:00',
        createdAt: '2025-12-27 09:30:00'
      }
    }
  },
  mounted() {
    this.fetchTrips()
  },
  methods: {
    async fetchTrips() {
      this.loading = true
      try {
        // 获取当前用户的所有行程
        const response = await axios.get('http://47.100.65.234/api/trip/user', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.trips = response.data || []
      } catch (error) {
        console.error('获取行程列表失败:', error)
        // 使用示例数据用于演示
        this.trips = [
          {
            id: 1,
            startLocation: '上海市浦东国际机场T2航站楼',
            startLatitude: 31.1443,
            startLongitude: 121.8083,
            endLocation: '上海市虹桥火车站',
            endLatitude: 31.1979,
            endLongitude: 121.3206,
            departureAt: '2025-12-28 14:30:00',
            statusDesc: '已创建',
            passengerCount: 3,
            matchAt: '2025-12-27 10:15:00',
            createdAt: '2025-12-27 09:30:00'
          },
          {
            id: 2,
            startLocation: '上海站',
            startLatitude: 31.2494,
            startLongitude: 121.4568,
            endLocation: '上海迪士尼度假区',
            endLatitude: 31.1434,
            endLongitude: 121.6570,
            departureAt: '2025-12-29 09:00:00',
            statusDesc: '已到达',
            passengerCount: 4,
            matchAt: '2025-12-26 15:20:00',
            createdAt: '2025-12-26 14:00:00'
          }
        ]
      } finally {
        this.loading = false
      }
    },

    handleViewTrip(trip) {
      console.log('查看行程详情:', trip)
      // 可以跳转到详情页或打开对话框
      this.$router.push(`/trip/${trip.id}`)
    },

    async handleCancelTrip(trip) {
      console.log('取消行程:', trip)
      try {
        await axios.put(`http://47.100.65.234/api/trip/${trip.id}/cancel`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        alert('行程已取消')
        // 刷新列表
        this.fetchTrips()
      } catch (error) {
        console.error('取消行程失败:', error)
        const errorMsg = error.response?.data?.message || error.message
        alert('取消行程失败: ' + errorMsg)
      }
    }
  }
}
</script>

<style scoped>
.trip-example-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 1rem;
  color: #7f8c8d;
}

.page-content {
  margin-bottom: 40px;
}

.single-card-example {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 2px solid #e8eaed;
}

.single-card-example h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .trip-example-page {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 1.5rem;
  }

  .page-header p {
    font-size: 0.9rem;
  }
}
</style>
