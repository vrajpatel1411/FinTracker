import {  createSlice } from "@reduxjs/toolkit";
import AuthState from "../../Types/AuthState";
import loginUser  from "../Reducers/loginUser";
import registerUser  from "../Reducers/registerUser";
import Oauth2Success from "../Reducers/Oauth2Success";
import validateUser from "../Reducers/validateUser";
import VerifyOTP from "../Reducers/VerifyOTP";


const initialState: AuthState = {
    isAuthenticated: false,
    message: '',
    isError: false,
    needEmailVerification: false,
    userEmail: ''
}

export const authSlice = createSlice({
    name:"auth",
    initialState,
    reducers:{
        logout: (state) =>{

            
            state.isAuthenticated = false;
            state.message = '';
            state.isError = false;
            state.needEmailVerification = false;
            state.userEmail = '';

        }
    },
   extraReducers: (builder) => {
    
    // REGISTER
    builder.addCase(registerUser.fulfilled, (state, action) => {
      console.log("Register action payload:", action.payload);
      if (action.payload?.status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = action.payload.message;
      } else if(action.payload?.status === false && action.payload?.needEmailVerification ) {
        state.isAuthenticated = false;
        state.isError = false;
        state.message = "Registration successful, please verify your email.";
        state.userEmail = action.payload?.email;
        state.needEmailVerification = true;
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = action.payload?.message || "Registration failed";
      }
    });
    builder.addCase(registerUser.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isError = true;
      state.message = (action.payload as { message: string })?.message || "Registration error";
    });

    // LOGIN
    builder.addCase(loginUser.fulfilled, (state, action) => {
      if (action.payload?.status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = action.payload.message;
      } else if(action.payload?.status === false && action.payload?.needEmailVerification ) {
        state.isAuthenticated = false;
        state.isError = false;
        state.message = "Registration successful, please verify your email.";
        state.userEmail = action.payload?.email;
        state.needEmailVerification = true;
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = action.payload?.message || "Login failed";
      }
    });
    builder.addCase(loginUser.rejected, (state, action) => {
      state.isAuthenticated = false;
      state.isError = true;
      state.message = (action.payload as { message: string })?.message || "Login error";
    });

    // VALIDATE
    builder.addCase(validateUser.fulfilled, (state) => {
      state.isAuthenticated = true;
      
    });
    builder.addCase(validateUser.rejected, (state) => {
      state.isAuthenticated = false;
      
    });

    // OAUTH
    builder.addCase(Oauth2Success.fulfilled, (state, action) => {
      console.log("Oauth2Success action payload:", action.payload);
      if (action.payload.status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = '';
      }else if(action.payload.status === false && action.payload.userEmail) {
        state.isAuthenticated = false;
        state.isError = false;
        state.message = "Needs email verification";
        state.userEmail = action.payload.userEmail;
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = "OAuth failed";
      }
    });


    /// VERIFY OTP
    builder.addCase(VerifyOTP.fulfilled,(state, action) => {
      console.log("VerifyOTP action payload:", action.payload);
      if (action.payload.status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = action.payload.message;
        state.needEmailVerification = false; // Reset after successful verification
      }
      else if(action.payload.status === false && action.payload.email!== null){
        state.isAuthenticated = false;
        state.isError = false;
        state.message = "OTP Verification Failed, please try again.";
        state.userEmail = action.payload.email;
        state.needEmailVerification = true;
      }
      else{
        state.isAuthenticated = false;
        state.isError = true;
        state.message = action.payload.message || "OTP Verification failed";
      }
    })
  }
});


export default authSlice.reducer;