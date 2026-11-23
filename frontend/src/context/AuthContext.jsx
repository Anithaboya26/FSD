import { createContext, useContext, useEffect, useMemo, useState } from "react";
import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
} from "../api/client.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let active = true;
    getCurrentUser()
      .then((currentUser) => {
        if (active) {
          setUser(currentUser);
          setStatus("ready");
        }
      })
      .catch((error) => {
        if (!active) return;
        if (error.response?.status === 401) {
          setUser(null);
          setStatus("ready");
        } else {
          setStatus("error");
        }
      });

    return () => {
      active = false;
    };
  }, []);

  async function signIn(credentials) {
    const currentUser = await loginRequest(credentials);
    setUser(currentUser);
    setStatus("ready");
    return currentUser;
  }

  async function signUp(details) {
    const currentUser = await registerRequest(details);
    setUser(currentUser);
    setStatus("ready");
    return currentUser;
  }

  async function signOut() {
    await logoutRequest();
    setUser(null);
  }

  const value = useMemo(() => ({
    user,
    status,
    signIn,
    signUp,
    signOut,
  }), [user, status]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
