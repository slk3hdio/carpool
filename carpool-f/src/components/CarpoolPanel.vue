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
        <h3>{{ panelTitle }}</h3>

        <!-- 用户ID (通常从登录状态获取) -->
        <!-- <div class="item">
          <label>用户ID</label>
          <input type="number" v-model="form.userId" placeholder="请输入用户ID" />
        </div> -->

        <!-- 是否有车 -->
        <div class="item">
          <label>拼车类型</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="form.hasCar" :value="true" />
              我有车
            </label>
            <label class="radio-label">
              <input type="radio" v-model="form.hasCar" :value="false" />
              我找车
            </label>
          </div>
        </div>

        <!-- 最大乘客数量 (仅当有车时显示) -->
        <div class="item" v-if="form.hasCar">
          <label>最大乘客数</label>
          <input type="number" v-model="form.maxPassengerCount" min="1" max="20" placeholder="最大乘客数" />
        </div>

        <!-- 乘客数量 -->
        <div class="item">
          <label>{{ form.hasCar ? '已乘坐乘客数' : '乘客数量' }}</label>
          <input type="number" v-model="form.passengerCount" :min="1" :max="form.hasCar ? form.maxPassengerCount : 1" placeholder="乘客数量" />
        </div>

        <!-- 起点 -->
        <div class="item">
          <label>起点</label>
          <input type="text" v-model="form.startLocation" placeholder="请输入起点位置" />
          <div class="coords-hint" v-if="form.startLatitude && form.startLongitude">
            坐标: {{ form.startLatitude.toFixed(6) }}, {{ form.startLongitude.toFixed(6) }}
          </div>
        </div>

        <!-- 终点 -->
        <div class="item">
          <label>终点</label>
          <input type="text" v-model="form.endLocation" placeholder="请输入终点位置" />
          <div class="coords-hint" v-if="form.endLatitude && form.endLongitude">
            坐标: {{ form.endLatitude.toFixed(6) }}, {{ form.endLongitude.toFixed(6) }}
          </div>
        </div>

        <!-- 最早出发时间 -->
        <div class="item">
          <label>最早出发时间</label>
          <input
            type="datetime-local"
            v-model="form.earliestDepartureTime"
          />
        </div>

        <!-- 最晚出发时间 -->
        <div class="item">
          <label>最晚出发时间</label>
          <input
            type="datetime-local"
            v-model="form.latestDepartureTime"
          />
        </div>

        <!-- 手机号 -->
        <div class="item">
          <label>手机号</label>
          <input type="tel" v-model="form.phoneNumber" maxlength="11" placeholder="请输入手机号" />
        </div>

        <!-- 状态描述 -->
        <div class="item">
          <label>状态描述</label>
          <textarea
            v-model="form.statusDesc"
            placeholder="请输入备注信息（可选）"
            rows="2"
            maxlength="200"
          ></textarea>
        </div>

        <!-- 提交按钮 -->
        <button class="submit-btn" @click="submitForm" :disabled="submitting">
          {{ submitButtonText }}
        </button>
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
  editData: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'submitted']);
const userStore = useUserStore();

const form = ref({
  userId: userStore.userId || null,
  hasCar: true,
  maxPassengerCount: 4,
  passengerCount: 1,
  startLocation: '',
  startLatitude: null,
  startLongitude: null,
  endLocation: '',
  endLatitude: null,
  endLongitude: null,
  earliestDepartureTime: '',
  latestDepartureTime: '',
  phoneNumber: '',
  statusDesc: '寻找拼车'
});

const submitting = ref(false);
const isEditMode = computed(() => props.editData !== null);
const panelTitle = computed(() => isEditMode.value ? '编辑拼车' : '发布拼车');
const submitButtonText = computed(() => submitting.value ? '提交中...' : (isEditMode.value ? '保存修改' : '发布拼车'));

// 监听 editData 变化，自动填充表单
watch(() => props.editData, (newData) => {
  if (newData) {
    form.value = {
      userId: newData.userId,
      hasCar: newData.hasCar,
      maxPassengerCount: newData.maxPassengerCount || 4,
      passengerCount: newData.passengerCount || 1,
      startLocation: newData.startLocation || '',
      startLatitude: newData.startLatitude,
      startLongitude: newData.startLongitude,
      endLocation: newData.endLocation || '',
      endLatitude: newData.endLatitude,
      endLongitude: newData.endLongitude,
      earliestDepartureTime: newData.earliestDepartureTime ? newData.earliestDepartureTime.slice(0, 16) : '',
      latestDepartureTime: newData.latestDepartureTime ? newData.latestDepartureTime.slice(0, 16) : '',
      phoneNumber: newData.phoneNumber || '',
      statusDesc: newData.statusDesc || '寻找拼车'
    };
  }
}, { immediate: true });

const close = () => {
  emit('update:visible', false);
  // 重置表单
  if (!isEditMode.value) {
    resetForm();
  }
};

const toggle = () => {
  emit('update:visible', !props.visible);
};

const resetForm = () => {
  form.value = {
    userId: userStore.userId || null,
    hasCar: true,
    maxPassengerCount: 4,
    passengerCount: 1,
    startLocation: '',
    startLatitude: null,
    startLongitude: null,
    endLocation: '',
    endLatitude: null,
    endLongitude: null,
    earliestDepartureTime: '',
    latestDepartureTime: '',
    phoneNumber: '',
    statusDesc: '寻找拼车'
  };
};

// 提交表单
const submitForm = async () => {
  // 表单验证
  if (!userStore.userId) {
    alert('请先登录');
    return;
  }

  // 更新userId为当前登录用户ID
  form.value.userId = userStore.userId;

  if (!form.value.startLocation || !form.value.endLocation) {
    alert('请填写起点和终点');
    return;
  }

  if (!form.value.earliestDepartureTime || !form.value.latestDepartureTime) {
    alert('请选择出发时间');
    return;
  }

  if (!form.value.phoneNumber) {
    alert('请填写手机号');
    return;
  }

  if (!/^1[3-9]\d{9}$/.test(form.value.phoneNumber)) {
    alert('请输入正确的手机号');
    return;
  }

  // 有车时才需要验证最大乘客数
  if (form.value.hasCar && (!form.value.maxPassengerCount || form.value.maxPassengerCount < 1)) {
    alert('请填写最大乘客数');
    return;
  }

  submitting.value = true;

  try {
    console.log('CarpoolPanel - isEditMode:', isEditMode.value);
    console.log('CarpoolPanel - editData:', props.editData);

    // 格式化时间，并根据hasCar设置maxPassengerCount
    const formData = {
      ...form.value,
      maxPassengerCount: form.value.hasCar ? form.value.maxPassengerCount : 0,
      earliestDepartureTime: formatDateTime(form.value.earliestDepartureTime),
      latestDepartureTime: formatDateTime(form.value.latestDepartureTime)
    };

    if (isEditMode.value) {
      // 编辑模式：更新现有需求
      console.log('CarpoolPanel - 发起更新请求，ID:', props.editData.id, '数据:', formData);
      await axios.put(`http://localhost:8080/api/carpool/request/${props.editData.id}`, formData);
      alert('修改成功！');
    } else {
      // 创建模式：发布新需求
      console.log('CarpoolPanel - 发起创建请求，数据:', formData);
      await axios.post('http://localhost:8080/api/carpool/request', formData);
      alert('发布成功！');
    }

    emit('submitted');
    close();

    // 重置表单
    resetForm();
  } catch (error) {
    console.error(isEditMode.value ? '修改失败:' : '发布失败:', error);
    if (error.response?.status === 403) {
      alert(error.response?.data?.message || '您没有权限修改此拼车需求');
    } else {
      alert(error.response?.data?.message || (isEditMode.value ? '修改失败' : '发布失败') + '，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};

// 格式化日期时间为 yyyy-MM-dd HH:mm:ss
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  // datetime-local 返回的是 yyyy-MM-ddTHH:mm 格式，需要转换为 yyyy-MM-dd HH:mm:ss
  return dateTimeStr.replace('T', ' ') + ':00';
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
  bottom: -80%;
  height: 80%;
  background: white;
  border-radius: 14px 14px 0 0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 999;
  transition: all 0.3s ease;
  max-width: 480px;
  margin: 0 auto; /* PC 居中 */
  display: flex;
  flex-direction: column;
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

.item textarea {
  resize: vertical;
  min-height: 60px;
}

/* 单选按钮组 */
.radio-group {
  display: flex;
  gap: 20px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  cursor: pointer;
}

.radio-label input[type="radio"] {
  width: auto;
  margin: 0;
}

/* 坐标提示 */
.coords-hint {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

/* 按钮 */
.submit-btn {
  margin-top: 16px;
  width: 100%;
  padding: 12px;
  background: #1e90ff;
  border: none;
  color: white;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.submit-btn:hover:not(:disabled) {
  background: #1174cc;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* PC 端优化：面板宽度最大 480px */
@media (min-width: 600px) {
  .carpool-panel {
    border-radius: 14px;
  }
}
</style>
