import {  createSlice } from "@reduxjs/toolkit";
import AuthState from "../../Types/AuthState";
import loginUser  from "../Reducers/loginUser";
import registerUser  from "../Reducers/registerUser";
import Oauth2Success from "../Reducers/Oauth2Success";


const initialState: AuthState = {
    isAuthenticated: false,
    jwtToken: '',
    message: '',
    isError: false,
}

export const authSlice = createSlice({
    name:"auth",
    initialState,
    reducers:{
        
    },
    extraReducers: (builder) => {
        builder.addCase(registerUser.pending, (state) => {
            state.isAuthenticated = false;
        })
        .addCase(registerUser.fulfilled, (state, action) => {
            if(action.payload?.status){
                state.isAuthenticated = true;
            state.jwtToken = action.payload.jwtToken;
            localStorage.setItem("jwtToken", action.payload.jwtToken);
            state.message = action.payload.message;
            state.isError = false;
            }
            else{
                state.isAuthenticated = false;
                state.isError = true;
                state.message = (action.payload as { message: string }).message || "";
            }
        })
        .addCase(registerUser.rejected, (state, action) => {
            state.isAuthenticated = false;
            state.isError = true;
            state.message = (action.payload as { message: string }).message || "";
            
        })
        .addCase(loginUser.pending, (state) => {
            state.isAuthenticated = false;
        })
        .addCase(loginUser.fulfilled, (state, action) => {
            if(action.payload?.status || action.payload?.jwtToken!==null){
                state.isAuthenticated = true;
                state.jwtToken = action.payload.jwtToken;
                localStorage.setItem("jwtToken", action.payload.jwtToken);
                state.message = action.payload.message;
                state.isError = false;
            }
            else{
                state.isAuthenticated = false;
                state.isError = true;
                state.message = (action.payload as { message: string }).message || "";
            }
        })
        .addCase(loginUser.rejected, (state, action) => {
           
            state.isAuthenticated = false;
            state.isError = true;
            state.message = (action.payload as { message: string }).message || "";
        })
        .addCase(Oauth2Success.pending, (state) => {
            state.isAuthenticated = false;
        })
        .addCase(Oauth2Success.fulfilled, (state, action) => {

           if(action.payload!=null){
            state.isAuthenticated = true;
            state.jwtToken = action.payload;
            localStorage.setItem("jwtToken", action.payload);
           }
           
        })
    }
})



export default authSlice.reducer;