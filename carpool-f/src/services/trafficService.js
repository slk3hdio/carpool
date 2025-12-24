import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    console.log('API Request:', config.method?.toUpperCase(), config.url)
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('API Response:', response.config.url, response.status)
    return response.data
  },
  error => {
    console.error('Response Error:', error)

    if (error.response) {
      // 服务器返回了错误状态码
      console.error('Error Status:', error.response.status)
      console.error('Error Data:', error.response.data)

      // 可以根据不同的错误状态码进行不同的处理
      switch (error.response.status) {
        case 404:
          throw new Error('请求的资源不存在')
        case 500:
          throw new Error('服务器内部错误')
        default:
          throw new Error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('No Response Received')
      throw new Error('网络连接失败，请检查网络设置')
    } else {
      // 请求配置出错
      console.error('Request Config Error:', error.message)
      throw new Error('请求配置错误')
    }
  }
)

// 路况数据服务
const trafficService = {
  /**
   * 获取所有路况信息（分页）
   * @param {Object} params 查询参数
   * @param {number} params.page 页码，默认0
   * @param {number} params.size 每页大小，默认20
   * @param {string} params.sortBy 排序字段，默认requestTime
   * @param {string} params.sortDir 排序方向，默认desc
   * @returns {Promise} 路况数据列表
   */
  async getAllTraffic(params = {}) {
    const {
      page = 0,
      size = 20,
      sortBy = 'requestTime',
      sortDir = 'desc'
    } = params

    try {
      const response = await api.get('/traffic', {
        params: { page, size, sortBy, sortDir }
      })
      return response
    } catch (error) {
      console.error('获取路况数据失败:', error)
      throw error
    }
  },

  /**
   * 根据城市获取路况信息
   * @param {string} city 城市名称
   * @param {Object} params 查询参数
   * @returns {Promise} 指定城市的路况数据
   */
  async getTrafficByCity(city, params = {}) {
    const {
      page = 0,
      size = 20
    } = params

    try {
      const response = await api.get(`/traffic/city/${city}`, {
        params: { page, size }
      })
      return response
    } catch (error) {
      console.error(`获取${city}路况数据失败:`, error)
      throw error
    }
  },

  /**
   * 根据道路和城市查询路况
   * @param {string} roadName 道路名称
   * @param {string} city 城市名称
   * @param {Object} params 查询参数
   * @returns {Promise} 指定道路的路况数据
   */
  async getTrafficByRoadAndCity(roadName, city, params = {}) {
    const {
      page = 0,
      size = 10
    } = params

    try {
      const response = await api.get(
        `/traffic/road/${roadName}/city/${city}`,
        { params: { page, size } }
      )
      return response
    } catch (error) {
      console.error(`获取${roadName}(${city})路况数据失败:`, error)
      throw error
    }
  },

  /**
   * 根据拥堵状态查询路况
   * @param {number} status 拥堵状态 (0-4)
   * @returns {Promise} 指定状态的路况数据
   */
  async getTrafficByStatus(status) {
    try {
      const response = await api.get(`/traffic/status/${status}`)
      return response
    } catch (error) {
      console.error(`获取状态${status}的路况数据失败:`, error)
      throw error
    }
  },

  /**
   * 搜索路况信息
   * @param {string} keyword 搜索关键词
   * @returns {Promise} 搜索结果
   */
  async searchTraffic(keyword) {
    try {
      const response = await api.get('/traffic/search', {
        params: { keyword }
      })
      return response
    } catch (error) {
      console.error(`搜索路况关键词"${keyword}"失败:`, error)
      throw error
    }
  },

  /**
   * 获取路况统计信息
   * @returns {Promise} 路况统计数据
   */
  async getTrafficStats() {
    try {
      const response = await api.get('/traffic/stats')
      return response
    } catch (error) {
      console.error('获取路况统计信息失败:', error)
      throw error
    }
  },

  /**
   * 获取路况详情
   * @param {number} id 路况ID
   * @returns {Promise} 路况详细信息
   */
  async getTrafficDetails(id) {
    try {
      const response = await api.get(`/traffic/${id}`)
      return response
    } catch (error) {
      console.error(`获取路况详情(ID:${id})失败:`, error)
      throw error
    }
  },

  /**
   * 获取路况概览数据（用于首页展示）
   * @param {Object} params 查询参数
   * @returns {Promise} 路况概览数据
   */
  async getTrafficOverview(params = {}) {
    const {
      page = 0,
      size = 12
    } = params

    try {
      const response = await api.get('/traffic/overview', {
        params: { page, size }
      })
      return response
    } catch (error) {
      console.error('获取路况概览数据失败:', error)
      throw error
    }
  },

  /**
   * 获取热门道路路况
   * @returns {Promise} 热门道路路况列表
   */
  async getPopularTraffic() {
    try {
      const response = await api.get('/traffic/popular')
      return response
    } catch (error) {
      console.error('获取热门道路路况失败:', error)
      throw error
    }
  },

  /**
   * 获取历史路况数据
   * @param {Object} params 查询参数
   * @param {string} params.roadName 道路名称
   * @param {string} params.city 城市名称
   * @param {string} params.startTime 开始时间 (ISO string)
   * @param {string} params.endTime 结束时间 (ISO string)
   * @param {number} params.page 页码，默认0
   * @param {number} params.size 每页大小，默认100
   * @returns {Promise} 历史路况数据
   */
  async getHistoricalTraffic(params = {}) {
    const {
      roadName,
      city,
      startTime,
      endTime,
      page = 0,
      size = 100
    } = params

    if (!roadName || !city) {
      throw new Error('道路名称和城市名称不能为空')
    }

    if (!startTime || !endTime) {
      throw new Error('开始时间和结束时间不能为空')
    }

    try {
      const response = await api.get('/traffic/historical', {
        params: {
          roadName,
          city,
          startTime,
          endTime,
          page,
          size
        }
      })
      return response.content || response
    } catch (error) {
      console.error(`获取历史路况数据失败 (${roadName}, ${city}):`, error)
      throw error
    }
  },

  /**
   * 获取城市道路列表
   * @param {string} city 城市名称
   * @returns {Promise} 道路列表
   */
  async getRoadsByCity(city) {
    if (!city) {
      throw new Error('城市名称不能为空')
    }

    try {
      const response = await api.get(`/traffic/cities/${city}/roads`)
      return response || []
    } catch (error) {
      console.error(`获取${city}道路列表失败:`, error)
      throw error
    }
  },

  /**
   * 获取支持的城市列表
   * @returns {Promise} 城市列表
   */
  async getSupportedCities() {
    try {
      const response = await api.get('/traffic/cities')
      return response || []
    } catch (error) {
      console.error('获取支持的城市列表失败:', error)
      throw error
    }
  },

  /**
   * 格式化路况数据，适配前端组件
   * @param {Object} trafficData 后端返回的路况数据
   * @returns {Object} 格式化后的路况数据
   */
  formatTrafficData(trafficData) {
    return {
      id: trafficData.id,
      road_name: trafficData.roadName,
      city: trafficData.city,
      evaluation_status: trafficData.evaluationStatus,
      evaluation_status_desc: trafficData.evaluationStatusDesc,
      description: trafficData.description,
      request_time: trafficData.requestTime,
      speed: trafficData.speed,
      congestion_distance: trafficData.congestionDistance,
      statusText: trafficData.statusText
    }
  },

  /**
   * 获取拥堵状态的颜色类
   * @param {number} status 拥堵状态 (0-4)
   * @returns {string} CSS类名
   */
  getCongestionClass(status) {
    const classMap = {
      0: 'congestion-0',
      1: 'congestion-1',
      2: 'congestion-2',
      3: 'congestion-3',
      4: 'congestion-4'
    }
    return classMap[status] || 'congestion-0'
  }
}

export default trafficService