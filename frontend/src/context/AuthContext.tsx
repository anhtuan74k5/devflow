import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import client from '../api/client';
import type { User, LoginRequest, RegisterRequest, AuthResponseData } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function parseUserFromResponse(responseData: AuthResponseData): { token: string; refreshToken: string; user: User } {
  return {
    token: responseData.token || responseData.accessToken!,
    refreshToken: responseData.refreshToken,
    user: {
      id: responseData.id ?? 0,
      username: responseData.username,
      role: responseData.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER',
    },
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore session from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      try {
        const parsed = JSON.parse(storedUser);
        // Ensure backward compat: if stored user doesn't have role, default to 'USER'
        if (!parsed.role) {
          parsed.role = 'USER';
        }
        setUser(parsed);
      } catch {
        localStorage.removeItem('user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    const response = await client.post('/auth/login', data);
    const parsed = parseUserFromResponse(response.data.data);

    localStorage.setItem('token', parsed.token);
    localStorage.setItem('refreshToken', parsed.refreshToken);
    localStorage.setItem('user', JSON.stringify(parsed.user));

    setToken(parsed.token);
    setUser(parsed.user);
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    const response = await client.post('/auth/register', data);
    const parsed = parseUserFromResponse(response.data.data);

    localStorage.setItem('token', parsed.token);
    localStorage.setItem('refreshToken', parsed.refreshToken);
    localStorage.setItem('user', JSON.stringify(parsed.user));

    setToken(parsed.token);
    setUser(parsed.user);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
