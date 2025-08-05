import { useSelector } from "react-redux";
import { RootState } from "../Redux/Store";
import OTP from "../Component/auth/Otp";
import {  useState } from "react";
import { Box, Button, Link, Typography } from "@mui/material";
import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import {useTimer} from "react-timer-hook"
import { useAppDispatch } from "../Redux/hooks";
import VerifyOTP from "../Component/auth/Reducers/VerifyOTP";
import { useNavigate } from "react-router";
import Modal from "../Utils/Modal";
import axios from "axios";


const OtpVerificationPage = () => {

    const userEmail = useSelector((state:RootState)=> state.authReducer.userEmail);
    const [otp, setOtp] = useState<string>("");
    const [isTimerExpired,setTimerExpired] = useState(false);
    const [modal, setModal] = useState(false);
    const [oauthError, setoauthError] = useState<string | null>(null);
    const dispatch = useAppDispatch();

    const navigate = useNavigate();

    const getExpiryTime = () => {
    const time = new Date();
    time.setSeconds(time.getSeconds() + 2 * 60); // 2 minutes
    return time;
  };
    const {
        minutes,
        seconds,
        restart
    }=useTimer({ expiryTimestamp: getExpiryTime() , onExpire: () => setTimerExpired(true) });

    const requestOtp = () => {
        restart(getExpiryTime());

        axios.get(import.meta.env.VITE_RESENDOTP_URL+"?email="+userEmail, {
            withCredentials: true
        }).then((response) => {
            if (response.data.status === true) {
                setModal(true);
                setoauthError("OTP sent to your email");
            }
            else{
                setModal(true);
                setoauthError(response.data.message || "An error occurred while requesting OTP");
            }
        })
       
        setTimerExpired(false);
    }

    const submitOtp = (e: React.FormEvent) => {
        e.preventDefault();
        dispatch(VerifyOTP({ otp, userEmail: userEmail || "" }))
            .unwrap()
            .then((res)=>{
                if(res.status === true) {
                    navigate("/home");
                }
                if(res.status === false && res.email!== "") {
                    navigate("/verify-email");
                    setModal(true);
                    setoauthError(res.message);
                }
                else{
                    setModal(true);
                    setoauthError(res.message || "An error occurred while verifying OTP");
                }
            })
        console.log("OTP submitted:", otp);
    }
    return (
        <div>
            {modal && <Modal error={oauthError} setModal={setModal} />}
            <SignUpContainer direction="column" justifyContent="space-between">
          <Card variant="outlined">
            {/* <SitemarkIcon /> */}
            <Typography
              component="h1"
              variant="h4"
              sx={{ width: '100%', fontSize: 'clamp(2rem, 10vw, 2.15rem)' }}
            >
              OTP Verification
            </Typography>
            <Typography
              component="p"
              variant="body1"
              sx={{ width: '100%', fontSize: 'clamp(1rem, 5vw, 0.75rem)' }}
            >
              Please enter the OTP sent to your email: {userEmail}
            </Typography>
            
            <Box
            method='post'
              component="form"
              onSubmit={submitOtp}
              sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
            >
            <OTP separator={<span>-</span>} length={6} value={otp} onChange={setOtp} />
            {!isTimerExpired ?(<Typography
              component="p"
              variant="body1"
              sx={{ width: '100%', fontSize: 'clamp(1rem, 5vw, 0.75rem)' }}
            >
              Request otp in {minutes}:{seconds} minutes
            </Typography>):(<Typography
              component="p"
              variant="body1"
              sx={{ width: '100%', fontSize: 'clamp(1rem, 5vw, 0.75rem)' }}
            >
              OTP expired. Please request a new one <Link sx={{ cursor: 'pointer' }} onClick={requestOtp}>Here</Link>
            </Typography>)}

            <Button
                type="submit"
                disabled={!otp}
                variant="contained"
                sx={{width:'50%'}}>
                Submit
            </Button>
            </Box>
            </Card>
            </SignUpContainer>
        </div>
    );
};

export default OtpVerificationPage;
