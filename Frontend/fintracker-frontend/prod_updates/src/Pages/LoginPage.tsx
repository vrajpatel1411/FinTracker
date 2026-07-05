// src/Pages/LoginPage.tsx
// Drop-in replacement. Same RTK-Query / routing / props contract — pure visual + a11y refresh.
// - Inherits dark theme from fintrackerTheme (no more local <CssBaseline /> conflict).
// - Adds id/htmlFor pairings and aria-invalid.
// - Removes the light-mode background.

import { Box, Button, Divider, FormControl, FormLabel, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router";
import { useState } from "react";

import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import SocialMediaButtons from "../Component/auth/SocialMediaButtons";
import Modal from "../Utils/Modal";
import { useLoginMutation } from "../api/authApi"; // adjust to your real path

export default function LoginPage() {
    const navigate = useNavigate();
    const [login, { isLoading }] = useLoginMutation();
    const [emailErr, setEmailErr] = useState("");
    const [pwErr, setPwErr] = useState("");
    const [modal, setModal] = useState<string | null>(null);

    const validate = (email: string, password: string) => {
        let ok = true;
        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            setEmailErr("Enter a valid email."); ok = false;
        } else setEmailErr("");
        if (!password || password.length < 6) {
            setPwErr("Password must be at least 6 characters."); ok = false;
        } else setPwErr("");
        return ok;
    };

    const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const fd = new FormData(e.currentTarget);
        const email = String(fd.get("email") || "");
        const password = String(fd.get("password") || "");
        if (!validate(email, password)) return;
        try {
            await login({ email, password }).unwrap();
            navigate("/personal");
        } catch {
            setModal("Login failed. Please try again.");
        }
    };

    return (
        <SignUpContainer direction="column" justifyContent="center">
            <Card variant="outlined">
                <Box sx={{ textAlign: "center", mb: 1 }}>
                    <Typography
                        component="h1"
                        sx={{
                            fontFamily: "'Poppins', 'Inter', sans-serif",
                            fontWeight: 700,
                            fontSize: { xs: 26, sm: 30 },
                            letterSpacing: "-0.02em",
                            color: "#f4f4f5",
                        }}
                    >
                        Fin<Box component="span" sx={{ color: "#009689" }}>tracker</Box>
                    </Typography>
                    <Typography sx={{ color: "#a1a1aa", fontSize: 14, mt: 1 }}>
                        Welcome back. Sign in to your account.
                    </Typography>
                </Box>

                <Box component="form" onSubmit={onSubmit} noValidate sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                    <FormControl>
                        <FormLabel htmlFor="email">Email</FormLabel>
                        <TextField
                            id="email" name="email" type="email" autoComplete="email"
                            placeholder="your@email.com" required
                            error={!!emailErr} helperText={emailErr}
                            inputProps={{ "aria-invalid": !!emailErr }}
                        />
                    </FormControl>
                    <FormControl>
                        <FormLabel htmlFor="password">Password</FormLabel>
                        <TextField
                            id="password" name="password" type="password" autoComplete="current-password"
                            placeholder="••••••" required
                            error={!!pwErr} helperText={pwErr}
                            inputProps={{ "aria-invalid": !!pwErr }}
                        />
                    </FormControl>

                    <Button type="submit" variant="contained" disabled={isLoading} fullWidth>
                        {isLoading ? "Signing in…" : "Sign In"}
                    </Button>
                </Box>

                <Divider sx={{ my: 1, "&::before, &::after": { borderColor: "rgba(63,63,70,0.5)" }, color: "#71717a", fontSize: 12 }}>
                    OR
                </Divider>

                <SocialMediaButtons />

                <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
                    No account?{" "}
                    <Link to="/register" style={{ color: "#14b8a6", textDecoration: "none" }}>Sign up</Link>
                </Typography>
            </Card>

            {modal && <Modal error={modal} setModal={setModal} />}
        </SignUpContainer>
    );
}
