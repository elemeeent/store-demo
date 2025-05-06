import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  Chip,
  CircularProgress,
  Alert,
  Pagination,
  TextField,
  Button,
  InputAdornment,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Grid
} from '@mui/material';
import { 
  Search as SearchIcon,
  Visibility as VisibilityIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { adminApi, orderApi } from '../../services/api';

const AdminOrdersPage = ({ searchQuery }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery || '');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderDetailsOpen, setOrderDetailsOpen] = useState(false);

  // Load orders when the component mounts or when search/page changes
  useEffect(() => {
    fetchOrders();
  }, [page, searchQuery]);

  // Update local search query when prop changes
  useEffect(() => {
    setLocalSearchQuery(searchQuery || '');
  }, [searchQuery]);

  const fetchOrders = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await adminApi.getAllOrders(page);

      // Filter orders by ID if search query is provided
      let orderData = response.data.data;
      if (searchQuery) {
        orderData = orderData.filter(order => 
          order.orderId.toString().includes(searchQuery)
        );
      }

      setOrders(orderData);

      // Get pagination info from response
      if (response.data.pagination) {
        const { total, maxSize } = response.data.pagination;
        setTotalPages(Math.ceil(total / maxSize) || 1);
      } else {
        setTotalPages(1);
      }

    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Failed to load orders. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    // In a real app, we would call an API with the search query
    // For this demo, we'll just filter the orders client-side
    if (localSearchQuery) {
      const filteredOrders = orders.filter(order => 
        order.orderId.toString().includes(localSearchQuery)
      );
      setOrders(filteredOrders);
    } else {
      fetchOrders();
    }
  };

  const handlePageChange = (event, newPage) => {
    // Ensure page is never less than 0
    const newPageZeroBased = Math.max(0, newPage - 1); // API uses 0-based indexing
    setPage(newPageZeroBased);
  };

  const handleViewOrderDetails = async (orderId) => {
    try {
      setLoading(true);
      const response = await orderApi.getOrder(orderId);
      setSelectedOrder(response.data.data);
      setOrderDetailsOpen(true);
    } catch (err) {
      console.error('Error fetching order details:', err);
      setError('Failed to load order details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCloseOrderDetails = () => {
    setOrderDetailsOpen(false);
    setSelectedOrder(null);
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(price);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CREATED':
        return 'info';
      case 'PAID':
        return 'success';
      case 'EXPIRED':
        return 'error';
      case 'CANCELLED':
        return 'warning';
      default:
        return 'default';
    }
  };

  const calculateTotal = (products) => {
    return products.reduce((total, item) => total + item.totalPrice, 0);
  };

  return (
    <Box sx={{ pt: 2 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Order Management
      </Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={8} md={9}>
            <TextField
              label="Search Orders"
              variant="outlined"
              fullWidth
              value={localSearchQuery}
              onChange={(e) => setLocalSearchQuery(e.target.value)}
              placeholder="Search by order ID"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} sm={4} md={3}>
            <Button
              variant="contained"
              color="primary"
              startIcon={<SearchIcon />}
              onClick={handleSearch}
              fullWidth
            >
              Search
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {loading && !orderDetailsOpen ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ my: 2 }}>{error}</Alert>
      ) : orders.length === 0 ? (
        <Alert severity="info" sx={{ my: 2 }}>
          No orders found.
        </Alert>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Order ID</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Created At</TableCell>
                  <TableCell>Expires At</TableCell>
                  <TableCell>Paid At</TableCell>
                  <TableCell align="center">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {orders.map((order) => (
                  <TableRow key={order.orderId}>
                    <TableCell>{order.orderId}</TableCell>
                    <TableCell>
                      <Chip 
                        label={order.status} 
                        color={getStatusColor(order.status)} 
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>{formatDate(order.createdAt)}</TableCell>
                    <TableCell>{formatDate(order.expiresAt)}</TableCell>
                    <TableCell>{formatDate(order.paidAt)}</TableCell>
                    <TableCell align="center">
                      <IconButton 
                        color="primary" 
                        onClick={() => handleViewOrderDetails(order.orderId)}
                        size="small"
                      >
                        <VisibilityIcon />
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

      {/* Order Details Dialog */}
      <Dialog 
        open={orderDetailsOpen} 
        onClose={handleCloseOrderDetails}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Order Details
          <IconButton
            aria-label="close"
            onClick={handleCloseOrderDetails}
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
          {selectedOrder && (
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  Order ID: {selectedOrder.orderId}
                </Typography>
                <Chip 
                  label={selectedOrder.status} 
                  color={getStatusColor(selectedOrder.status)} 
                  variant="outlined" 
                />
              </Box>

              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Created At</Typography>
                  <Typography variant="body1">{formatDate(selectedOrder.createdAt)}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Expires At</Typography>
                  <Typography variant="body1">{formatDate(selectedOrder.expiresAt)}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Paid At</Typography>
                  <Typography variant="body1">{formatDate(selectedOrder.paidAt)}</Typography>
                </Grid>
              </Grid>

              <Typography variant="h6" gutterBottom>
                Order Items
              </Typography>

              <TableContainer component={Paper} variant="outlined" sx={{ mb: 2 }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Product</TableCell>
                      <TableCell align="right">Unit Price</TableCell>
                      <TableCell align="right">Quantity</TableCell>
                      <TableCell align="right">Total</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {selectedOrder.products.map((item) => (
                      <TableRow key={item.productId}>
                        <TableCell>{item.name}</TableCell>
                        <TableCell align="right">{formatPrice(item.unitPrice)}</TableCell>
                        <TableCell align="right">{item.quantity}</TableCell>
                        <TableCell align="right">{formatPrice(item.totalPrice)}</TableCell>
                      </TableRow>
                    ))}
                    <TableRow>
                      <TableCell colSpan={3} align="right">
                        <Typography variant="subtitle2"><strong>Total:</strong></Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="subtitle2">
                          <strong>{formatPrice(calculateTotal(selectedOrder.products))}</strong>
                        </Typography>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseOrderDetails}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminOrdersPage;
