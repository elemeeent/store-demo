import React, { useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box, CssBaseline } from '@mui/material';
import { useAuth } from './contexts/AuthContext';

// Layout components
import Header from './components/Header';
import Sidebar from './components/Sidebar';

// Pages
import ProductsPage from './pages/ProductsPage';
import OrdersPage from './pages/OrdersPage';
import AdminProductsPage from './pages/admin/AdminProductsPage';
import AdminOrdersPage from './pages/admin/AdminOrdersPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';

// Protected route component
const ProtectedRoute = ({ children, requireAdmin }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/products" />;
  }

  return children;
};

const App = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const { isAuthenticated, isAdmin } = useAuth();

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <CssBaseline />

      <Header searchQuery={searchQuery} setSearchQuery={setSearchQuery} />

      <Box sx={{ display: 'flex', flexGrow: 1 }}>
        <Sidebar isAdmin={isAdmin} />

        <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />

            {/* User routes */}
            <Route path="/" element={<Navigate to="/products" />} />
            <Route path="/products" element={<ProductsPage searchQuery={searchQuery} />} />
            <Route path="/orders" element={<OrdersPage searchQuery={searchQuery} />} />

            {/* Admin routes */}
            <Route 
              path="/admin/products" 
              element={
                <ProtectedRoute requireAdmin>
                  <AdminProductsPage searchQuery={searchQuery} />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/orders" 
              element={
                <ProtectedRoute requireAdmin>
                  <AdminOrdersPage searchQuery={searchQuery} />
                </ProtectedRoute>
              } 
            />

            {/* 404 route */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Box>
      </Box>
    </Box>
  );
};

export default App;
