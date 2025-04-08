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
    isValidUser: false,
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
            console.log(action.payload)
            if(action.payload?.status ){
                state.isAuthenticated = true;
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
        .addCase(validateUser.pending, (state) => {
            state.isAuthenticated = false;
        })
        .addCase(validateUser.fulfilled, (state, action) => {
            if(action.payload?.valid){
                state.isAuthenticated = true;
                state.isValidUser = true;
            }
            else{
                state.isAuthenticated = false;
               state.isValidUser = false;
            }
        })
        .addCase(validateUser.rejected, (state, action) => {
            state.isAuthenticated = false;
            state.isError = true;
            state.message = (action.payload as { message: string }).message || "";
        })
        .addCase(Oauth2Success.pending, (state) => {
            state.isAuthenticated = false;
        })
        .addCase(Oauth2Success.fulfilled, (state, action) => {

           if(action.payload!=null && action.payload){
            state.isAuthenticated = true;
           }
           else{
            state.isAuthenticated = false;
           }
           
        })
    }
})



export default authSlice.reducer;