<script setup>
import {inject, onMounted, ref, watch} from "vue";
import http from "@/http/request.js";
import router from "@/router/index.js";

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
const showError = inject("showError");
const showMessage = inject("showMessage");

// 表单数据
const form = ref({
  project_name: "",
  startDate: "",
  endDate: "",
  description: "",
  priority: "LOW",
  managerID: "",
});

const formRef = ref(null)

const resetForm = () => {
  formRef.value.resetFields();
}

const searchQuery = ref("")
const managers = ref([])

const fetchManagers = async () => {
  try {
    managers.value = (await http.get("/api/user/all")).data;
  } catch (error) {
    console.error("Failed to fetch managers:", error);
  }
};

const searchManagers = (query) => {
  if (!query) {
    fetchManagers();
    return;
  }
  try {
    http.post("/api/user/search",
        {username: query}).then(({data}) => {
      managers.value = data || [];
    });
  } catch (error) {
    showError("Failed to fetch managers:", error);
  }
}

const submitProject = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      const projectData = {
        name: form.value.project_name,
        description: form.value.description,
        startDate: form.value.startDate,
        endDate: form.value.endDate,
        managerId: form.value.managerID,
        priority: form.value.priority,
      }
      try {
        const response = await http.post("/api/project/create", projectData);
        if (response.status === 200) {
          showMessage("项目创建成功，正在跳转到项目列表...")
          await delay(1000);
          await router.push("/project/list")
        }
      } catch (error) {
        const errorMessage = error.response.data;
        showError("项目创建失败: " + errorMessage);
      }
    }
  })
}

// 初始化获取数据
onMounted(() => {
  fetchManagers();
});

const rules = {
  project_name: [
    {required: true, message: "请输入项目名", trigger: "blur"},
    {min: 3, max: 25, message: "长度在 3 到 25 个字符", trigger: "blur"},
  ],
  startDate: [{required: true, message: "请选择开始日期", trigger: "change"}],
  endDate: [{required: true, message: "请选择结束日期", trigger: "change"}],
  description: [
    {required: true, message: "请输入描述", trigger: "blur"},
    {min: 10, message: "描述长度不少于 10 个字符", trigger: "blur"},
  ],
  priority: [{required: true, message: "请选择优先级", trigger: "change"}],
  managerID: [{required: true, message: "请选择负责人", trigger: "change"}],
};
</script>

<template>
  <div class="projects-create">
    <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
    >
      <!-- 项目名 -->
      <el-form-item label="项目名" prop="project_name">
        <el-input v-model="form.project_name" placeholder="请输入项目名"/>
      </el-form-item>

      <!-- 开始日期 -->
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
        />
      </el-form-item>

      <!-- 结束日期 -->
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
        />
      </el-form-item>

      <!-- 描述 -->
      <el-form-item label="描述" prop="description">
        <el-input
            type="textarea"
            v-model="form.description"
            rows="5"
            placeholder="请输入项目描述"
        />
      </el-form-item>

      <!-- 优先级 -->
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级">
          <el-option label="低" value="LOW"/>
          <el-option label="中" value="MEDIUM"/>
          <el-option label="高" value="HIGH"/>
        </el-select>
      </el-form-item>

      <!-- 负责人 -->
      <el-form-item label="负责人" prop="managerID">
        <el-select
            v-model="form.managerID"
            filterable
            remote
            reserve-keyword
            placeholder="请输入负责人名称"
            :remote-method="searchManagers"
        >
          <el-option
              v-for="manager in managers"
              :key="manager.id"
              :label="manager.username"
              :value="manager.id"
          />
        </el-select>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" @click="submitProject">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.input-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-container label {
  width: 70px;
  text-align: right;
  white-space: nowrap;
}

.input-field {
  width: 100%; /* 输入框占据剩余空间 */
  padding: 8px;
  margin-left: 10px; /* 稍微调整输入框与标签之间的距离 */
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-container {
  display: flex;
}
</style>