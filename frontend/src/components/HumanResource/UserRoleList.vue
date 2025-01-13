<script setup>
import {ref, onMounted, inject} from "vue";
import http from "@/http/request.js";
import {useRouter} from "vue-router";

let users = ref([]);  // 存储用户数据
let roles = ref([]);  // 存储角色数据
const showMessage = inject("showMessage");
const showError = inject("showError");

const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

const search = ref("");

// 角色名称映射
const roleMapping = {
  "ROLE_USER": "用户",
  "ROLE_PROJECT_MANAGER": "项目经理",
  "ROLE_ADMIN": "管理员"
};


const fetchAllUsers = async () => {
  if (search.value.length === 0) {
    try {
      const response = (await http.get("/api/user/role/all", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
        }
      }));
      users.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } catch (error) {
      showError("获取用户失败" + error.message);
    }
  } else
    try {
      const response = (await http.get("/api/user/role/search", {
        params: {
          username: search.value,
          page: currentPage.value,
          size: pageSize.value,
        }
      }));
      users.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } catch (error) {
      showError("获取用户失败" + error.message);
    }
};

// 获取所有可用角色
const fetchAllRoles = async () => {
  try {
    const response = (await http.get("/api/user/role/available")).data;
    roles.value = response || [];
  } catch (error) {
    showError("获取角色列表失败: " + error.message);
  }
};

// 更新用户角色
const updateUserRole = async (user) => {
  try {
    await http.post("/api/user/role/update", null, {
      params: {
        userId: user.id,
        roleId: user.roleId,
      },
    });
    showMessage("角色更新成功");
    await fetchAllUsers(); // 重新获取用户数据
  } catch (error) {
    showError("角色更新失败: " + (error.response?.data || error.message));
  }
};


//unused
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchAllUsers();
}

const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchAllUsers();
}

const onSearchContentChange = () => {
  fetchAllUsers();
}

onMounted(async () => {
  await fetchAllUsers();
  await fetchAllRoles();
});
</script>

<template>
  <div v-if="Array.isArray(users) && users.length > 0">
    <el-table :data="users" border style="width: 100%">
      <el-table-column prop="username" label="用户名" width="300">
        <template #header>
          <el-input v-model="search" size="small"
                    placeholder="搜索用户"
                    @change="onSearchContentChange"
                    @keyup.enter="onSearchContentChange"
          />
        </template>
      </el-table-column>
      <el-table-column prop="roleName" label="角色" width="200">
        <template #default="scope">
          <el-select
              v-model="scope.row.roleId"
              :placeholder="roleMapping[scope.row.roleName] || '选择角色'"
              size="small"
              @change="() => updateUserRole(scope.row)"
          >
            <el-option
                v-for="role in roles"
                :key="role.id"
                :label="roleMapping[role.name]"
                :value="role.id"
            />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        background
        layout="prev, pager, next, size, ->, total"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 10px; text-align: right;"
    />
  </div>
  <div v-else>
    <span>正在加载用户列表...</span>
  </div>
</template>

<style scoped>
/* 自定义样式 */
</style>
