import { Box, Button, CssBaseline, Divider, FormControl, FormLabel, IconButton, Link, Stack, styled, TextField, Typography } from '@mui/material';
import React, { useEffect, useRef } from 'react'
import { ValidateEmail, ValidatePassword } from '../Utils/ValidateInputs';
import HandleOauthLogin from '../Utils/HandleOauthLogin';
import { FacebookIcon, GithubIcon, GoogleIcon } from '../Utils/CustomIcons';
import MuiCard from '@mui/material/Card';
import { useNavigate, useSearchParams } from 'react-router';
import Modal from '../Utils/Modal';
import UserLogin from '../Types/UserLogin';
import loginUser  from '../Redux/Reducers/loginUser';
import { useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';

import validateUser from '../Redux/Reducers/validateUser';
import {  AxiosError, } from 'axios';
import { useAppDispatch } from '../Redux/hooks';
const Card = styled(MuiCard)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    alignSelf: 'center',
    width: '100%',
    padding: theme.spacing(3),
    gap: theme.spacing(1),
    margin: 'auto',
    boxShadow:
      'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
    [theme.breakpoints.up('sm')]: {
      width: '450px',
    },
    ...theme.applyStyles('dark', {
      boxShadow:
        'hsla(220, 30%, 5%, 0.5) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.08) 0px 15px 35px -5px',
    }),
  }));
  
  const SignUpContainer = styled(Stack)(({ theme }) => ({
    height: 'calc((1 - var(--template-frame-height, 0)) * 100dvh)',
    minHeight: '100%',
    padding: theme.spacing(1),
    [theme.breakpoints.up('sm')]: {
      padding: theme.spacing(2),
    },
    '&::before': {
      content: '""',
      display: 'block',
      position: 'absolute',
      zIndex: -1,
      inset: 0,
      backgroundImage:
        'radial-gradient(ellipse at 50% 50%, hsl(210, 100%, 97%), hsl(0, 0%, 100%))',
      backgroundRepeat: 'no-repeat',
      ...theme.applyStyles('dark', {
        backgroundImage:
          'radial-gradient(at 50% 50%, hsla(210, 100%, 16%, 0.5), hsl(220, 30%, 5%))',
      }),
    },
  }));
const LoginUser = () => {
    const [emailError, setEmailError] = React.useState(false);
    const [emailErrorMessage, setEmailErrorMessage] = React.useState('');
    const [passwordError, setPasswordError] = React.useState(false);
    const [passwordErrorMessage, setPasswordErrorMessage] = React.useState('');
    const [isValid, setValid] = React.useState(false);
    const [queryParameter]=useSearchParams()
    const [modal, setModal] = React.useState(false);
    const [oauthError, setoauthError] = React.useState<string | null>(null);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const {message,isError}=useSelector((state:RootState)=>state.authReducer)
  const hasValidatedRef = useRef(false);

useEffect(() => {
    if (hasValidatedRef.current) return;
    hasValidatedRef.current = true;

    dispatch(validateUser())
      .unwrap()
      .then(() => navigate("/home"))
      .catch(() => {
        // do nothing — stay on login
      });
  }, [dispatch, navigate]);

     React.useEffect(()=>{
        const error=queryParameter.get('error');
       
        if(error){
          setModal(true)
          setoauthError(error)
        }
        else if(isError && message){
          setModal(true)
          setoauthError(message)
        }
      },[queryParameter, isError,message])
    
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
      dispatch(loginUser(user))
    .unwrap()
    .then((res:{
      status: boolean;
      message: string;
    }) => {
     
      if (res.status === true) {
        navigate("/home");
      } else {
        // optional: show error modal if needed
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
                  id="email"
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
                  id="password"
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
                  disabled={!isValid  || emailError || passwordError}
              >
                Sign up
              </Button>
             
            </Box>
            <Divider>
              <Typography sx={{ color: 'text.secondary' }}>or</Typography>
            </Divider>
            <Box sx={{ display: 'flex', justifyContent:"center", alignItems:"center", flexDirection: 'row', gap: 2 }}>
              <IconButton   onClick={()=>HandleOauthLogin("google")}><GoogleIcon /></IconButton>
              <IconButton onClick={()=>HandleOauthLogin("facebook")}><FacebookIcon /></IconButton>
              <IconButton onClick={()=>HandleOauthLogin("github")}><GithubIcon /></IconButton>
              </Box>
              
              <Typography sx={{ textAlign: 'center' }}>
                Don't have an account?{' '}
                <Link href="/register"
                  variant="body2"
                  sx={{ alignSelf: 'center' }}
                >
                  Create Here
                </Link>
              </Typography>
            {/* </Box> */}
          </Card>
        </SignUpContainer>
      </div>
    );
  }
  
  export default LoginUser;