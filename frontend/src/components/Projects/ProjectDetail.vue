<template>
  <div>
    <!-- 项目详情 -->
    <h1>项目详情</h1>
    <div v-if="!isEditing" class="project-info">
      <p><strong>项目名称：</strong>{{ project.projectName }}</p>
      <p><strong>描述：</strong>{{ project.description }}</p>
      <p><strong>开始日期：</strong>{{ project.startDate }}</p>
      <p><strong>结束日期：</strong>{{ project.endDate }}</p>
      <p><strong>优先级：</strong>{{ project.priority }}</p>
      <p><strong>完成状态：</strong>{{ project.isCompleted ? "已完成" : "未完成" }}</p>
      <p><strong>进度：</strong>{{ project.progress }}%</p>
      <p><strong>负责人:</strong>{{ project.manager?.username }}</p>
      <button @click="isEditing = true">编辑信息</button>
    </div>
    <div v-else>
      <label>
        项目名称：
        <textarea v-model="project.projectName"/>
      </label>
      <label>
        描述：
        <textarea v-model="project.description"></textarea>
      </label>
      <label>
        开始日期：
        <input type="date" v-model="project.startDate"/>
      </label>
      <label>
        结束日期：
        <input type="date" v-model="project.endDate"/>
      </label>
      <label>
        优先级：
        <select v-model="project.priority">
          <option value="HIGH">HIGH</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="LOW">LOW</option>
        </select>
      </label>
      <label>
        进度：
        <input type="number" v-model="project.progress" step="0.1" min="0" max="100"/>
      </label>
      <label>
        完成状态：
        <select v-model="project.isCompleted">
          <option :value="true">已完成</option>
          <option :value="false">未完成</option>
        </select>
      </label>
      <button @click="saveProject">保存</button>
      <button @click="isEditing = false">取消</button>
    </div>

    <!-- 显示已有参与者 -->
    <div>
      <h2>已有参与者</h2>
      <ul>
        <li v-for="participant in participants" :key="participant.id">
          {{ participant.username }}
        </li>
      </ul>
    </div>

    <!-- 添加参与者 -->
    <div>
      <h2>添加参与者</h2>
      <el-form>
        <el-form-item label="搜索可添加用户或直接选择">
          <el-input
              v-model="searchQuery"
              placeholder="搜索用户..."
              @input="searchCandidates"
          />
        </el-form-item>
        <el-form-item label="选择用户">
          <el-select
              v-model="selectedCandidates"
              multiple
              placeholder="请选择需要添加的用户"
              style="width: 100%;"
          >
            <el-option
                v-for="candidate in candidates"
                :key="candidate.id"
                :label="candidate.username"
                :value="candidate.id"
            />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="addParticipants">添加</el-button>
      </el-form>
    </div>
    <!-- 跳转到项目甘特图页面 -->
    <button @click="goToGantt">查看甘特图</button>
  </div>
  <TaskOfProjectList :project-id="project.id" />
</template>

<script setup>
import {ref, onMounted, inject} from "vue";
import axios from "axios";
import {useRoute} from "vue-router";
import http from "@/http/request.js";
import TaskOfProjectList from "@/components/Task/TaskOfProjectList.vue";
import router from "@/router/index.js";

const showError = inject("showError");
const showMessage = inject("showMessage");

const route = useRoute();
const projectId = route.params.id;

const project = ref({});
const participants = ref({});
const candidates = ref([]);         // 候选用户列表
const searchQuery = ref("");        // 搜索关键词
const selectedCandidates = ref([]);   // 已选择的用户 ID 列表
const isEditing = ref(false);

// 获取项目详情
const fetchProjectDetails = async () => {
  try {
    const {data} = await http.post("/api/project/get", {
      id: projectId,
    });
    project.value = data;
  } catch (error) {
    console.error("获取项目详情失败:", error);
  }
  try {
    const projectIDJson = {
      id: projectId,
    }
    const {data} = await http.post("/api/user/allbyproject", projectIDJson);
    participants.value = data;
  } catch (error) {
    showError(error);
  }
};

// 获取可添加的候选用户
const fetchCandidates = async () => {
  try {
    candidates.value = (await http.get("/api/user/all")).data;
  } catch (error) {
    console.error("获取候选用户失败:", error);
  }
};

const searchCandidates = async () => {
  if (!searchQuery.value) {
    await fetchCandidates();
    return;
  }
  try {
    candidates.value = (await http.post("/api/user/search",
        {username: searchQuery.value}
    )).data;
  } catch (error) {
    showError(error);
  }
}

// 添加参与者
const addParticipants = async () => {
  if (selectedCandidates.value.length === 0) {
    alert("请选择需要添加的用户！");
    return;
  }

  try {
    await http.post("http://localhost:8081/api/project/add-participant", {
      projectId: projectId,
      userIds: selectedCandidates.value, // 发送选中的用户 ID 列表
    });
    selectedCandidates.value = []; // 清空已选择的用户
    await fetchProjectDetails(); // 重新加载项目详情
  } catch (error) {
    showError(error);
    console.error("添加参与者失败:", error);
    alert("添加失败，请稍后再试！");
  }
};

const saveProject = async () => {
  try {
    // 构建需要发送给后端的项目数据
    const updatedProject = {
      id: projectId, // 当前项目的 ID
      projectName: project.value.projectName,
      description: project.value.description,
      startDate: project.value.startDate,
      endDate: project.value.endDate,
      priority: project.value.priority,
      isCompleted: project.value.isCompleted,
      progress: project.value.progress,
    };
    const response = await http.post("/api/project/update", updatedProject);
    // 如果请求成功
    if (response.status === 200) {
      showMessage("项目更新成功");
      isEditing.value = false;
      await fetchProjectDetails(); // 重新获取项目详情
    } else {
      alert("项目更新失败，请稍后再试！");
    }
  } catch (error) {
    console.error("保存项目时出错:", error);
    alert("保存失败，请稍后再试！");
  }
};

// 跳转到项目甘特图页面
const goToGantt = () => {
  router.push({ name: "ProjectGantt", params: { id: projectId } });
};

onMounted(() => {
  fetchProjectDetails();
  fetchCandidates();
});
</script>

<style scoped>

.project-info p{
  font-size: 14px;
  margin: 10px 0;
  line-height: 1.5;
}

input[type="text"],
input[type="date"],
input[type="number"],
textarea,
select {
  width: 100%;
  padding: 10px;
  margin: 5px 0 20px 0;
  display: block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  resize: vertical;
}

</style>