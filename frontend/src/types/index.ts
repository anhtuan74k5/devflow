export interface User {
  id: number;
  username: string;
  role: 'ADMIN' | 'USER';
}

export interface AuthResponseData {
  id: number;
  token: string;
  accessToken?: string;
  refreshToken: string;
  username: string;
  role: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: AuthResponseData;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  ownerUsername: string;
}

export interface CreateProjectRequest {
  name: string;
  description: string;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  projectId: number;
  assigneeId: number | null;
  assigneeUsername: string | null;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  assigneeId?: number;
}

export interface UpdateTaskStatusRequest {
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
}

export interface ActivityLog {
  id: number;
  content: string;
  createdAt: string;
  projectId: number;
}
