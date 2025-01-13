<template>
  <div v-if="!$route.meta.nonLayout" class="common-layout">
    <el-header class="main-header">企业管理系统
    </el-header>
    <el-container class="container">
      <el-aside class="aside">
        <Menu/>
        <LazyMessageView/>
      </el-aside>
      <el-container class="container">
        <el-header class="header">{{ currentHeader }}</el-header>
        <div class="header-message" v-if="headerMessage.visible">
          <el-alert
              :title="headerMessage.text"
              :type="headerMessage.type"
              show-icon
              closable
              @close="clearMessage"
          />
        </div>
        <el-main class="main">
          <router-view/>
        </el-main>
      </el-container>
    </el-container>
  </div>
  <div v-else class="center-pos">
    <router-view/>
  </div>
</template>

<script setup>

import {useRoute} from "vue-router";
import {provide, computed, reactive, onMounted, defineAsyncComponent} from "vue";
import {eventBus} from "@/scripts/eventBus.js";

const LazyMessageView = defineAsyncComponent(() =>
    import("@/components/Message/Message.vue")
);

const route = useRoute()
const currentHeader = computed(() => route.meta.header || "默认标题");
// 消息状态管理
const headerMessage = reactive({
  visible: false, // 是否显示消息
  text: "",       // 消息内容
  type: "success"
});

// 显示消息
const showMessage = (text) => {
  headerMessage.text = text;
  headerMessage.visible = true;
  headerMessage.type = "success";

  // 自动隐藏消息
  setTimeout(() => {
    headerMessage.visible = false;
  }, 3000);
};

// 显示错误
const showError = (text) => {
  headerMessage.text = text;
  headerMessage.visible = true;
  headerMessage.type = "error";

  // 自动隐藏消息
  setTimeout(() => {
    headerMessage.visible = false;
  }, 5000);
};

provide("showMessage", showMessage);
provide("showError", showError);

// 清除消息
const clearMessage = () => {
  headerMessage.visible = false;
};

onMounted(() => {
  eventBus.on("error", (message) => {
    showError(message);
  });
})
</script>

<style scoped>
header {
  line-height: 1.5;
}


@media (min-width: 300px) {
  .aside {
    background: #ffffff;
    width: 230px;
  }

  .common-layout {
    height: 100vh;
    width: 100%;
  }

  .container {
    display: flex;
    height: 100%;
    width: 100%;
  }

  .header {
    background: #a4fdfd;
    height: 70px;
    line-height: 70px;
    text-align: center;
  }

  .main-header {
    background: #aaf5ff;
    height: 70px;
    line-height: 70px;
  }

  .center-pos {
    display: grid;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
  }

  .header-message {
    text-align: center;
  }
}

</style>
