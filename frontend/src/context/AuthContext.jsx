import { createContext, useContext, useState } from "react";

/**
 * AuthContext — manages JWT token and user state globally.
 * SECURITY: token stored in localStorage with auto-logout on 401.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [user,  setUser]  = useState(
    JSON.parse(localStorage.getItem("user") || "null")
  );

  const login = (tokenValue, userInfo) => {
    localStorage.setItem("token", tokenValue);
    localStorage.setItem("user", JSON.stringify(userInfo));
    setToken(tokenValue);
    setUser(userInfo);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider value={{ token, user, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
