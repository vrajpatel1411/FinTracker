// src/main.tsx
// REPLACES existing main.tsx — wraps App with the Fintracker MUI theme + CssBaseline
// so the dark surface/brand tokens apply globally. Keeps the Redux Provider.
// NOTE: Adjust the `./store` import path to match your actual store location.

import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";

import App from "./App";
import { store } from "./store";
import fintrackerTheme from "./theme/fintrackerTheme";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <Provider store={store}>
            <ThemeProvider theme={fintrackerTheme}>
                <CssBaseline />
                <BrowserRouter>
                    <App />
                </BrowserRouter>
            </ThemeProvider>
        </Provider>
    </React.StrictMode>
);
