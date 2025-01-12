import axios from "axios"
import {getAccessToken, refreshAccessToken, clearTokens} from "@/scripts/JWTs.js";
import {inject} from "vue";
import {eventBus} from "@/scripts/eventBus.js";

const showError = inject("showError");

const http = axios.create({
    baseURL: "http://localhost:8081/",
    withCredentials: true,
    timeout: 5000
})

http.interceptors.request.use(
    async (config) => {
        let token = getAccessToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

http.interceptors.response.use(
    (response) => {
        console.log("响应数据", response);
        return response
    },
    async (error) => {
        const originalRequest = error.config;

        if (!originalRequest) {
            console.error("original request not defined:", error);
            return Promise.reject(error);
        }


        if (error.response) {
            const status = error.response.status;
            if (status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                try {
                    const newToken = await refreshAccessToken();
                    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
                    return http(originalRequest);
                } catch (error) {
                    clearTokens();
                    eventBus.emit("error", "登录过期了");
                    window.location.href = '/login';
                }
            } else if (status === 403) {
                showError("访问失败，你没有访问这个资源的权限");
            } else if (status === 401) {
                eventBus.emit("error", "没有认证，请登录");
                clearTokens();
                window.location.href = '/login';
            }
        } else {
            console.error(error)
        }
        throw(error);
    }
)


export default http;