<template>
  <div>
    <input type="file" @change="onFileChange" />
    <button @click="uploadFile">Upload</button>
    <button @click="downloadFile">Download</button>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      selectedFile: null,
    };
  },
  methods: {
    onFileChange(event) {
      this.selectedFile = event.target.files[0];
    },
    async uploadFile() {
      if (!this.selectedFile) return alert("No file selected");
      const formData = new FormData();
      formData.append("file", this.selectedFile);
      formData.append("version", "1.0");
      formData.append("documentId", 1); // Example document ID

      try {
        const response = await http.post("/api/documents/upload", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        console.log("Upload success", response.data);
      } catch (error) {
        console.error("Upload error", error);
      }
    },
    async downloadFile() {
      try {
        const response = await http.get("/api/documents/download/1", {
          responseType: "blob",
        });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", "file");
        document.body.appendChild(link);
        link.click();
      } catch (error) {
        console.error("Download error", error);
      }
    },
  },
};
</script>
