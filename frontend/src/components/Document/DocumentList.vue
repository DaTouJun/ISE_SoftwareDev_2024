<template>
  <!-- 选择项目下拉菜单 -->
  <div v-if="projects.length">
    <h2>选择项目</h2>
    <el-select v-model="selectedProjectId" placeholder="请选择项目" @change="fetchDocuments">
      <el-option
          v-for="project in projects"
          :key="project.id"
          :label="project.projectName"
          :value="project.id">
      </el-option>
    </el-select>
  </div>

  <!-- 显示文档列表 -->
  <h2>文档列表</h2>
  <!-- 下拉菜单展示文档 -->
  <select v-model="selectedDocumentId" @change="viewDocument(selectedDocumentId)">
    <option value="" disabled>请选择文档</option>
    <option v-for="document in documents" :key="document.id" :value="document.id">
      {{ document.name }}
    </option>
  </select>

  <!-- 显示文档版本 -->
  <div v-if="documentVersions.length">
    <h3>文档版本</h3>
    <el-row :gutter="20">
      <el-col :span="8" v-for="version in documentVersions" :key="version.id">
        <el-card :body-style="{ padding: '20px' }">
          <div style="font-weight: bold;">{{ version.version }} - {{ version.name }}</div>
          <div>
            <el-icon :name="getFileIcon(version.name)"></el-icon> <!-- 根据文件名显示图标 -->
          </div>
          <div style="margin-top: 10px;">
            <button @click="downloadDocument(version)" class="download-btn">下载</button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 加载中提示 -->
  <div v-if="loading">加载中...</div>
</template>

<script>
import {ElSelect, ElOption, ElCard, ElRow, ElCol, ElIcon} from 'element-plus';
import http from "@/http/request.js"; // 引入Element Plus组件

export default {
  data() {
    return {
      projects: [], // 存储项目列表
      selectedProjectId: null, // 选中的项目ID
      documents: [], // 存储文档列表
      selectedDocumentId: null, // 选中的文档ID
      documentVersions: [], // 存储文档版本列表
      documentContent: null, // 存储单个文档的详细内容
      loading: false, // 加载状态
    };
  },
  methods: {
    // 获取当前用户参与的项目
    fetchProjects() {
      this.loading = true;
      http
          .get('http://localhost:8081/api/project/list/participated')
          .then(response => {
            this.projects = response.data.map(project => ({
              id: project.id,
              projectName: project.projectName, // 提取项目名称
            }));
          })
          .catch(error => {
            console.error('获取项目列表失败:', error);
          })
          .finally(() => {
            this.loading = false;
          });
    },

    // 获取指定项目的文档列表
    fetchDocuments() {
      if (!this.selectedProjectId) return; // 如果没有选择项目，直接返回

      this.loading = true;
      http.post('http://localhost:8081/api/documents/projects', {
        projectId: this.selectedProjectId // 传递 projectId 作为请求体参数
      })
          .then(response => {
            this.documents = response.data.map(doc => ({
              id: doc.documentId,
              name: doc.name,
            }));
          })
          .catch(error => {
            console.error('获取文档失败:', error);
          })
          .finally(() => {
            this.loading = false;
          });
    },

    // 获取选中文档的所有版本
    viewDocument(documentId) {
      if (!documentId) return;
      this.loading = true;
      http.post('http://localhost:8081/api/documents/projects/versions', {
        documentId: documentId,
      })
          .then(response => {
            this.documentVersions = response.data; // 获取文档版本信息
          })
          .catch(error => {
            console.error('获取文档版本失败:', error);
          })
          .finally(() => {
            this.loading = false;
          });
    },

    downloadDocument(version) {
      this.loading = true;
      http.post('/api/documents/projects/versions/download', {
        documentId: version.documentId,
        version: version.version
      }, {
        headers: {
          'Content-Type': 'application/json', // 确保请求头是正确的
        },
        responseType: 'arraybuffer' // 使用 arraybuffer 类型，适合下载二进制文件
      })
          .then(response => {
            console.log(response.data);

            // 获取文件名和扩展名
            let filename = version.name || 'default_file';

            // 确保文件名包含扩展名
            if (!filename.includes('.')) {
              filename += '.docx'; // 默认扩展名为 .docx
            }

            // 将 arraybuffer 转换为 Blob 对象
            const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });

            // 创建下载链接
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename); // 确保下载文件名正确
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
          })
          .catch(error => {
            console.error('下载文档失败:', error);
          })
          .finally(() => {
            this.loading = false;
          });
    },





    // 根据文件名获取文件图标
    getFileIcon(filename) {
      if (filename.endsWith('.pdf')) {
        return 'el-icon-file-pdf';
      } else if (filename.endsWith('.docx') || filename.endsWith('.doc')) {
        return 'el-icon-file-word';
      } else if (filename.endsWith('.xlsx') || filename.endsWith('.xls')) {
        return 'el-icon-file-excel';
      } else if (filename.endsWith('.txt')) {
        return 'el-icon-file';
      }
      return 'el-icon-document'; // 默认图标
    }
  },

  mounted() {
    // 页面加载时获取用户参与的项目列表
    this.fetchProjects();
  },
};
</script>

<style scoped>
/* 页面样式 */
h1 {
  text-align: center;
}

ul {
  list-style-type: none;
}

li {
  margin: 10px 0;
}

button {
  margin-left: 10px;
}

/* 自定义下载按钮 */
.download-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.download-btn:hover {
  background-color: #66b1ff;
}

.el-card {
  margin-bottom: 20px;
  background-color: #f9f9f9;
}

.el-card .el-card__header {
  font-weight: bold;
  font-size: 16px;
  padding: 10px;
}

.el-card .el-card__body {
  font-size: 14px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.el-card .el-card__body .el-icon {
  font-size: 24px;
  margin-bottom: 10px;
}
</style>
