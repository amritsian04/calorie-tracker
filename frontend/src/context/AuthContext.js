import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiFetch } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // Re-hydrate session on page load
  useEffect(() => {
    apiFetch('/api/auth/me')
      .then(res => (res.ok ? res.json() : null))
      .then(data => { setUser(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  async function login(email, password) {
    const res = await apiFetch('/api/auth/login', {
      method: 'POST',
      body: { email, password },
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Login failed');
    setUser(data);
    return data;
  }

  async function register(firstName, lastName, email, password) {
    const res = await apiFetch('/api/auth/register', {
      method: 'POST',
      body: { firstName, lastName, email, password },
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Registration failed');
    setUser(data);
    return data;
  }

  async function logout() {
    await apiFetch('/api/auth/logout', { method: 'POST' });
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
