import { createAsyncThunk } from "@reduxjs/toolkit";
import User from "../../Types/User";
import axios from "axios";

 const registerUser = createAsyncThunk(
    "auth/registerUser",
    async (user: User) => {
        try{
            const registerUser = await axios.post(import.meta.env.VITE_REGISTER_URL, user,{
                withCredentials: true
            });
            return registerUser.data;
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

export default registerUser;