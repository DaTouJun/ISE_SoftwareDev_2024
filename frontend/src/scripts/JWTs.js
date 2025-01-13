import {eventBus} from "@/scripts/eventBus.js";
import {jwtDecode} from "jwt-decode";
import axios from "axios";


export const saveTokens = (tokens) => {
    localStorage.setItem('access_token', tokens.access_token);
    localStorage.setItem('refresh_token', tokens.refresh_token);
};

const saveAccessToken = (accessToken) => {
    localStorage.setItem('access_token', accessToken);
}

export const getAccessToken = () => {
    return localStorage.getItem('access_token');
};

export const getRefreshToken = () => {
    return localStorage.getItem('refresh_token');
};

export const clearTokens = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
};

export const refreshAccessToken = async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        throw new Error('No refresh token available');
    }
    try {
        const response =
            await axios.post('/api/auth/refresh',
                {refresh_token: refreshToken});
        saveAccessToken(response.data);
    } catch (error) {
        if (error.response && error.response.status === 401) {
            clearTokens();
            window.location.href = '/login';
            eventBus.emit("error", "会话超时，请重新登录")
        } else
            throw new Error('Failed to refresh access token');
    }
}

export const getPermissions = () => {
    try {
        const decoded = jwtDecode(localStorage.getItem('access_token'));
        return decoded.permissions || [];
    } catch (error) {
        console.error("解析 JWT 失败:", error);
        return [];
    }
};