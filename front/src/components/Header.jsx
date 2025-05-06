import React, { useState } from 'react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  TextField, 
  InputAdornment,
  IconButton,
  Badge,
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Input,
  FormHelperText
} from '@mui/material';
import { 
  Search as SearchIcon, 
  ShoppingCart as ShoppingCartIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useCart } from '../contexts/CartContext';
import CartDrawer from './CartDrawer';

const Header = ({ searchQuery, setSearchQuery }) => {
  const { user, isAuthenticated, login, logout } = useAuth();
  const { getCartItemCount, openCart } = useCart();

  const [loginDialogOpen, setLoginDialogOpen] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const handleLoginClick = () => {
    setLoginDialogOpen(true);
  };

  const handleLoginClose = () => {
    setLoginDialogOpen(false);
    setUsername('');
    setPassword('');
    setError('');
  };

  const handleLogin = async () => {
    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }

    setIsLoggingIn(true);
    setError('');

    try {
      const result = await login(username, password);
      if (result.success) {
        handleLoginClose();
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError('An error occurred during login. Please try again.');
      console.error('Login error:', err);
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <>
      <AppBar position="sticky">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Store Demo Application
          </Typography>

          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <TextField
              placeholder="Search..."
              value={searchQuery}
              onChange={handleSearchChange}
              size="small"
              sx={{ 
                mr: 2, 
                backgroundColor: 'rgba(255, 255, 255, 0.15)',
                borderRadius: 1,
                '& .MuiOutlinedInput-root': {
                  color: 'white',
                  '& fieldset': {
                    borderColor: 'transparent',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(255, 255, 255, 0.3)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: 'rgba(255, 255, 255, 0.5)',
                  },
                },
                '& .MuiInputLabel-root': {
                  color: 'rgba(255, 255, 255, 0.7)',
                },
                '& .MuiInputBase-input::placeholder': {
                  color: 'rgba(255, 255, 255, 0.7)',
                  opacity: 1,
                },
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'rgba(255, 255, 255, 0.7)' }} />
                  </InputAdornment>
                ),
              }}
            />

            <IconButton 
              color="inherit" 
              onClick={openCart}
              sx={{ mr: 2 }}
            >
              <Badge 
                badgeContent={getCartItemCount()} 
                color="secondary" 
                showZero
                anchorOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                sx={{ 
                  '& .MuiBadge-badge': { 
                    fontSize: '0.8rem',
                    zIndex: 1000 
                  } 
                }}
              >
                <ShoppingCartIcon />
              </Badge>
            </IconButton>

            {isAuthenticated ? (
              <Button 
                color="inherit" 
                onClick={handleLogout}
              >
                Logout ({user.username})
              </Button>
            ) : (
              <Button 
                color="inherit" 
                onClick={handleLoginClick}
              >
                Login
              </Button>
            )}
          </Box>
        </Toolbar>
      </AppBar>

      {/* Login Dialog */}
      <Dialog open={loginDialogOpen} onClose={handleLoginClose}>
        <DialogTitle>
          Login
          <IconButton
            aria-label="close"
            onClick={handleLoginClose}
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
            <FormControl fullWidth margin="normal" error={!!error}>
              <InputLabel htmlFor="username">Username</InputLabel>
              <Input
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoFocus
              />
            </FormControl>
            <FormControl fullWidth margin="normal" error={!!error}>
              <InputLabel htmlFor="password">Password</InputLabel>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              {error && <FormHelperText>{error}</FormHelperText>}
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleLoginClose}>Cancel</Button>
          <Button 
            onClick={handleLogin} 
            variant="contained" 
            disabled={isLoggingIn}
          >
            {isLoggingIn ? 'Logging in...' : 'Login'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cart Drawer */}
      <CartDrawer />
    </>
  );
};

export default Header;
