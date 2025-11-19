<template>
  <div class="home">
    <h2>欢迎使用同城拼车与实时路况系统</h2>

    <!-- 地图显示区域 -->
    <div class="map-container">
      <div id="map" class="map"></div>
    </div>

    <div class="card-group">
      <router-link to="/traffic" class="card">实时路况查看</router-link>
      <router-link to="/carpool" class="card">我要拼车</router-link>
      <router-link to="/user" class="card">用户中心</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  mounted() {
    this.initMap();
  },
  methods: {
    initMap() {
      // 检查高德地图API是否加载
      if (typeof AMap === 'undefined') {
        console.error('高德地图API未正确加载，请检查API密钥配置');
        return;
      }

      // 初始化地图
      const map = new AMap.Map('map', {
        zoom: 12, // 缩放级别
        center: [121.5, 31.25], // 中心点坐标（北京天安门）
        viewMode: '3D', // 使用3D视图
        mapStyle: 'amap://styles/normal' // 地图样式
      });

      // 添加比例尺控件
      map.addControl(new AMap.Scale());

      // 添加工具条控件
      map.addControl(new AMap.ToolBar());

      // 添加标记点
      const marker = new AMap.Marker({
        position: [116.397428, 39.90923],
        title: '当前位置'
      });
      map.add(marker);

      console.log('高德地图初始化成功');
    }
  }
}
</script>

<style>
.home {
  padding: 20px;
}

/* 地图容器样式 */
.map-container {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.map {
  width: 100%;
  height: 700px;
  border-radius: 8px;
}

.card-group {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}
.card {
  flex: 1;
  padding: 20px;
  background: #e6f2ff;
  border-radius: 8px;
  text-align: center;
  font-size: 18px;
  text-decoration: none;
  color: #1e90ff;
}
.card:hover {
  background: #d4e8ff;
}
</style>
