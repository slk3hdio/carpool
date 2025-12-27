<template>
  <div class="invitation-list-container">
    <!-- 筛选标签 -->
    <div class="filter-tabs" v-if="!loading && invitations.length > 0">
      <button
        class="filter-tab"
        :class="{ active: activeFilter === 'all' }"
        @click="setFilter('all')"
      >
        全部 ({{ invitations.length }})
      </button>
      <button
        class="filter-tab"
        :class="{ active: activeFilter === 1 }"
        @click="setFilter(1)"
      >
        待处理 ({{ pendingCount }})
      </button>
      <button
        class="filter-tab"
        :class="{ active: activeFilter === 2 }"
        @click="setFilter(2)"
      >
        已接受 ({{ acceptedCount }})
      </button>
      <button
        class="filter-tab"
        :class="{ active: activeFilter === 3 }"
        @click="setFilter(3)"
      >
        已拒绝 ({{ rejectedCount }})
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredInvitations.length === 0" class="empty-state">
      <div class="empty-icon">📬</div>
      <h3>{{ emptyStateTitle }}</h3>
      <p>{{ emptyStateMessage }}</p>
    </div>

    <!-- 邀请列表 -->
    <div v-else class="invitation-list">
      <InvitationCard
        v-for="invitation in filteredInvitations"
        :key="invitation.id"
        :invitation="invitation"
        @updated="handleInvitationUpdated"
        @view="handleViewInvitation"
      />
    </div>
  </div>
</template>

<script>
import InvitationCard from './InvitationCard.vue';
import axios from 'axios';

export default {
  name: 'InvitationList',
  components: {
    InvitationCard
  },
  props: {
    carpoolRequestId: {
      type: Number,
      default: null
    },
    userId: {
      type: Number,
      default: null
    },
    received: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      invitations: [],
      loading: false,
      activeFilter: 'all' // 'all' or status code (1, 2, 3, 4)
    };
  },
  computed: {
    filteredInvitations() {
      if (this.activeFilter === 'all') {
        return this.invitations;
      }
      return this.invitations.filter(inv => inv.status === this.activeFilter);
    },

    pendingCount() {
      return this.invitations.filter(inv => inv.status === 1).length;
    },

    acceptedCount() {
      return this.invitations.filter(inv => inv.status === 2).length;
    },

    rejectedCount() {
      return this.invitations.filter(inv => inv.status === 3).length;
    },

    emptyStateTitle() {
      if (this.activeFilter === 'all') {
        return '暂无邀请';
      }
      switch (this.activeFilter) {
        case 1: return '暂无待处理的邀请';
        case 2: return '暂无已接受的邀请';
        case 3: return '暂无已拒绝的邀请';
        default: return '暂无邀请';
      }
    },

    emptyStateMessage() {
      if (this.activeFilter === 'all') {
        return '还没有人向您发送拼车邀请';
      }
      return '切换到其他标签查看更多邀请';
    }
  },
  mounted() {
    this.loadInvitations();
  },
  methods: {
    async loadInvitations() {
      this.loading = true;

      try {
        let url;

        if (this.carpoolRequestId) {
          // 获取某个拼车需求的邀请（需求发布者查看）
          url = `http://localhost:8080/api/carpool/invitation/request/${this.carpoolRequestId}`;
        } else if (this.userId) {
          if (this.received) {
            // 获取用户收到的邀请（别人向用户发布的拼车需求发送的邀请）
            url = `http://localhost:8080/api/carpool/invitation/received/${this.userId}`;
          } else {
            // 获取用户发起的邀请（用户自己查看）
            url = `http://localhost:8080/api/carpool/invitation/inviter/${this.userId}`;
          }
        } else {
          throw new Error('必须提供 carpoolRequestId 或 userId');
        }

        const response = await axios.get(url);
        this.invitations = response.data;
      } catch (error) {
        console.error('加载邀请失败:', error);
        this.$message?.error('加载邀请失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },

    setFilter(filter) {
      this.activeFilter = filter;
    },

    handleInvitationUpdated() {
      // 重新加载列表
      this.loadInvitations();
    },

    handleViewInvitation(invitation) {
      this.$emit('view', invitation);
    }
  }
};
</script>

<style scoped>
.invitation-list-container {
  width: 100%;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
}

.filter-tab {
  padding: 10px 20px;
  background: white;
  border: 2px solid #e8eaed;
  border-radius: 24px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-tab:hover {
  background: #f5f5f5;
  border-color: #667eea;
  color: #667eea;
}

.filter-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  font-size: 1rem;
  color: #666;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 1.25rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 0.95rem;
  color: #7f8c8d;
  margin: 0;
}

/* 邀请列表 */
.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-tabs {
    padding-bottom: 8px;
    gap: 8px;
  }

  .filter-tab {
    padding: 8px 16px;
    font-size: 0.85rem;
  }

  .invitation-list {
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .empty-icon {
    font-size: 48px;
  }

  .empty-state h3 {
    font-size: 1.1rem;
  }

  .empty-state p {
    font-size: 0.85rem;
  }
}
</style>
