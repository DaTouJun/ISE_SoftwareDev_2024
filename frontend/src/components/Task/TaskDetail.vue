<template>
  <div class="task-detail">
    <!-- 任务标题和描述 -->
    <h2>任务名称：{{ task.title }}</h2>

    <!-- 任务进度展示 -->
    <div class="task-progress">
      <p>任务进度: {{ taskProgress }}%</p>
      <el-progress :percentage="taskProgress"></el-progress>
      <el-input-number
          v-model="taskProgress"
          :min="0"
          :max="100"
          label="更新进度"
          size="small"
      ></el-input-number>
      <el-button :loading="loading" type="primary" @click="updateProgress">更新任务进度</el-button>
    </div>

    <!-- 任务详情 -->
    <el-divider>任务详情</el-divider>
    <div>
      <p><strong>负责人:</strong> {{ task.publisher }}</p>
      <p><strong>创建时间:</strong> {{ task.startDate }}</p>
      <p><strong>结束时间:</strong> {{ task.endDate }}</p>
      <p>任务描述：{{ task.description }}</p>
    </div>

    <!-- 当前参与用户 -->
    <el-divider>当前参与用户</el-divider>
    <div v-if="currentAssignees.length > 0">
      <ul>
        <li v-for="user in currentAssignees" :key="user.id">
          {{ user.username }} (ID: {{ user.id }})
        </li>
      </ul>
    </div>
    <div v-else>
      <p>暂无参与用户</p>
    </div>

    <!-- 添加参与用户 -->
    <el-divider>添加参与用户</el-divider>
    <el-select v-model="selectedUserId" placeholder="选择用户">
      <el-option
          v-for="user in userList"
          :key="user.id"
          :label="user.username"
          :value="user.id"
      ></el-option>
    </el-select>
    <el-button type="primary" @click="addAssignee">添加用户</el-button>

    <!-- 评论显示区域 -->
    <el-divider>评论</el-divider>
    <div v-if="task.logs && task.logs.length > 0">
      <div v-for="(log, index) in task.logs" :key="index" class="comment-item">
        <p><strong>{{ log.publisher }}:</strong> <span>{{ log.timestamp }}</span></p>
        <p>{{ log.content }}</p>
        <hr/>
      </div>
    </div>

    <!-- 评论输入区 -->
    <el-input
        type="textarea"
        v-model="comment"
        placeholder="写评论，提及人使用 @"
        rows="3"
    ></el-input>

    <el-button type="primary" @click="submitComment">提交评论</el-button>

    <!-- 文档选择 -->
    <el-select v-model="selectedDocumentId" placeholder="请选择文档" :loading="loading">
      <el-option
          v-for="document in documents"
          :key="document.id"
          :label="document.name"
          :value="document.id"
      ></el-option>
    </el-select>
    <!-- 版本号输入 -->
    <el-input
        v-model="selectedVersion"
        placeholder="请输入版本号"
        label="版本号"
    ></el-input>

    <!-- 上传按钮 -->
    <!-- 文件选择 -->
    <!--    <el-upload-->
    <!--        ref="upload"-->
    <!--        class="upload-demo"-->
    <!--        :action="uploadUrl"-->
    <!--        :on-success="handleFileSuccess"-->
    <!--        :on-error="handleFileError"-->
    <!--        :show-file-list="false"-->
    <!--        :auto-upload="false"-->
    <!--        :v-model="uploadData"-->
    <!--        @change="handleFileChange"-->
    <!--    >-->
    <!--      <el-button size="small" type="primary">选择文件</el-button>-->
    <!--    </el-upload>-->
    <input type="file" @change="handleFileChange"/>

    <!-- 上传按钮 -->
    <el-button size="small" type="primary" @click="handleUploadClick">上传文件</el-button>

  </div>
</template>


<script setup>
import {ref, onMounted, inject} from 'vue';
import http from '@/http/request.js';
import {ElMessage} from 'element-plus';
import {useRoute} from "vue-router";

const task = ref({});
const comment = ref('');
const taskProgress = ref(0);
const route = useRoute();
const taskId = route.params.taskId;
const uploadData = ref({});
const file = ref(null);
const selectedVersion = ref('');  // 初始化 selectedVersion 变量，作为响应式数据
const selectedDocumentId = ref();  // 选中的文档ID，初始值为null
const documents = ref([]);  // 存放文档列表
const showError = inject("showError");

let selectedFile;

// 上传文件
const uploadFile = async (documentId, taskId, version, file) => {
  const formData = new FormData();
  formData.append('documentId', documentId);
  formData.append('taskId', taskId);
  formData.append('version', version);
  formData.append('file', file);

  try {
    const response = await http.post('http://localhost:8081/api/comment/add/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    ElMessage.success('文件上传成功');
    return response.data;
  } catch (error) {
    console.error('文件上传失败', error);
    ElMessage.error('文件上传失败');
  }
};

// 获取任务详情
const fetchTaskDetail = async (taskId) => {
  try {
    const response = await http.get(`http://localhost:8081/api/task/getbyid?id=${taskId}`);
    task.value = {
      id: response.data.id,
      title: response.data.title,
      description: response.data.description,
      publisher: response.data.publisher.username,
      assignees: response.data.assigneesId,
      startDate: response.data.startDate,
      endDate: response.data.endDate,
      isCompleted: response.data.isCompleted,
      percentCompleted: response.data.percentCompleted,
      projectId: response.data.projectId, // 获取projectId
      logs: []  // 确保初始化日志数组
    };
    taskProgress.value = task.value.percentCompleted;// 根据任务的projectId加载文档列表
    await loadDocuments(task.value.projectId); // 使用获取到的projectId

  } catch (error) {
    ElMessage.error('获取任务详情失败');
  }
};
// 获取文档列表
const loadDocuments = async (projectId) => {
  if (!projectId) {
    ElMessage.error('无效的项目ID');
    return;
  }

  try {
    const response = await http.post('http://localhost:8081/api/documents/projects', {projectId});
    documents.value = response.data.map(doc => ({
      id: doc.documentId,
      name: doc.name,
    }));
  } catch (error) {
    console.error('获取文档列表失败:', error);
    ElMessage.error('获取文档列表失败');
  }
};
// 进度更新
const updateProgress = async () => {
  try {
    task.value.percentCompleted = taskProgress.value;
    task.value.isCompleted = taskProgress.value === 100;

    const requestData = {
      id: task.value.id,
      title: task.value.title,
      description: task.value.description,
      startDate: task.value.startDate,
      endDate: task.value.endDate,
      isCompleted: task.value.isCompleted,
      percentCompleted: task.value.percentCompleted,
      publisher: {id: task.value.publisher.id},
    };

    const response = await http.post('/api/task/modify', requestData);
    ElMessage.success('任务进度更新成功');
  } catch (error) {
    ElMessage.error('任务进度更新失败');
  }
};

// 提交评论
const submitComment = async () => {
  if (!comment.value.trim()) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  try {
    // 提交评论
    const response = await http.post('http://localhost:8081/api/comment/add/string', {
      stringComment: comment.value,
      belongedTask: {id: task.value.id},
    });

    // 假设接口返回的是包含评论内容和用户信息的数据
    if (response.data && response.data.id) {
      // 从返回的评论对象中提取评论内容和发布者信息
      const newComment = {
        timestamp: new Date().toLocaleString(),  // 本地时间格式化
        content: response.data.stringComment,  // 获取评论内容
        publisher: response.data.publisher.username,  // 获取发布者用户名
      };

      // 更新任务的评论列表
      task.value.logs.push(newComment);
      comment.value = '';  // 清空评论框
      ElMessage.success('评论提交成功');
    } else {
      ElMessage.error('评论提交失败');
    }
  } catch (error) {
    console.error('提交评论时发生错误:', error);
    ElMessage.error('评论提交失败');
  }
};

// 获取评论
const fetchComments = async () => {
  try {
    const response = await http.get(`/api/comment/get?id=${taskId}`);

    if (response.data && Array.isArray(response.data)) {
      task.value.logs = [...response.data.map((comment) => {
        // 如果评论内容为空，则使用 filename 并加上尾巴
        const content = comment.stringComment || `${comment.filename} 已上传`;

        return {
          timestamp: new Date(comment.datetime).toLocaleString(),  // 格式化时间
          content,  // 获取评论内容，若为空则使用文件名和尾巴
          publisher: comment.publisher.username,  // 获取发布者用户名
        };
      })];  // 使用扩展运算符来确保 Vue 侦测到新数组的变化
    } else {
      console.error('评论数据格式不正确');
    }
  } catch (error) {
    console.error('获取评论失败', error);
  }
};


// 监听文件选择变化
const handleFileChange = (event) => {
  selectedFile = event.target.files[0];
};

// 上传文件
const handleUploadClick = async () => {
  console.log('选择的文件:', selectedFile);
  if (!selectedFile) {
    ElMessage.warning('请先选择文件');
    return;
  }

  if (!selectedDocumentId.value) {
    showError("未选择文档");
    return;
  }

  const version = selectedVersion.value || '1.0';

  const formData = new FormData();
  formData.append('file', selectedFile);
  formData.append('version', version);
  formData.append('documentId', Number(selectedDocumentId.value)); // 传递 documentId
  formData.append('taskId', Number(taskId));

  try {
    const response = await http.post('/api/comment/add/file', formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    // 如果上传成功，显示成功消息
    if (response.status === 200) {
      ElMessage.success('文件上传成功');

      // 清空输入框和选中的文件
      selectedFile = null;
      selectedDocumentId.value = null;
      selectedVersion.value = ''; // 如果有版本选择框，清空它
      window.location.reload();  // 刷新页面
    } else {
      ElMessage.error('文件上传失败');
    }
  } catch (error) {
    console.error('上传文件时发生错误:', error);
    ElMessage.error('文件上传失败');
  }
};



const handleFileSuccess = (response, file, fileList) => {
  ElMessage.success('文件上传成功');
};

const handleFileError = (error, file, fileList) => {
  ElMessage.error('文件上传失败');
};

const userList = ref([]); // 可选用户列表
const currentAssignees = ref([]); // 当前参与用户
const selectedUserId = ref(null); // 选择的新用户ID

// 获取用户列表
const fetchUserList = async () => {
  try {
    const response = await http.get('http://localhost:8081/api/user/all');
    userList.value = response.data;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  }
};

// 更新当前参与用户
const fetchCurrentAssignees = async () => {
  try {
    const response = await http.get(`http://localhost:8081/api/task/getbyid?id=${taskId}`);
    const assigneesId = response.data.assigneesId;
    currentAssignees.value = userList.value.filter(user => assigneesId.includes(user.id));
  } catch (error) {
    console.error("获取当前参与用户失败:", error);
    ElMessage.error("获取当前参与用户失败");
  }
};

// 添加参与用户
const addAssignee = async () => {
  if (!selectedUserId.value) {
    ElMessage.warning('请选择一个用户');
    return;
  }

  try {
    const newAssignee = userList.value.find(user => user.id === selectedUserId.value);

    if (!newAssignee) {
      ElMessage.error('无效的用户');
      return;
    }

    // 更新任务参与用户
    // task.value.assignees.push({ id: newAssignee.id });

    const requestData = {
      id: task.value.id,
      title: task.value.title,
      description: task.value.description,
      startDate: task.value.startDate,
      endDate: task.value.endDate,
      belongedProject: { id: task.value.projectId },
      publisher: { id: task.value.publisher.id },
      assignees: [
        {
          "id": newAssignee.id
        }
      ],
      isCompleted: task.value.isCompleted,
      percentCompleted: task.value.percentCompleted
    };

    await http.post('/api/task/modify', requestData);

    // 更新当前参与用户列表
    currentAssignees.value.push(newAssignee);
    ElMessage.success('用户添加成功');
    selectedUserId.value = null; // 清空选择
  } catch (error) {
    ElMessage.error('添加用户失败');
  }
};


// 页面加载时初始化任务信息和评论
onMounted(() => {
  fetchComments(taskId);  // 获取评论
  fetchTaskDetail(taskId);  // 获取任务详情
  fetchUserList();
  fetchCurrentAssignees();
  fetchComments(taskId);  // 获取评论
  fetchCurrentAssignees();
});
</script>


<style scoped>
.task-detail {
  padding: 20px;
}

.task-progress {
  margin-bottom: 20px;
}

.comment-item {
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.comment-item p {
  margin: 5px 0;
}

.assignee-management {
  margin-top: 20px;
}

.assignee-management ul {
  list-style-type: none;
  padding: 0;
}

.assignee-management li {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
