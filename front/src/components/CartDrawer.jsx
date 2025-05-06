import React, { useState } from 'react';
import { 
  Drawer, 
  Box, 
  Typography, 
  IconButton, 
  List, 
  ListItem, 
  ListItemText, 
  ListItemSecondaryAction, 
  Divider, 
  Button,
  TextField,
  Grid,
  Alert,
  Snackbar
} from '@mui/material';
import { 
  Close as CloseIcon, 
  Add as AddIcon, 
  Remove as RemoveIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { useCart } from '../contexts/CartContext';
import { orderApi } from '../services/api';

const CartDrawer = () => {
  const { 
    cartItems, 
    isCartOpen, 
    closeCart, 
    updateCartItemQuantity, 
    removeFromCart, 
    clearCart,
    getCartTotal 
  } = useCart();

  const [orderSuccess, setOrderSuccess] = useState(false);
  const [orderError, setOrderError] = useState('');
  const [orderId, setOrderId] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const handleQuantityChange = (productId, newQuantity) => {
    updateCartItemQuantity(productId, newQuantity);
  };

  const handleRemoveItem = (productId) => {
    removeFromCart(productId);
  };

  const handleCreateOrder = async () => {
    if (cartItems.length === 0) {
      setOrderError('Your cart is empty');
      return;
    }

    setIsProcessing(true);
    setOrderError('');

    try {
      // Format cart items for the API
      const orderItems = cartItems.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }));

      // Call the API to create the order
      const response = await orderApi.createOrder(orderItems);

      // Extract order ID from the response
      const newOrderId = response.data.data.orderId;
      setOrderId(newOrderId);
      setOrderSuccess(true);

      // Clear the cart after successful order
      clearCart();
    } catch (error) {
      console.error('Error creating order:', error);
      setOrderError(error.response?.data?.message || 'Failed to create order. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleCloseSnackbar = () => {
    setOrderSuccess(false);
    setOrderError('');
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(price);
  };

  return (
    <>
      <Drawer
        anchor="right"
        open={isCartOpen}
        onClose={closeCart}
        PaperProps={{
          sx: { width: { xs: '100%', sm: 400 } }
        }}
      >
        <Box sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Shopping Cart</Typography>
            <IconButton onClick={closeCart}>
              <CloseIcon />
            </IconButton>
          </Box>

          <Divider />

          {cartItems.length === 0 ? (
            <Box sx={{ py: 4, textAlign: 'center' }}>
              <Typography variant="body1">Your cart is empty</Typography>
              <Button 
                variant="contained" 
                color="primary" 
                sx={{ mt: 2 }}
                onClick={closeCart}
              >
                Continue Shopping
              </Button>
            </Box>
          ) : (
            <>
              <List>
                {cartItems.map((item) => (
                  <React.Fragment key={item.id}>
                    <ListItem>
                      <Grid container spacing={2} alignItems="center">
                        <Grid item xs={7}>
                          <ListItemText
                            primary={item.name}
                            secondary={`${formatPrice(item.price)} each`}
                          />
                        </Grid>
                        <Grid item xs={3}>
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <IconButton 
                              size="small" 
                              onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                              disabled={item.quantity <= 1}
                            >
                              <RemoveIcon fontSize="small" />
                            </IconButton>
                            <TextField
                              size="small"
                              value={item.quantity}
                              onChange={(e) => {
                                const value = parseInt(e.target.value);
                                if (!isNaN(value)) {
                                  handleQuantityChange(item.id, value);
                                }
                              }}
                              inputProps={{ 
                                min: 1, 
                                max: item.stockQuantity,
                                style: { textAlign: 'center', width: '30px' } 
                              }}
                              variant="outlined"
                              sx={{ mx: 1 }}
                            />
                            <IconButton 
                              size="small" 
                              onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                              disabled={item.quantity >= item.stockQuantity}
                            >
                              <AddIcon fontSize="small" />
                            </IconButton>
                          </Box>
                        </Grid>
                        <Grid item xs={2} sx={{ textAlign: 'right' }}>
                          <IconButton 
                            edge="end" 
                            onClick={() => handleRemoveItem(item.id)}
                            color="error"
                            size="small"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Grid>
                      </Grid>
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
              </List>

              <Box sx={{ mt: 2, mb: 2 }}>
                <Typography variant="h6" align="right">
                  Total: {formatPrice(getCartTotal())}
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Button 
                  variant="outlined" 
                  onClick={clearCart}
                  disabled={isProcessing}
                >
                  Clear Cart
                </Button>
                <Button 
                  variant="contained" 
                  color="primary" 
                  onClick={handleCreateOrder}
                  disabled={isProcessing}
                >
                  {isProcessing ? 'Processing...' : 'Create Order'}
                </Button>
              </Box>
            </>
          )}
        </Box>
      </Drawer>

      {/* Success Snackbar */}
      <Snackbar
        open={orderSuccess}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity="success" sx={{ width: '100%' }}>
          Order created successfully! Order ID: {orderId}
        </Alert>
      </Snackbar>

      {/* Error Snackbar */}
      <Snackbar
        open={!!orderError}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity="error" sx={{ width: '100%' }}>
          {orderError}
        </Alert>
      </Snackbar>
    </>
  );
};

export default CartDrawer;
