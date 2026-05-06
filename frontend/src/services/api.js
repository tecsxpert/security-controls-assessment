import axios from "axios";

/**
 * Axios instance with JWT interceptors.
 * SECURITY:
 * - Token from localStorage only — never from URL
 * - Auto logout on 401 — token removed immediately
 * - All errors handled — never exposes internals
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto logout on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

// ── Auth ──────────────────────────────────────────────────────────────────────
export const login    = (data) => api.post("/api/auth/login", data);
export const register = (data) => api.post("/api/auth/register", data);
export const refresh  = (data) => api.post("/api/auth/refresh", data);

// ── Controls ──────────────────────────────────────────────────────────────────
export const getControls = (page = 0, size = 10, sortBy = "createdAt", sortDir = "desc") =>
  api.get(`/api/controls/all?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`);

export const getControl    = (id)      => api.get(`/api/controls/${id}`);
export const createControl = (data)    => api.post("/api/controls/create", data);
export const updateControl = (id, data)=> api.put(`/api/controls/${id}`, data);
export const deleteControl = (id)      => api.delete(`/api/controls/${id}`);
export const searchControls = (q, page = 0) =>
  api.get(`/api/controls/search?q=${encodeURIComponent(q)}&page=${page}`);
export const filterByStatus = (status, page = 0) =>
  api.get(`/api/controls/filter?status=${status}&page=${page}`);
export const getStats = () => api.get("/api/controls/stats");
export const exportCsv = () =>
  api.get("/api/controls/export", { responseType: "blob" });

// ── Files ─────────────────────────────────────────────────────────────────────
export const uploadFile = (formData) =>
  api.post("/api/files/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
export const getFilesByControl = (controlId) =>
  api.get(`/api/files/control/${controlId}`);

export default api;
