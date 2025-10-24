import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

const VerifyOTP = createAsyncThunk(
    "auth/verifyOTP",
    async (
        {otp,userEmail}:VerifyOTPType, 
    ) => {
        try {

            const registerUser = await axios.post(import.meta.env.VITE_VERIFYOTP_URL, {otp:otp,userEmail:userEmail},{
                withCredentials: true
            });
            return registerUser.data;

        }
        catch (error) {
            if (axios.isAxiosError(error)) {
                throw new Error(error.response?.data || "An error occurred while verifying OTP");
            } else {
                throw new Error("An unexpected error occurred");
            }       
        }
}
)

export default VerifyOTP;
export type VerifyOTPType = {
    otp: string;
    userEmail: string;
};