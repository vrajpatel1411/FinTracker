import OTP from "../Component/auth/Otp";
import {  useEffect, useState } from "react";
import { Box, Button, Link, Typography } from "@mui/material";
import Card from "../styles/card";
import SignUpContainer from "../styles/SignUpContainer";
import {useTimer} from "react-timer-hook"
import { useAppDispatch } from "../Redux/hooks";
import VerifyOTP from "../Redux/Reducers/VerifyOTP";
import { useNavigate } from "react-router-dom";
import Modal from "../Utils/Modal";
import axios from "axios";


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

    // const [expiryTime, setExpireTime]= useState(getExpiryTime());
    const {
        minutes,
        seconds,
        restart
    }=useTimer({ expiryTimestamp: getExpiryTime() , onExpire: () => {
      localStorage.removeItem("expiryTime");
      setTimerExpired(true);
    } });


    useEffect(() => {
        if(!userEmail) {
          navigate("/login");
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
          navigate("/login");
          return;
        }
        axios.get(import.meta.env.VITE_RESENDOTP_URL+"?email="+userEmail, {
            withCredentials: true
        }).then((response) => {
            if (response.data.status === true) {
                setOtp("");
                setModal(true);
                setoauthError("OTP sent to your email");
                setTimerExpired(false);
                restart(getExpiryTime());
            }
            else{
                setModal(true);
                setoauthError(response.data.message || "An error occurred while requesting OTP");
            }
        })
        .catch((error) => {
            setModal(true);
            setoauthError(error.message || "An error occurred while requesting OTP");
        });
        
    }

    const submitOtp = (e: React.FormEvent) => {
        e.preventDefault();
        if(isTimerExpired) {
          requestOtp();
          return;
        }
        dispatch(VerifyOTP({ otp, userEmail: userEmail || "" }))
            .unwrap()
            .then((res)=>{
                if(res.status === true) {
                    localStorage.removeItem("expiryTime");
                    navigate("/personal");
                    return;
                }
                else if(res.status === false && res.email!== "") {
                    navigate("/verify-email");
                    setModal(true);
                    setoauthError(res.message);
                    return;
                }
                else{
                    setModal(true);
                    setoauthError(res.message || "An error occurred while verifying OTP");
                    return;
                }
            })
            .catch((err)=>{
                setModal(true);
                setoauthError(err.message || "An error occurred while verifying OTP");
            }) 
        }

    const pad=(num:number)=>{
      return String(num).padStart(2,'0');
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
              Request otp in {minutes}:{pad(seconds)}
            </Typography>):(<Typography
              component="p"
              variant="body1"
              sx={{ width: '100%', fontSize: 'clamp(1rem, 5vw, 0.75rem)' }}
            >
              OTP expired. Please request a new one <Link sx={{ cursor: 'pointer' }} onClick={requestOtp}>Here</Link>
            </Typography>)}

            <Button
                type="submit"
                disabled={isTimerExpired || otp.length!==6}
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
