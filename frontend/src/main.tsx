import React from 'react';
import ReactDOM from 'react-dom/client';
import { ChakraProvider, extendTheme, type ThemeConfig } from '@chakra-ui/react';
import App from './App';

const config: ThemeConfig = {
  initialColorMode: 'dark',
  useSystemColorMode: false,
};

const theme = extendTheme({
  config,
  styles: {
    global: {
      body: {
        bg: 'black',
        color: 'white',
      },
    },
  },
  colors: {
    gray: {
      50: '#f7f7f7',
      100: '#e1e1e1',
      200: '#cfcfcf',
      300: '#b1b1b1',
      400: '#9e9e9e',
      500: '#7e7e7e',
      600: '#626262',
      700: '#515151',
      800: '#1a1a1a',
      900: '#111111',
    },
  },
  components: {
    Table: {
      baseStyle: {
        th: { borderColor: 'gray.700' },
        td: { borderColor: 'gray.700' },
      },
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ChakraProvider theme={theme}>
      <App />
    </ChakraProvider>
  </React.StrictMode>,
);
