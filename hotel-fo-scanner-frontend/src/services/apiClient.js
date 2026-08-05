import axios from 'axios';
import useAuthStore from '../store/useAuthStore';

// Sesuaikan URL dasar sesuai dengan port Spring Boot
const API_URL = 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Sisipkan Access Token
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Tangani 401 dan otomatis Refresh Token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Jika 401 dan belum pernah dicoba ulang (menghindari infinite loop)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        // Panggil endpoint refresh token
        const response = await axios.post(`${API_URL}/auth/refresh`, {
          refreshToken: refreshToken,
        });

        const { accessToken: newAccess, refreshToken: newRefresh } = response.data;
        
        // Simpan token baru ke global state
        useAuthStore.getState().setTokens(newAccess, newRefresh);

        // Ulangi request asli dengan token baru
        originalRequest.headers.Authorization = `Bearer ${newAccess}`;
        return apiClient(originalRequest);
        
      } catch (refreshError) {
        // Jika refresh gagal (misal token kadaluarsa), logout paksa
        useAuthStore.getState().logout();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
