import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      // Set up basic auth headers
      const authHeader = 'Basic ' + btoa(username + ':' + password);
      
      // Make a request to check if credentials are valid
      const response = await axios.get('/api/login', {
        headers: {
          'Authorization': authHeader
        },
        withCredentials: true
      });
      
      // If successful, store user info
      const userData = {
        username,
        isAdmin: username === 'admin', // Assuming admin role based on username
        authHeader
      };
      
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      // Set default auth header for future requests
      axios.defaults.headers.common['Authorization'] = authHeader;
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Login failed. Please check your credentials.'
      };
    }
  };

  const logout = () => {
    // Remove user from local storage
    localStorage.removeItem('user');
    setUser(null);
    
    // Remove auth header
    delete axios.defaults.headers.common['Authorization'];
    
    // Call logout endpoint
    axios.post('/api/logout')
      .catch(error => console.error('Logout error:', error));
  };

  const value = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
    isAdmin: user?.isAdmin || false
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};