import {eventBus} from "@/scripts/eventBus.js";
import axios from "axios";


export const saveTokens = (tokens) => {
    localStorage.setItem('access_token', tokens.access_token);
    localStorage.setItem('refresh_token', tokens.refresh_token);
};

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
        saveTokens(response.data);
    } catch (error) {
        if (error.response && error.response.status === 401) {
            clearTokens();
            window.location.href = '/login';
            eventBus.emit("error", "会话超时，请重新登录")
        } else
            throw new Error('Failed to refresh access token');
    }
}