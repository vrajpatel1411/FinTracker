import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { AuthApiResponse, AuthState } from "../../Types/auth";
import loginUser from "../thunks/loginUser";
import registerUser from "../thunks/registerUser";
import validateUser from "../thunks/validateUser";
import VerifyOTP from "../thunks/VerifyOTP";

const initialState: AuthState = {
  isAuthenticated: false,
  message: '',
  isError: false,
  needEmailVerification: false,
  email: '',
};

const handleAuthFulfilled = (state: AuthState, payload: AuthApiResponse) => {
  if (!payload.success && payload.data?.needEmailVerification) {
    state.isAuthenticated = false;
    state.isError = false;
    state.message = payload.message;
    state.email = payload.data.email;
  } else if (payload.success) {
    state.isAuthenticated = true;
    state.isError = false;
    state.message = payload.message;
  } else {
    state.isAuthenticated = false;
    state.isError = true;
    state.message = payload.message || "Authentication failed";
  }
};

export const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout: (state) => {
      state.isAuthenticated = false;
      state.message = '';
      state.isError = false;
      state.needEmailVerification = false;
      state.email = '';
      localStorage.removeItem('userEmail');
    },
    Oauth2Success: (state, action: PayloadAction<{ status: boolean; message: string | null; userEmail?: string }>) => {
      const { status, userEmail } = action.payload;
      if (status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = '';
      } else if (status === false && userEmail) {
        state.isAuthenticated = false;
        state.isError = false;
        state.message = "Needs email verification";
        state.email = userEmail;
        state.needEmailVerification = true;
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = "OAuth failed";
      }
    },
  },
  extraReducers: (builder) => {
    builder.addCase(registerUser.fulfilled, (state, action) => {
      handleAuthFulfilled(state, action.payload);
    });
    builder.addCase(registerUser.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isError = true;
      state.message = (action.payload as { message: string })?.message || "Registration error";
    });

    builder.addCase(loginUser.fulfilled, (state, action) => {
      handleAuthFulfilled(state, action.payload);
    });
    builder.addCase(loginUser.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isError = true;
      state.message = (action.payload as { message: string })?.message || "Login error";
    });

    builder.addCase(validateUser.fulfilled, (state) => {
      state.isAuthenticated = true;
    });
    builder.addCase(validateUser.rejected, (state) => {
      state.isAuthenticated = false;
    });

    builder.addCase(VerifyOTP.fulfilled, (state, action) => {
      if (action.payload.success) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = action.payload.message;
        state.needEmailVerification = false;
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = action.payload.message || "OTP Verification failed";
      }
    });
    builder.addCase(VerifyOTP.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isError = true;
      state.message = (action.payload as { message: string })?.message || "OTP Verification error";
      state.needEmailVerification = true;
    });
  },
});

export const { logout, Oauth2Success } = authSlice.actions;
export default authSlice.reducer;
