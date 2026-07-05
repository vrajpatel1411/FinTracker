import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { ValidateEmail, ValidateName, ValidatePassword } from '../Utils/ValidateInputs';
import { Link, useNavigate, useSearchParams } from 'react-router';
import Modal from '../Utils/Modal';
import registerUser from '../Redux/thunks/registerUser';
import { useSelector } from 'react-redux';
import { RootState } from '../Redux/Store';
import validateUser from '../Redux/thunks/validateUser';
import { useAppDispatch } from '../Redux/hooks';
import { User } from '../Types/auth';
import Card from '../styles/card';
import SignUpContainer from '../styles/SignUpContainer';

const SocialMediaButtons = React.lazy(() => import('../Component/auth/SocialMediaButtons'));

const RegisterUser = () => {
  const [emailError, setEmailError] = React.useState(false);
  const [emailErrorMessage, setEmailErrorMessage] = React.useState('');
  const [passwordError, setPasswordError] = React.useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = React.useState('');
  const [nameError, setNameError] = React.useState(false);
  const [nameErrorMessage, setNameErrorMessage] = React.useState('');
  const [name, setName] = React.useState('');
  const [email, setEmail] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [confirmPassword, setConfirmPassword] = React.useState('');
  const [confirmPasswordError, setConfirmPasswordError] = React.useState('');
  const isSubmittable = !nameError && !emailError && !passwordError
    && name.trim() !== '' && email.trim() !== '' && password.trim() !== '';
  const [queryParameter] = useSearchParams();
  const [modal, setModal] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isLoading, setLoading] = React.useState(false);
  const [isValidating, setIsValidating] = React.useState(true);

  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { message, isError } = useSelector((state: RootState) => state.authReducer);
  const hasValidatedRef = React.useRef(false);

  React.useEffect(() => {
    if (hasValidatedRef.current) return;
    hasValidatedRef.current = true;
    dispatch(validateUser())
      .unwrap()
      .then(() => navigate("/personal"))
      .catch(() => setIsValidating(false));
  }, [dispatch, navigate]);

  React.useEffect(() => {
    if (!modal) return;
    const id = setTimeout(() => setModal(false), 2000);
    return () => clearTimeout(id);
  }, [modal]);

  React.useEffect(() => {
    const urlError = queryParameter.get('error');
    if (urlError) { setModal(true); setError(urlError); }
    else if (isError) { setModal(true); setError(message); }
  }, [queryParameter, isError, message]);

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (nameError || emailError || passwordError) return;

    if (password !== confirmPassword) {
      setConfirmPasswordError('Passwords do not match');
      return;
    }
    setConfirmPasswordError('');

    const user: User = {
      firstName: name,
      lastName: null,
      email,
      password,
    };
    setLoading(true);
    dispatch(registerUser(user))
      .unwrap()
      .then((res) => {
        if (res.success === false && res.data?.needEmailVerification === true) {
          localStorage.setItem("userEmail", res.data?.email ?? '');
          void navigate("/verify-email");
        } else if (res.success === true) {
          void navigate("/personal");
        } else {
          setModal(true);
          setError(res.message || "Registration failed");
        }
      })
      .catch(() => {
        setModal(true);
        setError("Registration failed. Please try again.");
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
    <div className="relative">
      {modal && <Modal error={error} setModal={setModal} />}
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
              Create your account.
            </Typography>
          </Box>
          <Box method='post' component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <FormControl>
              <FormLabel htmlFor="name">Full name</FormLabel>
              <TextField
                autoComplete="name"
                value={name}
                onChange={(e) => { setName(e.target.value); ValidateName(e.target.value, setNameError, setNameErrorMessage); }}
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
                value={email}
                onChange={(e) => { setEmail(e.target.value); ValidateEmail(e.target.value, setEmailError, setEmailErrorMessage); }}
                name="email"
                autoComplete="email"
                variant="outlined"
                error={emailError}
                helperText={emailErrorMessage}
                color={emailError ? 'error' : 'primary'}
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
                value={password}
                onChange={(e) => { setPassword(e.target.value); ValidatePassword(e.target.value, setPasswordError, setPasswordErrorMessage); }}
                autoComplete="new-password"
                variant="outlined"
                error={passwordError}
                helperText={passwordErrorMessage}
                color={passwordError ? 'error' : 'primary'}
              />
            </FormControl>
            <FormControl>
              <FormLabel htmlFor="confirmedpassword">Confirm Password</FormLabel>
              <TextField
                required
                fullWidth
                name="confirmed-password"
                placeholder="••••••"
                type="password"
                id="confirmedpassword"
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); if (confirmPasswordError) setConfirmPasswordError(''); }}
                autoComplete="new-password"
                variant="outlined"
                error={!!confirmPasswordError}
                helperText={confirmPasswordError}
                color={confirmPasswordError ? 'error' : 'primary'}
              />
            </FormControl>
            <Button type="submit" fullWidth variant="contained" disabled={!isSubmittable || isLoading}>
              {isLoading ? "Signing up…" : "Sign up"}
            </Button>
          </Box>
          <Divider sx={{ my: 1, "&::before, &::after": { borderColor: "rgba(63,63,70,0.5)" }, color: "#71717a", fontSize: 12 }}>
            OR
          </Divider>
          <React.Suspense fallback={<div>Loading…</div>}>
            <SocialMediaButtons />
          </React.Suspense>
          <Typography sx={{ textAlign: "center", color: "#a1a1aa", fontSize: 13, mt: 1 }}>
            Already registered?{" "}
            <Link to="/login" style={{ color: "#14b8a6", textDecoration: "none" }}>Sign in</Link>
          </Typography>
        </Card>
      </SignUpContainer>
    </div>
  );
};

export default RegisterUser;
