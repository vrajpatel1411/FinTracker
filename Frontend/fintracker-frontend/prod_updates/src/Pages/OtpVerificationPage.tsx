// src/Pages/OtpVerificationPage.tsx
// Drop-in replacement. Keep your existing OTP component import path.

import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { Box, Button, Typography } from "@mui/material";

import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import OTP from "../Component/auth/Otp";
import Modal from "../Utils/Modal";
import { useVerifyOtpMutation, useResendOtpMutation } from "../api/authApi";

export default function OtpVerificationPage() {
    const navigate = useNavigate();
    const { state } = useLocation() as { state: { email?: string } | null };
    const email = state?.email ?? "";
    const [code, setCode] = useState("");
    const [seconds, setSeconds] = useState(60);
    const [modal, setModal] = useState<string | null>(null);
    const [verify, { isLoading: verifying }] = useVerifyOtpMutation();
    const [resend] = useResendOtpMutation();

    useEffect(() => {
        if (seconds <= 0) return;
        const t = setTimeout(() => setSeconds((s) => s - 1), 1000);
        return () => clearTimeout(t);
    }, [seconds]);

    const onVerify = async () => {
        if (code.length < 6) return setModal("Enter the 6-digit code.");
        try {
            await verify({ email, otp: code }).unwrap();
            navigate("/personal");
        } catch {
            setModal("Verification failed. Please try again.");
        }
    };

    const onResend = async () => {
        try {
            await resend({ email }).unwrap();
            setSeconds(60);
        } catch {
            setModal("Could not resend code.");
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
                            fontWeight: 700, fontSize: 26, letterSpacing: "-0.02em",
                            color: "#f4f4f5",
                        }}
                    >
                        Verify your email
                    </Typography>
                    <Typography sx={{ color: "#a1a1aa", fontSize: 14, mt: 1 }}>
                        We sent a 6-digit code to <Box component="span" sx={{ color: "#d4d4d8" }}>{email || "your email"}</Box>.
                    </Typography>
                </Box>

                <OTP value={code} onChange={setCode} length={6} />

                <Button onClick={onVerify} variant="contained" fullWidth disabled={verifying || code.length < 6}>
                    {verifying ? "Verifying…" : "Verify"}
                </Button>

                <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
                    Didn't get it?{" "}
                    {seconds > 0 ? (
                        <Box component="span" sx={{ color: "#71717a" }}>Resend in {seconds}s</Box>
                    ) : (
                        <Box component="button" type="button" onClick={onResend}
                            sx={{ background: "none", border: "none", color: "#14b8a6", cursor: "pointer", p: 0, fontSize: 13 }}>
                            Resend code
                        </Box>
                    )}
                </Typography>
            </Card>

            {modal && <Modal error={modal} setModal={setModal} />}
        </SignUpContainer>
    );
}
