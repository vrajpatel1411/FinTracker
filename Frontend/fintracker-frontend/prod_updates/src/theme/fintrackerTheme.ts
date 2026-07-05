// src/theme/fintrackerTheme.ts
// MUI v6 dark theme aligned to the Fintracker design system.
// Consumed in main.tsx via <ThemeProvider theme={fintrackerTheme}>.

import { createTheme } from "@mui/material/styles";

export const fintrackerTheme = createTheme({
    palette: {
        mode: "dark",
        primary: { main: "#009689", dark: "#005a52", contrastText: "#ffffff" },
        secondary: { main: "#14b8a6" },
        error: { main: "#f87171" },
        warning: { main: "#F2994A" },
        success: { main: "#34d399" },
        background: { default: "#0f0f14", paper: "#17171c" },
        text: { primary: "#f4f4f5", secondary: "#a1a1aa", disabled: "#52525b" },
        divider: "rgba(63,63,70,0.5)",
    },
    shape: { borderRadius: 12 },
    typography: {
        fontFamily: "'Inter', system-ui, sans-serif",
        h1: { fontFamily: "'Poppins', 'Inter', sans-serif", fontWeight: 700, letterSpacing: "-0.02em" },
        h2: { fontFamily: "'Poppins', 'Inter', sans-serif", fontWeight: 700, letterSpacing: "-0.02em" },
        h3: { fontFamily: "'Poppins', 'Inter', sans-serif", fontWeight: 600, letterSpacing: "-0.02em" },
        button: { textTransform: "none", fontWeight: 600, letterSpacing: "0em" },
    },
    components: {
        MuiCssBaseline: {
            styleOverrides: {
                body: { backgroundColor: "#0f0f14", color: "#f4f4f5" },
                "*::-webkit-scrollbar": { width: 10, height: 10 },
                "*::-webkit-scrollbar-thumb": { background: "#2a2a30", borderRadius: 8 },
                "*::-webkit-scrollbar-thumb:hover": { background: "#3d3d45" },
            },
        },
        MuiButton: {
            defaultProps: { disableElevation: true },
            styleOverrides: {
                root: { borderRadius: 8, padding: "8px 14px", fontSize: 14 },
                containedPrimary: {
                    backgroundColor: "#009689",
                    "&:hover": { backgroundColor: "#005a52" },
                    "&.Mui-disabled": { backgroundColor: "rgba(0,150,137,0.4)", color: "rgba(255,255,255,0.7)" },
                },
                outlined: {
                    borderColor: "rgba(63,63,70,0.8)",
                    color: "#d4d4d8",
                    "&:hover": { borderColor: "#52525b", backgroundColor: "#2a2a30" },
                },
            },
        },
        MuiTextField: {
            defaultProps: { variant: "outlined", size: "small", fullWidth: true },
            styleOverrides: {
                root: {
                    "& .MuiOutlinedInput-root": {
                        borderRadius: 12,
                        backgroundColor: "#0a0a0f",
                        "& fieldset": { borderColor: "rgba(63,63,70,0.8)" },
                        "&:hover fieldset": { borderColor: "#52525b" },
                        "&.Mui-focused fieldset": { borderColor: "#009689", borderWidth: 1 },
                    },
                    "& .MuiInputBase-input": { fontSize: 14, color: "#f4f4f5" },
                    "& .MuiInputBase-input::placeholder": { color: "#71717a", opacity: 1 },
                },
            },
        },
        MuiFormLabel: {
            styleOverrides: {
                root: { color: "#d4d4d8", fontSize: 14, fontWeight: 500, marginBottom: 6 },
            },
        },
        MuiDialog: {
            styleOverrides: {
                paper: {
                    backgroundColor: "#17171c",
                    border: "1px solid rgba(63,63,70,0.5)",
                    borderRadius: 16,
                    boxShadow: "0 25px 50px -12px rgba(0,0,0,0.8)",
                },
            },
        },
        MuiMenu: {
            styleOverrides: {
                paper: {
                    backgroundColor: "#2a2a30",
                    border: "1px solid rgba(63,63,70,0.8)",
                    borderRadius: 8,
                },
            },
        },
        MuiMenuItem: {
            styleOverrides: {
                root: {
                    fontSize: 14,
                    "&:hover": { backgroundColor: "#3d3d45" },
                },
            },
        },
        MuiIconButton: {
            styleOverrides: { root: { color: "#d4d4d8", "&:hover": { backgroundColor: "#3d3d45" } } },
        },
        MuiDivider: {
            styleOverrides: { root: { borderColor: "rgba(63,63,70,0.5)" } },
        },
        MuiPaper: {
            styleOverrides: {
                root: { backgroundImage: "none" },
            },
        },
    },
});

export default fintrackerTheme;
