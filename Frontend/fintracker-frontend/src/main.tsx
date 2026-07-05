import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { Provider } from "react-redux";
import Store from "./Redux/Store";
import { BrowserRouter } from 'react-router'
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import fintrackerTheme from './theme/fintrackerTheme';
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={Store}>
      <ThemeProvider theme={fintrackerTheme}>
        <CssBaseline />
          <BrowserRouter>
            <App />
          </BrowserRouter>
      </ThemeProvider>
    </Provider>
  </StrictMode>,
)
