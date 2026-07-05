import { Box, Button, Divider, FormControl, FormLabel, TextField, Typography } from '@mui/material';
import React, { useEffect, useRef, useState } from 'react';
import { ValidateEmail, ValidatePassword } from '../Utils/ValidateInputs';
import { Link, useNavigate, useSearchParams } from 'react-router';
import Modal from '../Utils/Modal';
import { UserLogin } from '../Types/auth';
import loginUser from '../Redux/thunks/loginUser';
import { useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';
import validateUser from '../Redux/thunks/validateUser';
import { useAppDispatch } from '../Redux/hooks';
import Card from '../styles/card';
import SignUpContainer from '../styles/SignUpContainer';

const SocialMediaButtons = React.lazy(() => import('../Component/auth/SocialMediaButtons'));

const LoginUser = () => {
  const [emailError, setEmailError] = useState(false);
  const [emailErrorMessage, setEmailErrorMessage] = useState('');
  const [passwordError, setPasswordError] = useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const isSubmittable = !emailError && !passwordError && email.trim() !== '' && password.trim() !== '';
  const [queryParameter] = useSearchParams();
  const [modal, setModal] = useState(false);
  const [oauthError, setoauthError] = useState<string | null>(null);
  const [isLoading, setLoading] = useState(false);
  const [isValidating, setIsValidating] = useState(() => !queryParameter.get('error'));

  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { message, isError } = useSelector((state: RootState) => state.authReducer);
  const hasValidatedRef = useRef(false);

  useEffect(() => {
    if (hasValidatedRef.current) return;
    hasValidatedRef.current = true;
    const urlError = queryParameter.get('error');
    if (!urlError) {
      dispatch(validateUser())
        .unwrap()
        .then(() => navigate("/personal"))
        .catch(() => setIsValidating(false));
    } else {
      setIsValidating(false);
    }
  }, [queryParameter, dispatch, navigate]);

  useEffect(() => {
    if (!modal) return;
    const id = setTimeout(() => setModal(false), 2000);
    return () => clearTimeout(id);
  }, [modal]);

  useEffect(() => {
    const urlError = queryParameter.get('error');
    if (urlError) {
      setModal(true);
      setoauthError(urlError);
    } else if (isError && message) {
      setModal(true);
      setoauthError(message);
    }
  }, [queryParameter, isError, message]);

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (emailError || passwordError) return;
    const user: UserLogin = { email, password };
    setLoading(true);
    dispatch(loginUser(user))
      .unwrap()
      .then((res) => {
        if (res.success === false && res.data?.needEmailVerification === true) {
          if (res.data?.email) localStorage.setItem("userEmail", res.data.email);
          void navigate("/verify-email");
        } else if (res.success === true) {
          void navigate("/personal");
        } else {
          setModal(true);
          setoauthError(res.message || "Login failed");
        }
      })
      .catch(() => {
        setModal(true);
        setoauthError("Login failed. Please try again.");
      })
      .finally(() => setLoading(false));
  };

  if (isValidating) {
    return (
      <div className="min-h-screen bg-surface-page flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-brand border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className='relative'>
      {modal && <Modal error={oauthError} setModal={setModal} />}
      <SignUpContainer direction="column" justifyContent="center">
        <Card variant="outlined">
          <Box sx={{ textAlign: "center", mb: 1 }}>
            <Typography component="h1" sx={{
              fontFamily: "'Poppins', 'Inter', sans-serif",
              fontWeight: 700,
              fontSize: { xs: 26, sm: 30 },
              letterSpacing: "-0.02em",
              color: "#f4f4f5",
            }}>
              Fin<Box component="span" sx={{ color: "#009689" }}>tracker</Box>
            </Typography>
            <Typography sx={{ color: "#a1a1aa", fontSize: 14, mt: 1 }}>
              Welcome back. Sign in to your account.
            </Typography>
          </Box>
          <Box method='post' component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <FormControl>
              <FormLabel htmlFor="emailFieldId">Email</FormLabel>
              <TextField
                required
                fullWidth
                id="emailFieldId"
                placeholder="your@email.com"
                name="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  ValidateEmail(e.target.value, setEmailError, setEmailErrorMessage);
                }}
                autoComplete="email"
                variant="outlined"
                error={emailError}
                helperText={emailErrorMessage}
                color={emailError ? 'error' : 'primary'}
              />
            </FormControl>
            <FormControl>
              <FormLabel htmlFor="passwordFieldId">Password</FormLabel>
              <TextField
                required
                fullWidth
                name="password"
                placeholder="••••••"
                type="password"
                id="passwordFieldId"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  ValidatePassword(e.target.value, setPasswordError, setPasswordErrorMessage);
                }}
                autoComplete="current-password"
                variant="outlined"
                error={passwordError}
                helperText={passwordErrorMessage}
                color={passwordError ? 'error' : 'primary'}
              />
            </FormControl>
            <Button type="submit" fullWidth variant="contained" disabled={!isSubmittable || isLoading}>
              {isLoading ? "Signing In…" : "Sign In"}
            </Button>
          </Box>
          <Divider sx={{ my: 1, "&::before, &::after": { borderColor: "rgba(63,63,70,0.5)" }, color: "#71717a", fontSize: 12 }}>
            OR
          </Divider>
          <React.Suspense fallback={<div>Loading…</div>}>
            <SocialMediaButtons />
          </React.Suspense>
          <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
            No account?{" "}
            <Link to="/register" style={{ color: "#14b8a6", textDecoration: "none" }}>Sign up</Link>
          </Typography>
        </Card>
      </SignUpContainer>
    </div>
  );
};

export default LoginUser;
