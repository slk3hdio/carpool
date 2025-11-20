<template>
  <div class="home">
    <!-- 地图显示区域 -->
    <div class="map-section">
      <AMapContaioner />

      <!-- 浮动操作面板 -->
      <!-- <div class="floating-panel">
        <div class="panel-header">
          <h3 class="title">同城拼车与实时路况</h3>
        </div>
        <div class="panel-actions">
          <button class="primary-btn" @click="panelVisible = true">
            <span class="btn-icon">🚗</span>
            发布拼车
          </button>
          <router-link to="/traffic" class="secondary-btn">
            <span class="btn-icon">🚦</span>
            实时路况
          </router-link>
        </div>
      </div> -->
    </div>

    <CarpoolPanel v-model:visible="panelVisible" />
  </div>
</template>

<script>
import AMapContaioner from "../components/AMapContaioner.vue"
import CarpoolPanel from "../components/CarpoolPanel.vue"
export default {
    components:{
        AMapContaioner,
        CarpoolPanel,
    },

    data() {
      return {
        panelVisible: false,
      }
    }
};
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 60px 0 0 0;
  position: relative;
}

/* 地图区域样式 */
.map-section {
  position: relative;
  height: calc(100vh - 60px);
  padding: 0;
}

/* 浮动操作面板样式 */
.floating-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  max-width: 350px;
  z-index: 100;
}

.panel-header {
  margin-bottom: 20px;
}

.title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.3;
}

.panel-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.primary-btn,
.secondary-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  text-decoration: none;
  width: 100%;
}

.secondary-btn {
  background: #ffffff;
  color: #667eea;
  border: 2px solid #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.primary-btn:active,
.secondary-btn:active {
  transform: translateY(0);
}

.btn-icon {
  font-size: 1.1rem;
}

/* 桌面端样式 */
@media (min-width: 769px) {
  .floating-panel {
    top: 30px;
    left: 30px;
    padding: 25px;
    max-width: 400px;
  }

  .panel-actions {
    flex-direction: row;
    gap: 15px;
  }

  .primary-btn,
  .secondary-btn {
    flex: 1;
  }

  .title {
    font-size: 1.7rem;
  }
}

/* 大屏幕优化 */
@media (min-width: 1200px) {
  .floating-panel {
    max-width: 450px;
    padding: 30px;
  }

  .primary-btn,
  .secondary-btn {
    padding: 16px 28px;
    font-size: 1.1rem;
  }
}

/* 平板端样式 */
@media (max-width: 768px) {
  .home {
    padding-top: 60px;
  }

  .map-section {
    height: calc(100vh - 60px);
  }

  .floating-panel {
    top: 15px;
    left: 15px;
    right: 15px;
    max-width: none;
    padding: 18px;
  }

  .title {
    font-size: 1.4rem;
    text-align: center;
  }

  .panel-actions {
    gap: 10px;
  }

  .primary-btn,
  .secondary-btn {
    padding: 12px 20px;
    font-size: 0.95rem;
  }
}

/* 手机端样式 */
@media (max-width: 480px) {
  .floating-panel {
    top: 10px;
    left: 10px;
    right: 10px;
    padding: 15px;
    border-radius: 12px;
  }

  .panel-header {
    margin-bottom: 15px;
  }

  .title {
    font-size: 1.3rem;
    text-align: center;
  }

  .panel-actions {
    gap: 10px;
  }

  .primary-btn,
  .secondary-btn {
    padding: 12px 18px;
    font-size: 0.9rem;
  }

  .btn-icon {
    font-size: 1rem;
  }
}

/* 超小屏幕优化 */
@media (max-width: 360px) {
  .floating-panel {
    top: 5px;
    left: 5px;
    right: 5px;
    padding: 12px;
  }

  .title {
    font-size: 1.2rem;
  }

  .panel-actions {
    gap: 8px;
  }

  .primary-btn,
  .secondary-btn {
    padding: 10px 16px;
    font-size: 0.85rem;
  }
}
</style>
