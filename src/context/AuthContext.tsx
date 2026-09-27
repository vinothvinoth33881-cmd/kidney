import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, fullName?: string) => void;
  register: (email: string, fullName: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const DEFAULT_USER: User = {
  fullName: 'Dr. Alexander Wright, MD',
  email: 'a.wright@radiology.med.org',
  institution: 'Biomedical Imaging & ML Research Laboratory',
  role: 'Senior Clinical Research Radiologist'
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('kidney_ai_user');
    return saved ? JSON.parse(saved) : DEFAULT_USER;
  });

  const login = (email: string, fullName?: string) => {
    const newUser: User = {
      fullName: fullName || (email.includes('wright') ? 'Dr. Alexander Wright, MD' : 'Dr. Clinical Radiologist'),
      email,
      institution: 'Biomedical Imaging & ML Research Laboratory',
      role: 'Staff Radiologist'
    };
    setUser(newUser);
    localStorage.setItem('kidney_ai_user', JSON.stringify(newUser));
  };

  const register = (email: string, fullName: string) => {
    login(email, fullName);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('kidney_ai_user');
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
