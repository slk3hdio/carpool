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
      class="invitation-panel"
      :class="{ show: visible }"
    >
      <!-- 顶部手柄 -->
      <div class="handler" @click="toggle"></div>

      <!-- 内容区域 -->
      <div class="content">
        <h3>发起拼车邀请</h3>

        <!-- 拼车需求信息预览 -->
        <div class="request-preview" v-if="carpoolRequest">
          <div class="preview-header">
            <span class="preview-icon">{{ carpoolRequest.hasCar ? '🚗' : '👤' }}</span>
            <span class="preview-title">{{ carpoolRequest.hasCar ? '车主信息' : '乘客信息' }}</span>
          </div>
          <div class="preview-route">
            <div class="route-item">
              <span class="route-label">起点:</span>
              <span class="route-value">{{ carpoolRequest.startLocation }}</span>
            </div>
            <div class="route-arrow">↓</div>
            <div class="route-item">
              <span class="route-label">终点:</span>
              <span class="route-value">{{ carpoolRequest.endLocation }}</span>
            </div>
          </div>
          <div class="preview-time">
            <span class="time-label">出发时间:</span>
            <span class="time-value">{{ formatTime(carpoolRequest.earliestDepartureTime) }}</span>
          </div>
        </div>

        <!-- 发起者ID (通常从登录状态获取) -->
        <!-- <div class="item">
          <label>发起者ID</label>
          <input type="number" v-model="form.inviterId" placeholder="请输入发起者ID" disabled />
          <div class="hint">当前登录用户ID</div>
        </div> -->

        <!-- 发起者人数 -->
        <div class="item">
          <label>您的乘车人数</label>
          <input
            type="number"
            v-model="form.passengerCount"
            min="1"
            :max="maxPassengers"
            placeholder="请输入乘车人数"
          />
          <div class="hint" v-if="carpoolRequest && carpoolRequest.hasCar">
            该车主最多可载 {{ carpoolRequest.maxPassengerCount - carpoolRequest.passengerCount }} 人
          </div>
        </div>

        <!-- 留言备注 -->
        <div class="item">
          <label>留言备注</label>
          <textarea
            v-model="form.message"
            placeholder="请输入留言（可选）"
            rows="3"
            maxlength="200"
          ></textarea>
        </div>

        <!-- 提示信息 -->
        <div class="info-box">
          <span class="info-icon">ℹ️</span>
          <span class="info-text">发送邀请后，对方将收到您的拼车请求</span>
        </div>

        <!-- 提交按钮 -->
        <div class="button-group">
          <button class="cancel-btn" @click="close" :disabled="submitting">
            取消
          </button>
          <button class="submit-btn" @click="submitForm" :disabled="submitting">
            {{ submitting ? '发送中...' : '发送邀请' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import axios from 'axios';
import { useUserStore } from '../stores/user';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  carpoolRequest: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'submitted']);
const userStore = useUserStore();

const form = ref({
  inviterId: userStore.userId || null,
  carpoolRequestId: null,
  passengerCount: 1,
  message: ''
});

const submitting = ref(false);

// 最大乘客数限制
const maxPassengers = computed(() => {
  if (props.carpoolRequest && props.carpoolRequest.hasCar) {
    return props.carpoolRequest.maxPassengerCount - props.carpoolRequest.passengerCount;
  }
  return 10; // 默认最大值
});

// 监听拼车需求变化，更新表单
watch(() => props.carpoolRequest, (newRequest) => {
  if (newRequest) {
    form.value.carpoolRequestId = newRequest.id;
  }
}, { immediate: true });

// 监听用户登录状态
watch(() => userStore.userId, (newUserId) => {
  if (newUserId) {
    form.value.inviterId = newUserId;
  }
}, { immediate: true });

const close = () => {
  emit('update:visible', false);
  // 延迟重置表单，等待动画完成
  setTimeout(() => {
    resetForm();
  }, 300);
};

const toggle = () => {
  emit('update:visible', !props.visible);
};

// 提交表单
const submitForm = async () => {
  // 表单验证
  if (!userStore.userId) {
    alert('请先登录');
    return;
  }

  if (!props.carpoolRequest) {
    alert('拼车需求信息不存在');
    return;
  }

  if (!form.value.passengerCount || form.value.passengerCount < 1) {
    alert('请输入乘车人数');
    return;
  }

  // 验证乘客数是否超过限制
  if (props.carpoolRequest.hasCar && form.value.passengerCount > props.carpoolRequest.maxPassengerCount) {
    alert(`乘车人数不能超过车主可载人数（${props.carpoolRequest.maxPassengerCount}人）`);
    return;
  }

  submitting.value = true;

  try {
    const formData = {
      inviterId: form.value.inviterId,
      carpoolRequestId: form.value.carpoolRequestId,
      passengerCount: form.value.passengerCount,
      message: form.value.message
    };

    const response = await axios.post('http://localhost:8080/api/carpool/invitation', formData);
    alert('邀请发送成功！');
    emit('submitted', response.data);
    close();
  } catch (error) {
    console.error('发送邀请失败:', error);
    alert(error.response?.data?.message || '发送邀请失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '待定';
  const date = new Date(timeString);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${month}-${day} ${hours}:${minutes}`;
};

const resetForm = () => {
  form.value = {
    inviterId: userStore.userId || null,
    carpoolRequestId: null,
    passengerCount: 1,
    message: ''
  };
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
.invitation-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: -80%;
  height: 80%;
  background: white;
  border-radius: 14px 14px 0 0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 999;
  transition: all 0.3s ease;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

/* 打开状态从底部滑出 */
.invitation-panel.show {
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
  flex-shrink: 0;
}

/* 内容区域 */
.content {
  padding: 0 20px 20px 20px;
  overflow-y: auto;
  flex: 1;
  -webkit-overflow-scrolling: touch;
}

.content h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
  padding-top: 10px;
}

/* 拼车需求预览卡片 */
.request-preview {
  background: linear-gradient(135deg, #667eea15, #764ba215);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #667eea30;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.preview-icon {
  font-size: 24px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.preview-route {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.route-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.route-label {
  font-size: 12px;
  color: #666;
}

.route-value {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  word-break: break-all;
}

.route-arrow {
  font-size: 18px;
  color: #667eea;
  flex-shrink: 0;
}

.preview-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 12px;
  color: #666;
}

.time-value {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

/* 输入项 */
.item {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
}

.item label {
  font-size: 13px;
  margin-bottom: 4px;
  color: #333;
  font-weight: 500;
}

.item input,
.item textarea {
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
  font-family: inherit;
}

.item input:disabled {
  background: #f5f5f5;
  color: #999;
}

.item textarea {
  resize: vertical;
  min-height: 60px;
}

.hint {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

/* 提示信息框 */
.info-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #E3F2FD;
  border-radius: 6px;
  margin: 16px 0;
}

.info-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.info-text {
  font-size: 13px;
  color: #1976D2;
  line-height: 1.4;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.submit-btn,
.cancel-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn {
  background: #1e90ff;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #1174cc;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover:not(:disabled) {
  background: #e0e0e0;
}

.cancel-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* PC 端优化 */
@media (min-width: 600px) {
  .invitation-panel {
    border-radius: 14px;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .content {
    padding: 0 16px 16px 16px;
  }

  .request-preview {
    padding: 12px;
  }

  .button-group {
    flex-direction: column;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
  }
}
</style>
