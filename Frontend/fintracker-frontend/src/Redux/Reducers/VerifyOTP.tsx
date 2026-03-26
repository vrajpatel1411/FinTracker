import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { AuthApiResponse, VerifyOTPType } from "../../Types/auth";

const VerifyOTP = createAsyncThunk<AuthApiResponse, VerifyOTPType, { rejectValue: { message: string } }>(
    "auth/verifyOTP",
    async (
        {otp,userEmail},{rejectWithValue} 
    ) => {
        try {

            const registerUser = await axios.post<AuthApiResponse>(import.meta.env.VITE_VERIFYOTP_URL as string, {otp:otp,userEmail:userEmail},{
                withCredentials: true
            });
            return registerUser.data;
        }
        catch (error) {
            if (axios.isAxiosError(error))
                return rejectWithValue({ message: error.message ?? "OTP verification failed" });
            return rejectWithValue({ message: "An unexpected error occurred" });    
        }
}
)

export default VerifyOTP;