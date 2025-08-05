import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import Divider from '@mui/material/Divider';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import Link from '@mui/material/Link';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import { ValidateEmail, ValidateName, ValidatePassword } from '../Utils/ValidateInputs';
import { useNavigate, useSearchParams } from 'react-router';

import Modal from '../Utils/Modal';
import User from '../Types/User';
import registerUser  from '../Component/auth/Reducers/registerUser';
import { useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';
import validateUser from '../Component/auth/Reducers/validateUser';

import { useAppDispatch } from '../Redux/hooks';

const Card = React.lazy(() => import('../styles/card'));
const SignUpContainer = React.lazy(() => import('../styles/SignUpContainer'));

// import SocialMediaButtons from '../Component/auth/SocialMediaButtons';
import { AxiosError } from 'axios';
const SocialMediaButtons = React.lazy(() => import('../Component/auth/SocialMediaButtons'));



const RegisterUser = () => {

  const [emailError, setEmailError] = React.useState(false);
  const [emailErrorMessage, setEmailErrorMessage] = React.useState('');
  const [passwordError, setPasswordError] = React.useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = React.useState('');
  const [nameError, setNameError] = React.useState(false);
  const [nameErrorMessage, setNameErrorMessage] = React.useState('');
  const [isValid, setValid] = React.useState(false);
  const [isPasswordMatched, setPasswordMatched] = React.useState(false);
  const [ passwordMatchedError, setPasswordMatchedErrorMessage ] = React.useState("");
  const [queryParameter]=useSearchParams()
  const [modal, setModal] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isLoading, setLoading] = React.useState(false);

  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const {message,isError}=useSelector((state:RootState)=>state.authReducer)
  const hasValidatedRef = React.useRef(false);

  // check if user is already validated
  React.useEffect(() => {
    if (hasValidatedRef.current) return; // Ref is used to prevent multiple validations
    hasValidatedRef.current = true;

    dispatch(validateUser())
      .unwrap()
      .then((res) => {
        console.log(res);
        navigate("/home")
  
      })
      
      .catch(() => {
        // Stay on register page
      });
  }, [dispatch, navigate]);

  React.useEffect(()=>{
    const error=queryParameter.get('error');
    if(error){
      setModal(true)
      setError(error)
    }

    // Check if we have a error in global state
    if(isError){
      setModal(true)
      setError(message)
    }
  },[queryParameter,isError,message])


  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    if ( nameError || emailError || passwordError) {
      event.preventDefault();
      return;
    }
    
    const data = new FormData(event.currentTarget);

    if(data.get('password') !== data.get('confirmed-password')){
        setPasswordMatched(true);
        setPasswordMatchedErrorMessage('Passwords do not match');
    }
    else{
        setPasswordMatched(false);
        setPasswordMatchedErrorMessage('');
        const user:User =   {
          firstName: data.get('name') as string | null,
          lastName: data.get('lastName') as string | null,
          email: data.get('email') as string,
          password: data.get('password') as string,
        };
        setLoading(true);
        dispatch(registerUser(user))
          .unwrap()
          .then((res) => {
            
            if(res.status === false && res.needEmailVerification){
              navigate("/verify-email");
            }
            else if (res.status === true) {
              navigate("/home");
            }
            })
          .catch((err: AxiosError) => {
            setLoading(false);
            console.error("Register failed:", err);
          });
    }
    if(isError){
      setModal(true)
      setError(message)
    }
    event.preventDefault();
  };

 
  return (
    <div>
      {
            modal && <Modal error={error} setModal={setModal} />
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
           
            <FormControl >
              <FormLabel htmlFor="name">Full name</FormLabel>
              <TextField
                autoComplete="name"
                onChange={(e) =>ValidateName(e.target.value,setNameError,setNameErrorMessage,setValid)}
                name="name"
                required
                fullWidth
                id="name"
                placeholder="Jon Snow"
                error={nameError}
                helperText={nameErrorMessage}
                color={nameError ? 'error' : 'primary'}
              />
            </FormControl>
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
            <FormControl>
              <FormLabel htmlFor="confirmed-password">Confirmed Password</FormLabel>
              <TextField
                required
                fullWidth
                name="confirmed-password"
                placeholder="••••••"
                type="password"
                id="confirmedpassword"
                autoComplete="new-password"
                variant="outlined"
                error={isPasswordMatched}
                helperText={passwordMatchedError}
                color={isPasswordMatched ? 'error' : 'primary'}
              />
            </FormControl>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={!isValid || nameError || emailError || passwordError || isLoading}
            >
              Sign up
            </Button>
           
          </Box>
          <Divider>
            <Typography sx={{ color: 'text.secondary' }}>or</Typography>
          </Divider>
          <React.Suspense fallback={<div>Loading Social Media Buttons...</div>}>
            <SocialMediaButtons />
          </React.Suspense>
            
            <Typography sx={{ textAlign: 'center' }}>
              Already have an account?{' '}
              <Link
                href="/login"
                variant="body2"
                sx={{ alignSelf: 'center' }}
              >
                Sign in
              </Link>
            </Typography>
          {/* </Box> */}
        </Card>
      </SignUpContainer>
    </div>
  );
}

export default RegisterUser