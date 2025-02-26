import { createAsyncThunk } from "@reduxjs/toolkit";
import User from "../../Types/User";
import axios from "axios";

 const registerUser = createAsyncThunk(
    "auth/registerUser",
    async (user: User) => {
        try{
            const registerUser = await axios.post("http://localhost:8081/userauthservice/api/auth/register", user);
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