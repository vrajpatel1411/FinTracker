import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

 const validateUser = createAsyncThunk(
    "auth/validateUser",
    async () => {
        try{
            const validUser = await axios.get("http://localhost:8081/userauthservice/api/auth/validate",{
                withCredentials: true
            });
            console.log(validUser.data);
            return validUser.data;
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

export default validateUser;