import axios from 'axios';

// Create axios instance with base URL
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token if available
api.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (user && user.authHeader) {
      config.headers.Authorization = user.authHeader;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Add response interceptor to transform JSON:API responses
api.interceptors.response.use(
  (response) => {
    console.log('Response before transformation:', JSON.parse(JSON.stringify(response.data)));

    // Only transform if it's a JSON:API response
    if (response.data && response.data.data && response.data.meta) {
      const jsonApiData = response.data.data;

      // Handle array of items (list endpoints)
      if (Array.isArray(jsonApiData)) {
        // Transform each item in the array to extract attributes
        response.data.data = jsonApiData.map(item => 
          item.attributes ? item.attributes : item
        );
        response.data.pagination = response.data.meta.page;
        console.log('Pagination data extracted:', response.data.pagination);
      } 
      // Handle single item
      else if (jsonApiData.attributes) {
        response.data.data = jsonApiData.attributes;
      }

      // Store original response in _originalData for debugging
      response.data._originalData = { ...response.data };
      console.log('Response after transformation:', JSON.parse(JSON.stringify(response.data)));
    }
    return response;
  },
  (error) => Promise.reject(error)
);

// Product API
export const productApi = {
  getAllProducts: (page = 0, size = 8) => {
    console.log('Calling getAllProducts with page:', page);
    return api.get(`/products?page=${page}&size=${size}`);
  },

  searchProducts: (query, page = 0, size = 20) => {
    console.log('Calling searchProducts with page:', page);
    return api.get(`/products?name=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  },
};

// Order API
export const orderApi = {
  createOrder: (items) => 
    api.post('/orders', items),

  getOrder: (orderId) => 
    api.get(`/orders/${orderId}`),

  cancelOrder: (orderId) => 
    api.delete(`/orders/${orderId}`),

  payOrder: (orderId) => 
    api.post(`/payments/${orderId}`),
};

// Admin API
export const adminApi = {
  // Product management
  getAllProducts: (page = 0, size = 8) => {
    console.log('Calling adminApi.getAllProducts with page:', page);
    return api.get(`/products?page=${page}&size=${size}`);
  },

  createProduct: (products) => 
    api.post('/admin/products', products),

  updateProduct: (productUpdates) => 
    api.patch('/admin/products', productUpdates),

  deleteProduct: (productId) => 
    api.delete(`/admin/products/${productId}`),

  searchProduct: (productId, productName) => {
    let url = '/admin/products/search?';
    if (productId) {
      url += `productId=${productId}`;
    } else if (productName) {
      url += `productName=${encodeURIComponent(productName)}`;
    }
    return api.get(url);
  },

  // Order management
  getAllOrders: (page = 0, size = 25) => {
    console.log('Calling adminApi.getAllOrders with page:', page);
    return api.get(`/admin/orders?page=${page}&size=${size}`);
  },
};

// Auth API
export const authApi = {
  login: (username, password) => {
    const authHeader = 'Basic ' + btoa(username + ':' + password);
    return api.get('/login', {
      headers: {
        'Authorization': authHeader
      },
      withCredentials: true
    });
  },

  logout: () => 
    api.post('/logout'),
};

export default api;
