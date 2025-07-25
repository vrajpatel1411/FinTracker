import { createAsyncThunk } from "@reduxjs/toolkit";
import UserLogin from "../../Types/UserLogin";
import axios from "axios";

 const loginUser = createAsyncThunk(
    "auth/loginUser",
    async (user: UserLogin) => {
        try{
            const loginUser = await axios.post(import.meta.env.VITE_LOGIN_URL, user,{
                withCredentials: true
            });
       
            return loginUser.data;
        }
        catch(error){
            if(axios.isAxiosError(error) && error?.response){

                return error.response.data;
            }
            else{
                const responseError={
                    message:error
                }
                return responseError;
            }
        }
    }
)

export default loginUser;