<script setup>
import { ref, onMounted } from 'vue';
import http from '@/http/request.js';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();

const taskTitle = ref('');
const taskDescription = ref('');
const taskPriority = ref('');
const taskStartDate = ref('');
const taskEndDate = ref('');
const assignedMembers = ref([]); // 存储选中的任务分配成员
const selectedProjectId = ref(''); // 存储选中的项目ID
const users = ref([]); // 存储项目成员
// 存储项目列表
const projects = ref([]);

const fetchProjects = async () => {
  try {
    const response = await http.get('http://localhost:8081/api/project/list/participated');
    console.log(response.data); // 打印数据
    projects.value = response.data.map(project => ({
      id: project.id,
      projectName: project.projectName,
    }));
  } catch (error) {
    alert('获取项目列表失败');
    console.error(error); // 打印错误信息
  }
};


// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await http.get('http://localhost:8081/api/user/all');
    // 映射数据，确保有成员的 id 和 username 属性
    users.value = response.data.map(user => ({
      id: user.id,
      username: user.username  // 映射为 name
    }));

  } catch (error) {
    ElMessage.error('获取成员列表失败');
  }
};

const searchUsers = (query) => {
  if (!query) {
    fetchUsers();
    return;
  }
  try {
    http.post("/api/user/search",
        {username: query}).then(({data}) => {
      users.value = data || [];
    });
  } catch (error) {
    showError("Failed to fetch managers:", error);
  }
}

// 创建任务
const createTask = async () => {
  if (!taskTitle.value || !taskDescription.value  || !taskStartDate.value || !taskEndDate.value || !selectedProjectId.value) {
    alert('请填写完整任务信息');
    return;
  }

  // 构建请求数据
  const requestData = {
    title: taskTitle.value,
    description: taskDescription.value,
    startDate: taskStartDate.value,
    endDate: taskEndDate.value,
    belongedProject: {
      id: selectedProjectId.value,  // 获取选中的项目ID
    },
    assignees: assignedMembers.value.map(member => ({
      id: member,  // 只传递成员的 id
    })),
  };
  try {
    await http.post('http://localhost:8081/api/task/allocation', requestData);
    ElMessage.success('任务创建成功');
    await router.push('/task/list'); // 跳转到任务列表页
  } catch (error) {
    ElMessage.error('创建任务失败');
  }
};


onMounted(() => {
  fetchUsers();  // 页面加载时获取成员列表
  fetchProjects();  // 加载项目列表
});
</script>

<template>
  <el-form label-width="80px">
    <!-- 任务名称 -->
    <el-form-item label="任务名称">
      <el-input v-model="taskTitle" placeholder="请输入任务名称"></el-input>
    </el-form-item>

    <!-- 任务描述 -->
    <el-form-item label="任务描述">
      <el-input v-model="taskDescription" placeholder="请输入任务描述" type="textarea"></el-input>
    </el-form-item>

    <!-- 开始时间 -->
    <el-form-item label="开始时间">
      <el-date-picker v-model="taskStartDate" type="date" placeholder="选择开始时间"></el-date-picker>
    </el-form-item>

    <!-- 结束时间 -->
    <el-form-item label="结束时间">
      <el-date-picker v-model="taskEndDate" type="date" placeholder="选择结束时间"></el-date-picker>
    </el-form-item>

    <!-- 选择项目 -->
    <el-form-item label="选择项目">
      <el-select v-model="selectedProjectId" placeholder="选择你所属的项目">
        <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.projectName"
            :value="project.id"
        />
      </el-select>
    </el-form-item>

    <!-- 选择任务分配成员 -->
    <el-form-item label="选择成员">
      <el-select
          v-model="assignedMembers"
          multiple
          placeholder="选择任务分配成员"
      >
        <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.username"
            :value="user.id"
        />
      </el-select>
    </el-form-item>

    <!-- 创建任务按钮 -->
    <el-button type="primary" @click="createTask">创建任务</el-button>
  </el-form>
</template>

<style scoped>
.el-form-item button {
  margin-top: 20px; /* 增加按钮上方的间距 */
}
</style>
