import { createAsyncThunk } from "@reduxjs/toolkit";
import UserLogin from "../../Types/UserLogin";
import axios from "axios";

 const loginUser = createAsyncThunk(
    "auth/loginUser",
    async (user: UserLogin) => {
        try{
            const loginUser = await axios.post("http://localhost:8081/userauthservice/api/auth/login", user);
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