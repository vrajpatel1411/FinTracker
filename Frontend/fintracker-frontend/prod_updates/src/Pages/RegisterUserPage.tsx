// src/Pages/RegisterUserPage.tsx
// Drop-in replacement. Same form fields + RTK-Query mutation contract.

import { Box, Button, Divider, FormControl, FormLabel, TextField, Typography } from "@mui/material";
import { Link, useNavigate } from "react-router";
import { useState } from "react";

import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import SocialMediaButtons from "../Component/auth/SocialMediaButtons";
import Modal from "../Utils/Modal";
import { useRegisterMutation } from "../api/authApi"; // adjust path

export default function RegisterUserPage() {
    const navigate = useNavigate();
    const [register, { isLoading }] = useRegisterMutation();
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [modal, setModal] = useState<string | null>(null);

    const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const fd = new FormData(e.currentTarget);
        const data = {
            firstName: String(fd.get("firstName") || "").trim(),
            lastName: String(fd.get("lastName") || "").trim(),
            email: String(fd.get("email") || "").trim(),
            password: String(fd.get("password") || ""),
        };
        const next: Record<string, string> = {};
        if (!data.firstName) next.firstName = "Required";
        if (!data.lastName) next.lastName = "Required";
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) next.email = "Enter a valid email.";
        if (data.password.length < 6) next.password = "At least 6 characters.";
        setErrors(next);
        if (Object.keys(next).length) return;
        try {
            await register(data).unwrap();
            navigate("/verify-otp", { state: { email: data.email } });
        } catch {
            setModal("Registration failed. Please try again.");
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
                        Create your account.
                    </Typography>
                </Box>

                <Box component="form" onSubmit={onSubmit} noValidate sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                    <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2 }}>
                        <FormControl>
                            <FormLabel htmlFor="firstName">First name</FormLabel>
                            <TextField id="firstName" name="firstName" placeholder="Jon" required
                                error={!!errors.firstName} helperText={errors.firstName} />
                        </FormControl>
                        <FormControl>
                            <FormLabel htmlFor="lastName">Last name</FormLabel>
                            <TextField id="lastName" name="lastName" placeholder="Snow" required
                                error={!!errors.lastName} helperText={errors.lastName} />
                        </FormControl>
                    </Box>
                    <FormControl>
                        <FormLabel htmlFor="email">Email</FormLabel>
                        <TextField id="email" name="email" type="email" autoComplete="email"
                            placeholder="your@email.com" required
                            error={!!errors.email} helperText={errors.email} />
                    </FormControl>
                    <FormControl>
                        <FormLabel htmlFor="password">Password</FormLabel>
                        <TextField id="password" name="password" type="password" autoComplete="new-password"
                            placeholder="At least 6 characters" required
                            error={!!errors.password} helperText={errors.password} />
                    </FormControl>

                    <Button type="submit" variant="contained" disabled={isLoading} fullWidth>
                        {isLoading ? "Creating…" : "Create Account"}
                    </Button>
                </Box>

                <Divider sx={{ my: 1, "&::before, &::after": { borderColor: "rgba(63,63,70,0.5)" }, color: "#71717a", fontSize: 12 }}>
                    OR
                </Divider>

                <SocialMediaButtons />

                <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
                    Already registered?{" "}
                    <Link to="/login" style={{ color: "#14b8a6", textDecoration: "none" }}>Sign in</Link>
                </Typography>
            </Card>

            {modal && <Modal error={modal} setModal={setModal} />}
        </SignUpContainer>
    );
}
