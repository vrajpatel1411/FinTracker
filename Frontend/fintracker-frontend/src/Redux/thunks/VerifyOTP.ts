import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { AuthApiResponse, VerifyOTPType } from "../../Types/auth";

const VerifyOTP = createAsyncThunk<AuthApiResponse, VerifyOTPType, { rejectValue: { message: string } }>(
  "auth/verifyOTP",
  async ({ otp, userEmail }, { rejectWithValue }) => {
    try {
      const res = await axios.post<AuthApiResponse>(
        import.meta.env.VITE_VERIFYOTP_URL as string,
        { otp, userEmail },
        { withCredentials: true }
      );
      return res.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const backendMessage = (error.response.data as AuthApiResponse)?.message ?? "OTP verification failed";
        return rejectWithValue({ message: backendMessage });
      }
      return rejectWithValue({ message: "An unexpected error occurred" });
    }
  }
);

export default VerifyOTP;
