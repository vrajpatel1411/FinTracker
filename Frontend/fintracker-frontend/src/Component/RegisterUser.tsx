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
import Stack from '@mui/material/Stack';
import MuiCard from '@mui/material/Card';
import { styled } from '@mui/material/styles';
import { ValidateEmail, ValidateName, ValidatePassword } from '../Utils/ValidateInputs';
import { FacebookIcon, GithubIcon, GoogleIcon } from '../Utils/CustomIcons';
import { IconButton} from '@mui/material';
import HandleOauthLogin from '../Utils/HandleOauthLogin';
import { useNavigate, useSearchParams } from 'react-router';
import Modal from '../Utils/Modal';
import User from '../Types/User';
import registerUser  from '../Redux/Reducers/registerUser';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';

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
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const {isAuthenticated,message,isError}=useSelector((state:RootState)=>state.authReducer)


  React.useEffect(()=>{
    if(isAuthenticated ){
      navigate("/home")
    }
  },[isAuthenticated]);

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
  },[queryParameter,isAuthenticated,isError,message])

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
        console.log(user);
        dispatch(registerUser(user));
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
                disabled={!isValid || nameError || emailError || passwordError}
            >
              Sign up
            </Button>
           
          </Box>
          <Divider>
            <Typography sx={{ color: 'text.secondary' }}>or</Typography>
          </Divider>
          <Box sx={{ display: 'flex', justifyContent:"center", alignItems:"center", flexDirection: 'row', gap: 2 }}>
            <IconButton   onClick={()=>HandleOauthLogin("google")}><GoogleIcon /></IconButton>
            <IconButton   onClick={()=>HandleOauthLogin("facebook")}><FacebookIcon /></IconButton>
            <IconButton onClick={()=>HandleOauthLogin("github")}><GithubIcon /></IconButton>
            </Box>
            
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