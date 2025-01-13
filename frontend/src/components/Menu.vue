<template>
  <el-row class="tac">
    <el-col>
      <el-menu
          class="el-menu-vertical-demo"
          router
          @open="handleOpen"
          @close="handleClose"
      >
        <el-sub-menu v-for="menu in filteredMenu" :key="menu.index" :index="menu.index">
          <template #title>
            <span>{{ menu.title }}</span>
          </template>
          <el-menu-item
              v-for="item in menu.children"
              :key="item.path"
              :index="item.path"
          >
            {{ item.title }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-col>
  </el-row>
</template>

<script setup>
import {ref} from "vue";
import {getPermissions} from "@/scripts/JWTs.js";

const userPermissions = getPermissions();

// 静态菜单配置
const menuConfig = [
  {
    title: "项目管理",
    index: "1",
    children: [
      {title: "项目列表", path: "/project/list", permission: null},
      {title: "创建项目", path: "/project/create", permission: null},
    ],
  },
  {
    title: "任务管理",
    index: "2",
    children: [
      {title: "创建任务", path: "/task/create", permission: "task:create"},
      {title: "被指派任务列表", path: "/task/list", permission: null},
    ],
  },
  {
    title: "文档管理",
    index: "3",
    children: [
      {title: "文档查看", path: "/document/list"},
    ],
  },
  {
    title: "角色管理",
    index: "4",
    children: [
      {
        title: "角色管理",
        path: "/user/role/list",
        permission: "role:modify",
      },
    ],
  },
];

// 过滤菜单
const filterMenu = (menuConfig, permissions) => {
  console.log("权限: " + permissions);
  return menuConfig
      .map((menu) => {
        const filteredChildren = menu.children.filter(
            (item) => !item.permission || permissions.includes(item.permission)
        );
        return filteredChildren.length > 0
            ? {...menu, children: filteredChildren}
            : null;
      })
      .filter((menu) => menu !== null);
};

const filteredMenu = ref(filterMenu(menuConfig, userPermissions));
</script>
