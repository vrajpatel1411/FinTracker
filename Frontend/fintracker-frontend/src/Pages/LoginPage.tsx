import { Box, Button, CssBaseline, Divider, FormControl, FormLabel, Link, TextField, Typography } from '@mui/material';
import React, { useEffect, useRef } from 'react'
import { ValidateEmail, ValidatePassword } from '../Utils/ValidateInputs';

import { useNavigate, useSearchParams } from 'react-router';
import Modal from '../Utils/Modal';
import UserLogin from '../Types/UserLogin';
import loginUser  from '../Component/auth/Reducers/loginUser';
import { useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';

import validateUser from '../Component/auth/Reducers/validateUser';
import {  AxiosError, } from 'axios';
import { useAppDispatch } from '../Redux/hooks';

// import SocialMediaButtons from '../Component/auth/SocialMediaButtons';

const SocialMediaButtons = React.lazy(() => import('../Component/auth/SocialMediaButtons'));
const Card = React.lazy(() => import('../styles/card'));
const SignUpContainer = React.lazy(() => import('../styles/SignUpContainer'));

const LoginUser = () => {
  const [emailError, setEmailError] = React.useState(false);  
  const [emailErrorMessage, setEmailErrorMessage] = React.useState('');

  const [passwordError, setPasswordError] = React.useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = React.useState('');

  const [isValid, setValid] = React.useState(false);

  const [queryParameter]=useSearchParams()

  const [modal, setModal] = React.useState(false);
    
    const [oauthError, setoauthError] = React.useState<string | null>(null);
    const error=queryParameter.get('error');

    const dispatch = useAppDispatch();

    const navigate = useNavigate();

    const {message,isError}=useSelector((state:RootState)=>state.authReducer)

    const [isLoading, setLoading] = React.useState(false);

    const hasValidatedRef = useRef(false);

    useEffect(() => {
        if (hasValidatedRef.current) return;
        hasValidatedRef.current = true;
        if(!error){
          dispatch(validateUser())
            .unwrap()
            .then(() => navigate("/home"))
            .catch(() => {
              // do nothing — stay on login
            });
        }
      }, [error,dispatch, navigate,oauthError]);

    useEffect(()=>{
        if(error){
          setModal(true)
          setoauthError(error)
        }
        else if(isError && message){
          setModal(true)
          setoauthError(message)
        }
    },[error, isError, message])
    
    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      if (  emailError || passwordError) {
        
        return;
      }
      const data = new FormData(event.currentTarget);
      const user:UserLogin =   {
            
              email: data.get('email') as string,
              password: data.get('password') as string,
            };
      setLoading(true);
      dispatch(loginUser(user))
      .unwrap()
      .then((res) => {
        setLoading(false);
          if(res.status === false && res.needEmailVerification){
              navigate("/verify-email");
            }
            else if (res.status === true) {
              navigate("/home");
            }
      })
      .catch((err:AxiosError) => {
        // Optional: handle rejected promise (network error, etc.)
        console.error("Login failed:", err);
      });
      
    };
    return (
      <div className='relative'>
        {
            modal && <Modal error={oauthError} setModal={setModal} />
        }
        <CssBaseline enableColorScheme />
        <SignUpContainer direction="column" justifyContent="space-between">
          <Card variant="outlined">
            {/* <SitemarkIcon /> */}
            <Typography
              component="h1"
              variant="h4"
              sx={{ width: '100%', fontSize: 'clamp(2rem, 10vw, 2.15rem)' }}
            >
              Sign up
            </Typography>
            <Box
            method='post'
              component="form"
              onSubmit={handleSubmit}
              sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
            >
              <FormControl>
                <FormLabel htmlFor="email">Email</FormLabel>
                <TextField
                  required
                  fullWidth
                  id="emailFieldId"
                  placeholder="your@email.com"
                  onChange={(e) =>ValidateEmail(e.target.value,setEmailError,setEmailErrorMessage,setValid)}
                  name="email"
                  autoComplete="email"
                  variant="outlined"
                  error={emailError}
                  helperText={emailErrorMessage}
                  color={passwordError ? 'error' : 'primary'}
                />
              </FormControl>
              <FormControl>
                <FormLabel htmlFor="password">Password</FormLabel>
                <TextField
                  required
                  fullWidth
                  name="password"
                  placeholder="••••••"
                  type="password"
                  id="passwordFieldId"
                  onChange={(e) =>ValidatePassword(e.target.value,setPasswordError,setPasswordErrorMessage,setValid)}
                  autoComplete="new-password"
                  variant="outlined"
                  error={passwordError}
                  helperText={passwordErrorMessage}
                  color={passwordError ? 'error' : 'primary'}
                />
              </FormControl>
              <Button
                type="submit"
                fullWidth
                variant="contained"
                  disabled={!isValid  || emailError || passwordError || isLoading}
              >
                Sign up
              </Button>
              </Box>
              <Divider>
                <Typography sx={{ color: 'text.secondary' }}>or</Typography>
              </Divider>
              <React.Suspense fallback={<div>Loading Social Media Buttons...</div>}>
              <SocialMediaButtons/>  
              </React.Suspense>
              <Typography sx={{ textAlign: 'center' }}>
                  Don't have an account?{' '}
                <Link href="/register"
                  variant="body2"
                  sx={{ alignSelf: 'center' }}
                >
                  Create Here
                </Link>
              </Typography> 
          </Card>
        </SignUpContainer>
      </div>
    );
  }
  export default LoginUser;