import {  createSlice } from "@reduxjs/toolkit";
import AuthState from "../../Types/AuthState";
import loginUser  from "../Reducers/loginUser";
import registerUser  from "../Reducers/registerUser";
import Oauth2Success from "../Reducers/Oauth2Success";
import validateUser from "../Reducers/validateUser";


const initialState: AuthState = {
    isAuthenticated: false,
    message: '',
    isError: false,
}

export const authSlice = createSlice({
    name:"auth",
    initialState,
    reducers:{
        
    },
   extraReducers: (builder) => {
    // REGISTER
    builder.addCase(registerUser.fulfilled, (state, action) => {
      if (action.payload?.status === true) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = action.payload.message;
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
      state.isError = false;
      state.message = '';
    });
    builder.addCase(validateUser.rejected, (state,) => {
      state.isAuthenticated = false;
     
    });

    // OAUTH
    builder.addCase(Oauth2Success.fulfilled, (state, action) => {
      if (action.payload) {
        state.isAuthenticated = true;
        state.isError = false;
        state.message = '';
      } else {
        state.isAuthenticated = false;
        state.isError = true;
        state.message = "OAuth failed";
      }
    });
  }
});


export default authSlice.reducer;