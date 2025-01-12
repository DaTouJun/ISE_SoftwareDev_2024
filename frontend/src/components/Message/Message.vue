<template>
  <!-- 消息图标 -->
  <div>
    <div class="message-icon" @click="showDialog = true">
      <el-icon>
        <Bell/>
      </el-icon>
      <!-- 未读消息标识 -->
      <div v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</div>
    </div>
  </div>

  <!-- 发送信息图标 -->
  <div class="send-email-button" @click="showSendDialog = true">
    <el-icon>
      <Promotion/>
    </el-icon>
  </div>

  <!-- 消息弹窗 -->
  <el-dialog
      title="消息中心"
      v-model="showDialog"
      width="50%"
      :close-on-click-modal="false"
  >
    <el-table :data="sortedMessages" style="width: 100%">
      <!-- 时间列 -->
      <el-table-column
          prop="datetime"
          label="时间"
          width="160"
          :formatter="formatTime"
      ></el-table-column>

      <!-- 发送人列 -->
      <el-table-column
          prop="author.username"
          label="发送人"
          width="120"
      ></el-table-column>

      <!-- 消息内容列 -->
      <el-table-column label="消息内容">
        <template #default="{ row }">
          {{ row.message.length > 10 ? row.message.slice(0, 10) + '...' : row.message }}
        </template>
      </el-table-column>

      <!-- 已读/未读列 -->
      <el-table-column
          prop="isRead"
          label="状态"
          width="80"
      >
        <template #default="{ row }">
          <span v-if="row.isRead === 'false'" style="color: red;">未读</span>
          <span v-else style="color: green;">已读</span>
        </template>
      </el-table-column>

      <!-- 查看按钮列 -->
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹窗的内容显示 -->
    <el-dialog
        v-if="selectedMessage"
        :title="'查看消息: '"
        v-model="selectedMessageDialog"
        width="40%"
    >
      <!-- 显示信息 -->
      <div class="message-details">
        <div><strong>发送人：</strong> {{ selectedMessage.author.username }}</div>
        <div><strong>发送时间：</strong> {{ formatTime(null, null, selectedMessage.datetime) }}</div>
        <div><strong>状态：</strong>
          <span v-if="selectedMessage.isRead === 'false'" style="color: red;">未读</span>
          <span v-else style="color: green;">已读</span>
        </div>

        <!-- 内容部分 -->
        <div>
          <strong>消息内容：</strong>
          <div class="message-content">{{ selectedMessage.message }}</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="dialog-footer">
        <el-button v-if="selectedMessage.isRead === 'false'" @click="markAsRead(selectedMessage.id)" type="primary">
          标记为已读
        </el-button>
        <!--      <el-button @click="selectedMessageDialog = false">关闭</el-button>-->
      </div>
    </el-dialog>

    <!--  <template #footer>-->
    <!--    <el-button @click="showDialog = false">关闭</el-button>-->
    <!--  </template>-->
  </el-dialog>
  `

  <el-dialog
      title="发送消息"
      v-model="showSendDialog"
      width="40%"
  >
    <el-form>
      <!-- 选择用户 -->
      <el-form-item label="选择用户">
        <el-select v-model="selectedUser" placeholder="请选择用户">
          <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.username"
              :value="user.id"
          ></el-option>
        </el-select>
      </el-form-item>

      <!-- 邮件内容 -->
      <el-form-item label="消息内容">
        <el-input
            v-model="emailContent"
            type="textarea"
            placeholder="请输入消息内容"
            rows="4"
        ></el-input>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="showSendDialog = false">取消</el-button>
      <el-button type="primary" @click="sendEmail">发送</el-button>
    </template>
  </el-dialog>

</template>

<script setup>
import {
  Document,
  Promotion,
  Menu as IconMenu,
  Location,
  Setting,
  Bell,
} from '@element-plus/icons-vue'
import { reactive, ref, onMounted, computed } from 'vue';
import 'element-plus/theme-chalk/el-message.css';
import { ElMessage } from 'element-plus'
import http from '@/http/request.js';
onMounted(() => {
  fetchMessages();
  fetchUsers()
})

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  console.log(key, keyPath)
}

// 控制弹窗显示
const showDialog = ref(false)

// 控制选择的消息弹窗
const selectedMessageDialog = ref(false)

// 消息数据
const messages = reactive([])

// 存储选中的消息
const selectedMessage = ref(null)

// 获取消息列表
const fetchMessages = async () => {
  try {
    const response = await http.post('http://localhost:8081/api/messages/all')
    messages.splice(0, messages.length, ...response.data) // 更新消息数据
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage({
      message: '获取消息失败',
      type: 'error',
    })
  }
}

// 获取未读消息数量
const unreadCount = computed(() => {
  return messages.filter((msg) => msg.isRead === 'false').length
})

// 格式化时间
const formatTime = (row, column, value) => {
  const date = new Date(value)
  return date.toLocaleString()
}

// 排序后的消息列表
const sortedMessages = computed(() => {
  return [...messages]
      .sort((a, b) => {
        // 按未读优先和时间倒序排序
        if (a.isRead === 'false' && b.isRead !== 'false') return -1
        if (a.isRead !== 'false' && b.isRead === 'false') return 1
        return new Date(b.datetime).getTime() - new Date(a.datetime).getTime()
      })
})

// 查看按钮逻辑
const handleView = (row) => {
  selectedMessage.value = row
  selectedMessageDialog.value = true
}

// 标记消息为已读
const markAsRead = async (messageId) => {
  try {
    const response = await http.post('http://localhost:8081/api/messages/markAsRead', { messageId })
    // 更新消息的状态
    const message = messages.find(msg => msg.id === messageId)
    if (message) {
      message.isRead = 'true'
    }
  } catch (error) {
    console.error('标记消息为已读失败:', error)
  }
}

// 发送邮件功能
const showSendDialog = ref(false)
const userList = reactive([])
const selectedUser = ref(null)
const emailContent = ref('')

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await http.get('http://localhost:8081/api/user/all')
    userList.splice(0, userList.length, ...response.data)
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 发送邮件
const sendEmail = async () => {
  if (!selectedUser.value || !emailContent.value.trim()) {
    return alert('请选择用户并填写内容')
  }
  try {
    await http.post('http://localhost:8081/api/messages/send', {
      receiverId: [selectedUser.value], // 用户ID需以数组形式传递
      message: [emailContent.value]    // 消息内容需以数组形式传递
    })
    ElMessage({
      message: '消息发送成功',
      type: 'success',
    })
    showSendDialog.value = false
    emailContent.value = ''
    selectedUser.value = null
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage({
      message: '发送消息失败',
      type: 'error',
    })
  }
  await fetchMessages()
}
</script>


<style scoped>
.message-icon {
  position: fixed;
  bottom: 20px; /* 距离底部20px */
  left: 20px; /* 距离左侧20px */
  width: 50px;
  height: 50px;
  background-color: #409eff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  font-size: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.message-icon:hover {
  background-color: #66b1ff;
}

.message-content {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f9fafb;
}

.message-details {
  padding: 20px;
}

.unread-badge {
  position: absolute;
  top: -3px;
  right: -3px;
  background-color: red;
  color: white;
  font-size: 12px;
  width: 20px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  font-weight: bold;
}

.send-email-button {
  position: fixed;
  bottom: 20px;
  left: 90px;
  width: 50px;
  height: 50px;
  background-color: #67c23a;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  font-size: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.send-email-button:hover {
  background-color: #85e377;
}
</style>