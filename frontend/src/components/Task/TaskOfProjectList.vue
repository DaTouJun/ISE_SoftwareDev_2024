<template>
  <!-- 显示任务列表 -->
  <el-table :data="tasks" style="width: 100%">
    <el-table-column prop="title" label="任务名称" width="180"></el-table-column>
    <el-table-column prop="description" label="任务描述" width="300"></el-table-column>
    <el-table-column prop="startDate" label="开始时间" width="150"></el-table-column>
    <el-table-column prop="endDate" label="结束时间" width="150"></el-table-column>

    <!-- 使用 v-slot 来替代 slot-scope -->
    <el-table-column label="操作">
      <template v-slot="scope">
        <el-button @click="goToTaskDetail(scope.row.id)" size="small">查看</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "@/http/request.js";

// 定义传入的 props
const props = defineProps({
  projectId: {
    type: Number,
    required: true, // 确保父组件必须传递项目 ID
  },
});

const tasks = ref([]); // 存储任务列表
const router = useRouter();

// 获取任务列表
const fetchTasks = async () => {
  if (!props.projectId) return; // 如果没有 projectId，则不进行请求

  try {
    const response = await http.get(`http://localhost:8081/api/task/get`, {
      params: { id: props.projectId },
    });
    tasks.value = response.data; // 将获取的数据赋值给 tasks
  } catch (error) {
    console.error("获取任务列表失败", error);
  }
};

// 跳转到任务详情页
const goToTaskDetail = (taskId) => {
  router.push({path: `/task/detail/${taskId}`});
};

// 当 projectId 变化时重新加载任务
watch(() => props.projectId, fetchTasks);

// 页面加载时获取任务列表
onMounted(fetchTasks);
</script>

<style scoped>
</style>
