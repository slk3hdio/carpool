<template>
  <div>
    <!-- 遮罩层 -->
    <div
      v-if="visible"
      class="overlay"
      @click="close"
    ></div>

    <!-- 底部面板 -->
    <div
      class="carpool-panel"
      :class="{ show: visible }"
    >
      <!-- 顶部手柄 -->
      <div class="handler" @click="toggle"></div>

      <!-- 内容区域 -->
      <div class="content">
        <h3>拼车信息</h3>

        <!-- 示例内容（你可替换为实际 UI） -->
        <div class="item">
          <label>起点</label>
          <input type="text" placeholder="请输入起点" />
        </div>

        <div class="item">
          <label>终点</label>
          <input type="text" placeholder="请输入终点" />
        </div>

        <div class="item">
          <label>人数</label>
          <input type="number" min="1" placeholder="1" />
        </div>

        <button class="submit-btn">发布拼车</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CarpoolPanel",
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ["update:visible"],

  methods: {
    close() {
      this.$emit("update:visible", false);
    },
    toggle() {
      this.$emit("update:visible", !this.visible);
    }
  }
};
</script>

<style scoped>
/* 遮罩层 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.45);
  z-index: 998;
}

/* 底部面板主体 */
.carpool-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: -60%;
  height: 60%;
  background: white;
  border-radius: 14px 14px 0 0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 999;
  transition: all 0.3s ease;
  max-width: 480px;
  margin: 0 auto; /* PC 居中 */
}

/* 打开状态从底部滑出 */
.carpool-panel.show {
  bottom: 0;
}

/* 顶部拖动小手柄 */
.handler {
  width: 40px;
  height: 6px;
  background: #ddd;
  border-radius: 3px;
  margin: 10px auto;
  cursor: pointer;
}

/* 内容区域 */
.content {
  padding: 20px;
}

/* 输入项 */
.item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.item label {
  font-size: 14px;
  margin-bottom: 4px;
}

.item input {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
}

/* 按钮 */
.submit-btn {
  margin-top: 20px;
  width: 100%;
  padding: 12px;
  background: #1e90ff;
  border: none;
  color: white;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #1174cc;
}

/* PC 端优化：面板宽度最大 480px */
@media (min-width: 600px) {
  .carpool-panel {
    border-radius: 14px;
  }
}
</style>
