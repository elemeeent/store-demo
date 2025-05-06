import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  IconButton,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Snackbar,
  Pagination,
  Grid,
  InputAdornment
} from '@mui/material';
import { 
  Add as AddIcon, 
  Edit as EditIcon, 
  Delete as DeleteIcon,
  Search as SearchIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { adminApi } from '../../services/api';

const AdminProductsPage = ({ searchQuery }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    price: '',
    stockQuantity: ''
  });
  const [formErrors, setFormErrors] = useState({});
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'success' });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [productToDelete, setProductToDelete] = useState(null);
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery || '');

  // Load products when the component mounts or when search/page changes
  useEffect(() => {
    fetchProducts();
  }, [page, searchQuery]);

  // Update local search query when prop changes
  useEffect(() => {
    setLocalSearchQuery(searchQuery || '');
  }, [searchQuery]);

  const fetchProducts = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await adminApi.getAllProducts(page);

      setProducts(response.data.data);

      // Get pagination info from response
      if (response.data.pagination) {
        const { total, maxSize } = response.data.pagination;
        setTotalPages(Math.ceil(total / maxSize) || 1);
      } else {
        setTotalPages(1);
      }

    } catch (err) {
      console.error('Error fetching products:', err);
      setError('Failed to load products. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!localSearchQuery) {
      fetchProducts();
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Try to parse the search query as UUID first
      let productId = null;
      try {
        // Simple UUID format validation
        if (localSearchQuery.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i)) {
          productId = localSearchQuery;
        }
      } catch (e) {
        // Not a valid UUID, will search by name
      }

      const response = await adminApi.searchProduct(productId, productId ? null : localSearchQuery);

      // If we get a single product back, convert it to an array for consistent handling
      const productData = response.data.data;
      setProducts(Array.isArray(productData) ? productData : [productData]);

      // Reset pagination since we're now showing search results
      setTotalPages(1);
    } catch (err) {
      console.error('Error searching products:', err);
      setError('Failed to search products. Product not found or was deleted');
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (event, newPage) => {
    // Ensure page is never less than 0
    const newPageZeroBased = Math.max(0, newPage - 1); // API uses 0-based indexing
    setPage(newPageZeroBased);
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setFormData({
      name: '',
      price: '',
      stockQuantity: ''
    });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleOpenEditDialog = (product) => {
    setDialogMode('edit');
    setSelectedProduct(product);
    setFormData({
      name: product.name,
      price: product.price.toString(),
      stockQuantity: product.stockQuantity.toString()
    });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });

    // Clear error for this field
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: ''
      });
    }
  };

  const validateForm = () => {
    const errors = {};

    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }

    if (!formData.price) {
      errors.price = 'Price is required';
    } else if (isNaN(parseFloat(formData.price)) || parseFloat(formData.price) < 0) {
      errors.price = 'Price must be a positive number';
    }

    if (!formData.stockQuantity) {
      errors.stockQuantity = 'Stock quantity is required';
    } else if (isNaN(parseInt(formData.stockQuantity)) || parseInt(formData.stockQuantity) < 0) {
      errors.stockQuantity = 'Stock quantity must be a non-negative integer';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const productData = {
        name: formData.name,
        price: parseFloat(formData.price),
        stockQuantity: parseInt(formData.stockQuantity)
      };

      if (dialogMode === 'create') {
        // Create new product
        const response = await adminApi.createProduct(productData);
        const responseData = response.data.data;

        // Check if there are any error products
        if (responseData.errorProducts && responseData.errorProducts.length > 0) {
          const errorProduct = responseData.errorProducts[0];
          setNotification({
            open: true,
            message: `Product not created: ${errorProduct.errorMessage}`,
            severity: 'error'
          });
        } else {
          setNotification({
            open: true,
            message: 'Product created successfully',
            severity: 'success'
          });
        }
      } else {
        // Update existing product
        const updateData = {
          [selectedProduct.id]: productData
        };
        await adminApi.updateProduct(updateData);
        setNotification({
          open: true,
          message: 'Product updated successfully',
          severity: 'success'
        });
      }

      // Refresh product list
      fetchProducts();
      handleCloseDialog();

    } catch (err) {
      console.error('Error saving product:', err);
      setNotification({
        open: true,
        message: `Failed to ${dialogMode} product: ${err.response?.data?.message || 'Unknown error'}`,
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDeleteDialog = (product) => {
    setProductToDelete(product);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setProductToDelete(null);
  };

  const handleDeleteProduct = async () => {
    if (!productToDelete) return;

    setLoading(true);

    try {
      const response = await adminApi.deleteProduct(productToDelete.id);
      const deletedProduct = response.data.data;

      setNotification({
        open: true,
        message: `Product "${deletedProduct.name}" deleted successfully`,
        severity: 'success'
      });

      // Refresh product list
      fetchProducts();

    } catch (err) {
      console.error('Error deleting product:', err);
      setNotification({
        open: true,
        message: `Failed to delete product: ${err.response?.data?.message || 'Unknown error'}`,
        severity: 'error'
      });
    } finally {
      setLoading(false);
      handleCloseDeleteDialog();
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
        Product Management
      </Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={8}>
            <TextField
              label="Search Products"
              variant="outlined"
              fullWidth
              value={localSearchQuery}
              onChange={(e) => setLocalSearchQuery(e.target.value)}
              placeholder="Search by product name"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', sm: 'flex-end' } }}>
            <Button
              variant="contained"
              color="primary"
              startIcon={<SearchIcon />}
              onClick={handleSearch}
              sx={{ mr: 1 }}
            >
              Search
            </Button>
            <Button
              variant="contained"
              color="secondary"
              startIcon={<AddIcon />}
              onClick={handleOpenCreateDialog}
            >
              Add Product
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {loading && !dialogOpen && !deleteDialogOpen ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ my: 2 }}>{error}</Alert>
      ) : products.length === 0 ? (
        <Alert severity="info" sx={{ my: 2 }}>
          No products found. Create a new product to get started.
        </Alert>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell align="right">Price</TableCell>
                  <TableCell align="right">Stock</TableCell>
                  <TableCell align="center">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell>{product.name}</TableCell>
                    <TableCell align="right">{formatPrice(product.price)}</TableCell>
                    <TableCell align="right">{product.stockQuantity}</TableCell>
                    <TableCell align="center">
                      <IconButton 
                        color="primary" 
                        onClick={() => handleOpenEditDialog(product)}
                        size="small"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton 
                        color="error" 
                        onClick={() => handleOpenDeleteDialog(product)}
                        size="small"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

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

      {/* Create/Edit Product Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {dialogMode === 'create' ? 'Create New Product' : 'Edit Product'}
          <IconButton
            aria-label="close"
            onClick={handleCloseDialog}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="name"
              label="Product Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              error={!!formErrors.name}
              helperText={formErrors.name}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="price"
              label="Price"
              name="price"
              type="number"
              inputProps={{ min: 0, step: 0.01 }}
              value={formData.price}
              onChange={handleInputChange}
              error={!!formErrors.price}
              helperText={formErrors.price}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="stockQuantity"
              label="Stock Quantity"
              name="stockQuantity"
              type="number"
              inputProps={{ min: 0, step: 1 }}
              value={formData.stockQuantity}
              onChange={handleInputChange}
              error={!!formErrors.stockQuantity}
              helperText={formErrors.stockQuantity}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained" 
            color="primary"
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the product "{productToDelete?.name}"?
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancel</Button>
          <Button 
            onClick={handleDeleteProduct} 
            variant="contained" 
            color="error"
            disabled={loading}
          >
            {loading ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Notification Snackbar */}
      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
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

export default AdminProductsPage;
