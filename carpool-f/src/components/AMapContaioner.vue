<template>
  <div ref="mapRef" class="map"></div>
</template>

<script>
import { onMounted, ref, nextTick } from "vue";

export default {
  name: "AMapContainer",
  setup() {
    const mapRef = ref(null);
    let map = null;

    // 等待AMap API加载完成
    const waitForAMap = () => {
      return new Promise((resolve) => {
        if (window.AMap) {
          resolve();
        } else {
          const checkInterval = setInterval(() => {
            if (window.AMap) {
              clearInterval(checkInterval);
              resolve();
            }
          }, 100);

          // 设置超时，防止无限等待
          setTimeout(() => {
            clearInterval(checkInterval);
            console.error("AMap API加载超时");
            resolve();
          }, 5000);
        }
      });
    };

    // 初始化地图
    const initMap = async () => {
      try {
        await waitForAMap();

        if (!window.AMap) {
          console.error("AMap SDK 未加载成功，请检查 index.html 是否正确引入。");
          return;
        }

        if (!mapRef.value) {
          console.error("地图容器元素不存在");
          return;
        }

        // 初始化地图
        map = new window.AMap.Map(mapRef.value, {
          viewMode: "3D",
          zoom: 13,
          center: [121.45806, 31.22222]
        });

        // 添加一个 marker 示例
        const marker = new window.AMap.Marker({
          position: [116.397428, 39.90923],
          title: "示例点位"
        });

        map.add(marker);
        console.log("地图初始化成功");
      } catch (error) {
        console.error("地图初始化失败:", error);
      }
    };

    onMounted(async () => {
      await nextTick();
      await initMap();
    });

    return { mapRef };
  }
};
</script>

<style scoped>
.map {
  width: 100%;
  height: 100%;
  border: none;
  overflow: hidden;
}

/* 确保地图在不同屏幕尺寸下正确显示 */
@media (max-width: 768px) {
  .map {
    min-height: calc(100vh - 60px);
  }
}

@media (max-width: 480px) {
  .map {
    min-height: calc(100vh - 60px);
  }
}
</style>
