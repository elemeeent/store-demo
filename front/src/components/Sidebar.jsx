import React from 'react';
import { 
  Drawer, 
  List, 
  ListItem, 
  ListItemIcon, 
  ListItemText, 
  Divider,
  Box,
  useTheme,
  useMediaQuery
} from '@mui/material';
import { 
  ShoppingBag as ShoppingBagIcon, 
  Receipt as ReceiptIcon,
  Inventory as InventoryIcon,
  Assignment as AssignmentIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

const drawerWidth = 240;

const Sidebar = ({ isAdmin }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const location = useLocation();
  
  const isActive = (path) => location.pathname === path;
  
  const menuItems = [
    {
      text: 'Products',
      icon: <ShoppingBagIcon />,
      path: '/products',
      admin: false
    },
    {
      text: 'Orders',
      icon: <ReceiptIcon />,
      path: '/orders',
      admin: false
    }
  ];
  
  const adminMenuItems = [
    {
      text: 'Manage Products',
      icon: <InventoryIcon />,
      path: '/admin/products',
      admin: true
    },
    {
      text: 'Manage Orders',
      icon: <AssignmentIcon />,
      path: '/admin/orders',
      admin: true
    }
  ];
  
  // Filter menu items based on user role
  const filteredMenuItems = [
    ...menuItems,
    ...(isAdmin ? adminMenuItems : [])
  ];
  
  return (
    <Drawer
      variant={isMobile ? 'temporary' : 'permanent'}
      open={!isMobile}
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          top: '64px', // AppBar height
          height: 'calc(100% - 64px)',
        },
      }}
    >
      <Box sx={{ overflow: 'auto', mt: 2 }}>
        <List>
          {filteredMenuItems.filter(item => !item.admin).map((item) => (
            <ListItem 
              button 
              key={item.text} 
              onClick={() => navigate(item.path)}
              selected={isActive(item.path)}
              sx={{
                '&.Mui-selected': {
                  backgroundColor: theme.palette.primary.light,
                  color: theme.palette.primary.contrastText,
                  '& .MuiListItemIcon-root': {
                    color: theme.palette.primary.contrastText,
                  },
                },
                '&.Mui-selected:hover': {
                  backgroundColor: theme.palette.primary.main,
                },
              }}
            >
              <ListItemIcon sx={{ 
                color: isActive(item.path) 
                  ? theme.palette.primary.contrastText 
                  : theme.palette.text.primary 
              }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItem>
          ))}
        </List>
        
        {isAdmin && (
          <>
            <Divider sx={{ my: 2 }} />
            <List>
              <ListItem sx={{ bgcolor: 'rgba(0, 0, 0, 0.08)', py: 0.5 }}>
                <ListItemText 
                  primary="Admin" 
                  primaryTypographyProps={{ 
                    variant: 'overline',
                    color: 'text.secondary'
                  }} 
                />
              </ListItem>
              {adminMenuItems.map((item) => (
                <ListItem 
                  button 
                  key={item.text} 
                  onClick={() => navigate(item.path)}
                  selected={isActive(item.path)}
                  sx={{
                    '&.Mui-selected': {
                      backgroundColor: theme.palette.secondary.light,
                      color: theme.palette.secondary.contrastText,
                      '& .MuiListItemIcon-root': {
                        color: theme.palette.secondary.contrastText,
                      },
                    },
                    '&.Mui-selected:hover': {
                      backgroundColor: theme.palette.secondary.main,
                    },
                  }}
                >
                  <ListItemIcon sx={{ 
                    color: isActive(item.path) 
                      ? theme.palette.secondary.contrastText 
                      : theme.palette.text.primary 
                  }}>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItem>
              ))}
            </List>
          </>
        )}
      </Box>
    </Drawer>
  );
};

export default Sidebar;