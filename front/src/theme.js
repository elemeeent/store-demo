import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#4caf50', // Green color for the header
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#ff9800', // Orange for accents
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 500,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 500,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 500,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 500,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 500,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 500,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          textTransform: 'none',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
        },
      },
    },
    MuiPagination: {
      styleOverrides: {
        root: {
          '& .MuiPaginationItem-root': {
            zIndex: 1,
          },
          '& .MuiPaginationItem-icon': {
            pointerEvents: 'auto',
          },
        },
      },
    },
    MuiPaginationItem: {
      styleOverrides: {
        root: {
          margin: '0 4px',
          minWidth: '32px',
          height: '32px',
          cursor: 'pointer',
          '&.Mui-selected': {
            fontWeight: 'bold',
          },
        },
        icon: {
          fontSize: '1.5rem',
          cursor: 'pointer',
        },
      },
    },
  },
});

export default theme;
