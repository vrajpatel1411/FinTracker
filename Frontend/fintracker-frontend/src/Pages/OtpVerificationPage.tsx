import OTP from "../Component/auth/Otp";
import { useEffect, useState } from "react";
import { Box, Button, Typography } from "@mui/material";
import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import {useTimer} from "react-timer-hook"
import { useAppDispatch } from "../Redux/hooks";
import VerifyOTP from "../Redux/thunks/VerifyOTP";
import { useNavigate } from "react-router";
import Modal from "../Utils/Modal";
import axios from "axios";
import { AuthApiResponse } from "../Types/auth";



const OtpVerificationPage = () => {

    const userEmail = localStorage.getItem("userEmail");
    const [otp, setOtp] = useState<string>("");
    const [isTimerExpired,setTimerExpired] = useState(false);
    const [modal, setModal] = useState(false);
    const [oauthError, setoauthError] = useState<string | null>(null);
    const dispatch = useAppDispatch();

    const navigate = useNavigate();

    const getExpiryTime = () => {
      const storedExpiryTime= localStorage.getItem("expiryTime");
      if(!isTimerExpired && storedExpiryTime){
        const ms=Number(storedExpiryTime);

        if(!Number.isNaN(ms)&& ms > Date.now()){
          return new Date(ms);
        }
      }
      const ms=new Date();
      ms.setSeconds(ms.getSeconds()+120);
      localStorage.setItem("expiryTime",ms.getTime().toString());
      return ms;
  };

    const [expiryTime] = useState(getExpiryTime);
    const {
        minutes,
        seconds,
        restart
    }=useTimer({ expiryTimestamp: expiryTime , onExpire: () => {
      localStorage.removeItem("expiryTime");
      setTimerExpired(true);
    } });


    useEffect(() => {
        if(!userEmail) {
          void navigate("/login");
          return;
        }
    },[userEmail,navigate])


    useEffect(() => {
  return () => {
    // optional: keep storage clean if you want a fresh timer next visit
    localStorage.removeItem("expiryTime");
  };
}, []);

    const requestOtp = () => {
        if(!userEmail) {
          void navigate("/login");
          return;
        }
        const resendUrl=import.meta.env.VITE_RESENDOTP_URL as string;
        axios.post<AuthApiResponse>(resendUrl, {email:userEmail},{
            withCredentials: true
        }).then((response) => {
            if (response.data.success) {
                setOtp("");
                setModal(true);
                setoauthError("OTP sent to your email");
                setTimerExpired(false);
                restart(getExpiryTime());
            } else {
                setModal(true);
                setoauthError(response.data.message ?? "An error occurred while requesting OTP");
            }
        }).catch((error) => {
            const message = axios.isAxiosError(error)
                ? (error.response?.data as AuthApiResponse)?.message ?? "Request failed"
                : "Network error";
            setModal(true);
            setoauthError(message);
        });
        
    }

    const submitOtp = (e: React.FormEvent) => {
        e.preventDefault();
        if(isTimerExpired) {
          requestOtp();
          return;
        }
        dispatch(VerifyOTP({ otp, userEmail: userEmail ?? "" }))
            .unwrap()
            .then((res)=>{
                if(res.success === true) {
                    localStorage.removeItem("expiryTime");
                    void navigate("/personal");
                    return;
                }
                else {
                    void navigate("/verify-email");
                    setModal(true);
                    setoauthError(res.message ?? "An error occurred while verifying OTP");
                    return;
                }
                
            })
            .catch(()=>{
                setModal(true);
                setoauthError("An error occurred while verifying OTP");
            }) 
        }

    const pad=(num:number)=>{
      return String(num).padStart(2,'0');
    }
    return (
        <div>
            {modal && <Modal error={oauthError} setModal={setModal} />}
            <SignUpContainer direction="column" justifyContent="center">
          <Card variant="outlined">
            <Box sx={{ textAlign: "center", mb: 1 }}>
              <Typography component="h1" sx={{
                fontFamily: "'Poppins', 'Inter', sans-serif",
                fontWeight: 700,
                fontSize: 26,
                letterSpacing: "-0.02em",
                color: "#f4f4f5",
              }}>
                Verify your email
              </Typography>
              <Typography sx={{ color: "#a1a1aa", fontSize: 14, mt: 1 }}>
                We sent a 6-digit code to{" "}
                <Box component="span" sx={{ color: "#d4d4d8" }}>{userEmail ?? "your email"}</Box>.
              </Typography>
            </Box>

            <Box
              method='post'
              component="form"
              onSubmit={submitOtp}
              sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
            >
              <OTP separator={<span>-</span>} length={6} value={otp} onChange={setOtp} />

              <Button
                type="submit"
                disabled={isTimerExpired || otp.length!==6}
                variant="contained"
                fullWidth
              >
                Verify
              </Button>
            </Box>

            <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
              Didn't get it?{" "}
              {!isTimerExpired ? (
                <Box component="span" sx={{ color: "#71717a" }}>
                  Resend in {minutes}:{pad(seconds)}
                </Box>
              ) : (
                <Box
                  component="button"
                  type="button"
                  onClick={requestOtp}
                  sx={{ background: "none", border: "none", color: "#14b8a6", cursor: "pointer", p: 0, fontSize: 13 }}
                >
                  Resend code
                </Box>
              )}
            </Typography>
            </Card>
            </SignUpContainer>
        </div>
    );
};

export default OtpVerificationPage;
