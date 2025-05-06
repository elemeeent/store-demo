import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  TextField, 
  Button, 
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton
} from '@mui/material';
import { 
  Search as SearchIcon,
  Payment as PaymentIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { orderApi } from '../services/api';

const OrdersPage = ({ searchQuery }) => {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery || '');
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [paymentError, setPaymentError] = useState('');
  
  // Update local search query when prop changes
  useEffect(() => {
    setLocalSearchQuery(searchQuery || '');
    if (searchQuery) {
      handleSearch();
    }
  }, [searchQuery]);
  
  const handleSearch = async () => {
    if (!localSearchQuery) {
      setError('Please enter an order ID');
      return;
    }
    
    setLoading(true);
    setError('');
    setOrder(null);
    
    try {
      const response = await orderApi.getOrder(localSearchQuery);
      setOrder(response.data.data);
    } catch (err) {
      console.error('Error fetching order:', err);
      setError('Order not found or an error occurred. Please check the ID and try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handlePayOrder = async () => {
    setPaymentLoading(true);
    setPaymentError('');
    setPaymentSuccess(false);
    
    try {
      await orderApi.payOrder(order.orderId);
      setPaymentSuccess(true);
      
      // Refresh order data
      const response = await orderApi.getOrder(order.orderId);
      setOrder(response.data.data);
    } catch (err) {
      console.error('Error paying for order:', err);
      setPaymentError('Failed to process payment. Please try again.');
    } finally {
      setPaymentLoading(false);
    }
  };
  
  const handlePaymentDialogClose = () => {
    setPaymentDialogOpen(false);
    setPaymentError('');
    setPaymentSuccess(false);
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
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <TextField
            label="Order ID"
            variant="outlined"
            fullWidth
            value={localSearchQuery}
            onChange={(e) => setLocalSearchQuery(e.target.value)}
            placeholder="Enter order ID to search"
            sx={{ mr: 2 }}
          />
          <Button
            variant="contained"
            color="primary"
            startIcon={<SearchIcon />}
            onClick={handleSearch}
            disabled={loading}
          >
            Search
          </Button>
        </Box>
      </Paper>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ my: 2 }}>{error}</Alert>
      ) : order ? (
        <Paper sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5">
              Order Details
            </Typography>
            <Chip 
              label={order.status} 
              color={getStatusColor(order.status)} 
              variant="outlined" 
            />
          </Box>
          
          <Box sx={{ mb: 3 }}>
            <Typography variant="body1">
              <strong>Order ID:</strong> {order.orderId}
            </Typography>
            <Typography variant="body1">
              <strong>Created:</strong> {formatDate(order.createdAt)}
            </Typography>
            {order.paidAt && (
              <Typography variant="body1">
                <strong>Paid:</strong> {formatDate(order.paidAt)}
              </Typography>
            )}
            {order.expiresAt && (
              <Typography variant="body1">
                <strong>Expires:</strong> {formatDate(order.expiresAt)}
              </Typography>
            )}
          </Box>
          
          <Typography variant="h6" gutterBottom>
            Order Items
          </Typography>
          
          <TableContainer component={Paper} sx={{ mb: 3 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Product</TableCell>
                  <TableCell align="right">Unit Price</TableCell>
                  <TableCell align="right">Quantity</TableCell>
                  <TableCell align="right">Total</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {order.products.map((item) => (
                  <TableRow key={item.productId}>
                    <TableCell>{item.name}</TableCell>
                    <TableCell align="right">{formatPrice(item.unitPrice)}</TableCell>
                    <TableCell align="right">{item.quantity}</TableCell>
                    <TableCell align="right">{formatPrice(item.totalPrice)}</TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell colSpan={3} align="right">
                    <Typography variant="subtitle1"><strong>Total:</strong></Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="subtitle1"><strong>{formatPrice(calculateTotal(order.products))}</strong></Typography>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
          
          {order.status === 'CREATED' && (
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="contained"
                color="primary"
                startIcon={<PaymentIcon />}
                onClick={() => setPaymentDialogOpen(true)}
              >
                Pay Now
              </Button>
              <Button
                  variant="outlined"
                  color="error"
                  sx={{ ml: 2 }}
                  onClick={async () => {
                    try {
                      await orderApi.cancelOrder(order.orderId);
                      setOrder(null);
                      setError('Order was cancelled.');
                    } catch (err) {
                      console.error('Failed to cancel order:', err);
                      setError('Failed to cancel the order.');
                    }
                  }}
              >
                Cancel Order
              </Button>
            </Box>
          )}
        </Paper>
      ) : (
        <Alert severity="info" sx={{ my: 2 }}>
          Enter an order ID to view details
        </Alert>
      )}
      
      {/* Payment Dialog */}
      <Dialog open={paymentDialogOpen} onClose={handlePaymentDialogClose}>
        <DialogTitle>
          Process Payment
          <IconButton
            aria-label="close"
            onClick={handlePaymentDialogClose}
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
          {paymentSuccess ? (
            <Alert severity="success" sx={{ my: 2 }}>
              Payment processed successfully!
            </Alert>
          ) : paymentError ? (
            <Alert severity="error" sx={{ my: 2 }}>
              {paymentError}
            </Alert>
          ) : (
            <Box sx={{ my: 2 }}>
              <Typography variant="body1" gutterBottom>
                You are about to process payment for order: <strong>{order?.orderId}</strong>
              </Typography>
              <Typography variant="body1" gutterBottom>
                Total amount: <strong>{order ? formatPrice(calculateTotal(order.products)) : ''}</strong>
              </Typography>
              <Typography variant="body2" color="text.secondary">
                This is a demo application. No actual payment will be processed.
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handlePaymentDialogClose}>
            {paymentSuccess ? 'Close' : 'Cancel'}
          </Button>
          {!paymentSuccess && (
            <Button 
              onClick={handlePayOrder} 
              variant="contained" 
              color="primary"
              disabled={paymentLoading}
            >
              {paymentLoading ? 'Processing...' : 'Confirm Payment'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrdersPage;