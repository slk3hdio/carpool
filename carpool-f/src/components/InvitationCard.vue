<template>
  <div class="invitation-card" :class="statusClass">
    <div class="card-header">
      <div class="inviter-info">
        <div class="avatar">
          <span class="avatar-icon">👤</span>
        </div>
        <div class="inviter-details">
          <div class="inviter-name">{{ invitation.inviterName || '未知用户' }}</div>
          <div class="inviter-phone">{{ maskPhone(invitation.inviterPhone) }}</div>
        </div>
      </div>
      <div class="status-badge" :class="`status-${invitation.status}`">
        {{ invitation.statusDesc || '待处理' }}
      </div>
    </div>

    <div class="invitation-details">
      <div class="detail-item">
        <span class="detail-icon">👥</span>
        <span class="detail-label">乘车人数:</span>
        <span class="detail-value">{{ invitation.passengerCount }}人</span>
      </div>

      <div class="detail-item" v-if="invitation.message">
        <span class="detail-icon">💬</span>
        <span class="detail-label">留言:</span>
        <span class="detail-value message-text">{{ invitation.message }}</span>
      </div>

      <div class="detail-item">
        <span class="detail-icon">🕐</span>
        <span class="detail-label">发起时间:</span>
        <span class="detail-value">{{ formatTime(invitation.createdAt) }}</span>
      </div>
    </div>

    <div class="card-actions" v-if="invitation.status === 1">
      <button class="action-btn reject-btn" @click="handleReject" :disabled="processing">
        {{ processing ? '处理中...' : '拒绝' }}
      </button>
      <button class="action-btn accept-btn" @click="handleAccept" :disabled="processing">
        {{ processing ? '处理中...' : '接受' }}
      </button>
    </div>

    <div class="card-footer" v-else>
      <div class="footer-text">
        {{ getFooterText() }}
      </div>
      <button class="view-btn" @click="handleView" v-if="invitation.status === 2">
        查看详情
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'InvitationCard',
  props: {
    invitation: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      processing: false
    };
  },
  computed: {
    statusClass() {
      const status = this.invitation.status || 1;
      return `status-${status}`;
    }
  },
  methods: {
    formatTime(timeString) {
      if (!timeString) return '未知';
      const date = new Date(timeString);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${month}-${day} ${hours}:${minutes}`;
    },

    maskPhone(phone) {
      if (!phone) return '未提供';
      if (phone.length < 7) return phone;
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
    },

    getFooterText() {
      const status = this.invitation.status;
      switch (status) {
        case 2: return '✅ 您已接受此邀请';
        case 3: return '❌ 您已拒绝此邀请';
        case 4: return '🔔 此邀请已取消';
        default: return '';
      }
    },

    async handleAccept() {
      if (this.processing) return;

      const confirmed = confirm(`确定接受 ${this.invitation.inviterName || '该用户'} 的拼车邀请吗？`);
      if (!confirmed) return;

      this.processing = true;

      try {
        await axios.put(`http://localhost:8080/api/carpool/invitation/${this.invitation.id}/accept`);
        this.$message?.success('已接受邀请');
        this.$emit('updated');
      } catch (error) {
        console.error('接受邀请失败:', error);
        const errorMsg = error.response?.data?.message || '接受邀请失败';
        alert(errorMsg);
      } finally {
        this.processing = false;
      }
    },

    async handleReject() {
      if (this.processing) return;

      const confirmed = confirm(`确定拒绝 ${this.invitation.inviterName || '该用户'} 的拼车邀请吗？`);
      if (!confirmed) return;

      this.processing = true;

      try {
        await axios.put(`http://localhost:8080/api/carpool/invitation/${this.invitation.id}/reject`);
        this.$message?.success('已拒绝邀请');
        this.$emit('updated');
      } catch (error) {
        console.error('拒绝邀请失败:', error);
        const errorMsg = error.response?.data?.message || '拒绝邀请失败';
        alert(errorMsg);
      } finally {
        this.processing = false;
      }
    },

    handleView() {
      this.$emit('view', this.invitation);
    }
  }
};
</script>

<style scoped>
.invitation-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid #e8eaed;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.invitation-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53);
}

.invitation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 状态样式 */
.invitation-card.status-2::before {
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
}

.invitation-card.status-3::before {
  background: linear-gradient(90deg, #9E9E9E, #BDBDBD);
}

.invitation-card.status-4::before {
  background: linear-gradient(90deg, #FF9800, #FFB74D);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.inviter-info {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 24px;
}

.inviter-details {
  flex: 1;
}

.inviter-name {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.inviter-phone {
  font-size: 0.85rem;
  color: #666;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.status-1 {
  background: #FFF3E0;
  color: #F57C00;
}

.status-badge.status-2 {
  background: #E8F5E9;
  color: #388E3C;
}

.status-badge.status-3 {
  background: #FFEBEE;
  color: #D32F2F;
}

.status-badge.status-4 {
  background: #E3F2FD;
  color: #1976D2;
}

/* 邀请详情 */
.invitation-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px;
  background: #F8F9FA;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.detail-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.detail-label {
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
  flex: 1;
  word-break: break-all;
}

.message-text {
  font-style: italic;
  color: #555;
}

/* 操作按钮区域 */
.card-actions {
  display: flex;
  gap: 12px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.action-btn {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.accept-btn {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: white;
}

.accept-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.reject-btn {
  background: linear-gradient(135deg, #F44336, #FF5722);
  color: white;
}

.reject-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.footer-text {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.view-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .invitation-card {
    padding: 16px;
  }

  .card-header {
    margin-bottom: 12px;
  }

  .avatar {
    width: 40px;
    height: 40px;
  }

  .avatar-icon {
    font-size: 20px;
  }

  .card-actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .view-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .invitation-card {
    padding: 12px;
    border-radius: 12px;
  }

  .invitation-details {
    padding: 10px;
  }

  .detail-item {
    font-size: 0.85rem;
  }
}
</style>
