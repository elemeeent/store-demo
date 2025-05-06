import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Grid, 
  Card, 
  CardContent, 
  CardActions, 
  Typography, 
  Button, 
  TextField, 
  InputAdornment,
  IconButton,
  Pagination,
  CircularProgress,
  Alert,
  Snackbar
} from '@mui/material';
import { 
  Add as AddIcon, 
  Remove as RemoveIcon,
  AddShoppingCart as AddShoppingCartIcon
} from '@mui/icons-material';
import { productApi } from '../services/api';
import { useCart } from '../contexts/CartContext';
import { useSearchParams, useNavigate } from 'react-router-dom';

const ProductsPage = ({ searchQuery }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const pageParam = parseInt(searchParams.get('page')) || 0;
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [quantities, setQuantities] = useState({});
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'success' });

  const { addToCart } = useCart();

  // Load products when the component mounts or when search query changes
  useEffect(() => {
    console.log('useEffect running, page:', page, 'searchQuery:', searchQuery);
    fetchProducts();
  }, [page, searchQuery]);

  useEffect(() => {
    const currentPage = parseInt(searchParams.get('page')) || 0;
    setPage(currentPage);
  }, [searchParams]);

  const fetchProducts = async () => {
    setLoading(true);
    setError('');

    try {
      let response;

      if (searchQuery) {
        response = await productApi.searchProducts(searchQuery, page);
      } else {
        response = await productApi.getAllProducts(page);
      }

      console.log('Full Response:', response);
      console.log('API Response:', response.data);
      console.log('Pagination Info:', response.data.pagination);

      const productData = response.data.data;
      setProducts(productData);

      // Initialize quantities for each product
      const initialQuantities = {};
      productData.forEach(product => {
        initialQuantities[product.id] = 1;
      });
      setQuantities(initialQuantities);

      // Get pagination info from response
      if (response.data.pagination) {
        const { total, maxSize } = response.data.pagination;
        console.log('Total:', total, 'MaxSize:', maxSize);
        const calculatedTotalPages = Math.ceil(total / maxSize) || 1;
        console.log('Calculated totalPages:', calculatedTotalPages);
        setTotalPages(calculatedTotalPages);
      } else {
        console.log('No pagination info in response');
        setTotalPages(1);
      }

    } catch (err) {
      console.error('Error fetching products:', err);
      setError('Failed to load products. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (productId, value) => {
    setQuantities(prev => ({
      ...prev,
      [productId]: Math.max(1, Math.min(value, getProductById(productId)?.stockQuantity || 1))
    }));
  };

  const getProductById = (productId) => {
    return products.find(product => product.id === productId);
  };

  const handleAddToCart = (product) => {
    const quantity = quantities[product.id];
    // Create a simplified product object
    const cartProduct = {
      id: product.id,
      name: product.name,
      price: product.price,
      stockQuantity: product.stockQuantity
    };
    addToCart(cartProduct, quantity);

    // Show notification
    setNotification({
      open: true,
      message: `Added ${quantity} ${product.name} to cart`,
      severity: 'success'
    });

    // Reset quantity to 1
    handleQuantityChange(product.id, 1);
  };

  // const handlePageChange = (event, newPage) => {
  //   console.log('Page changed to:', newPage);
  //   // Ensure page is never less than 0
  //   const newPageZeroBased = Math.max(0, newPage - 1); // API uses 0-based indexing
  //   setPage(newPageZeroBased);
  // };

  // const handlePageChange = (event, newPage) => {
  //   const newPageZeroBased = Math.max(page, newPage);
  //   if (newPageZeroBased !== page) {
  //     console.log('Switching to page:', newPageZeroBased);
  //     setPage(newPageZeroBased);
  //     const [searchParams, setSearchParams] = userSearchParams()
  //   }
  // };

  const handlePageChange = (event, newPage) => {
    const newPageZeroBased = Math.max(0, newPage - 1);
    if (newPageZeroBased !== page) {
      setSearchParams({ page: newPageZeroBased });
      setPage(newPageZeroBased);
    }
  };

  const handleCloseNotification = () => {
    setNotification({ ...notification, open: false });
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(price);
  };

  return (
    <Box sx={{ pt: 2 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        {searchQuery ? `Search Results: ${searchQuery}` : 'All Products'}
      </Typography>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ my: 2 }}>{error}</Alert>
      ) : products.length === 0 ? (
        <Alert severity="info" sx={{ my: 2 }}>
          {searchQuery 
            ? `No products found matching "${searchQuery}"`
            : 'No products available'
          }
        </Alert>
      ) : (
        <>
          <Grid container spacing={3}>
            {products.map((product) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                <Card 
                  sx={{ 
                    height: '100%', 
                    display: 'flex', 
                    flexDirection: 'column',
                    transition: 'transform 0.2s, box-shadow 0.2s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                    }
                  }}
                >
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component="div" gutterBottom>
                      {product.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Price: {formatPrice(product.price)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Available: {product.stockQuantity}
                    </Typography>
                  </CardContent>
                  <CardActions sx={{ p: 2, pt: 0 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
                        <IconButton 
                          size="small" 
                          onClick={() => handleQuantityChange(product.id, quantities[product.id] - 1)}
                          disabled={quantities[product.id] <= 1}
                        >
                          <RemoveIcon fontSize="small" />
                        </IconButton>
                        <TextField
                          size="small"
                          value={quantities[product.id]}
                          onChange={(e) => {
                            const value = parseInt(e.target.value);
                            if (!isNaN(value)) {
                              handleQuantityChange(product.id, value);
                            }
                          }}
                          inputProps={{ 
                            min: 1, 
                            max: product.stockQuantity,
                            style: { textAlign: 'center', width: '30px' } 
                          }}
                          variant="outlined"
                          sx={{ mx: 1 }}
                        />
                        <IconButton 
                          size="small" 
                          onClick={() => handleQuantityChange(product.id, quantities[product.id] + 1)}
                          disabled={quantities[product.id] >= product.stockQuantity}
                        >
                          <AddIcon fontSize="small" />
                        </IconButton>
                      </Box>
                      <Button 
                        variant="contained" 
                        color="primary"
                        startIcon={<AddShoppingCartIcon />}
                        onClick={() => handleAddToCart(product)}
                        disabled={product.stockQuantity === 0}
                        fullWidth
                      >
                        Add to Cart
                      </Button>
                    </Box>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>

          {console.log('Rendering with totalPages:', totalPages)}
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4, mb: 4, py: 2 }}>
            <Pagination 
              count={totalPages || 1} 
              page={page + 1} 
              onChange={handlePageChange} 
              color="primary" 
              siblingCount={1}
              size="large"
              sx={{ '& .MuiPaginationItem-root': { cursor: 'pointer' } }}
            />
          </Box>
        </>
      )}

      <Snackbar
        open={notification.open}
        autoHideDuration={3000}
        onClose={handleCloseNotification}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
      >
        <Alert 
          onClose={handleCloseNotification} 
          severity={notification.severity} 
          sx={{ width: '100%' }}
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ProductsPage;
