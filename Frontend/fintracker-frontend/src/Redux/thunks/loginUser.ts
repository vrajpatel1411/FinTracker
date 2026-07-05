import { createAsyncThunk } from "@reduxjs/toolkit";
import { UserLogin, AuthApiResponse } from "../../Types/auth";
import axios from "axios";

const loginUser = createAsyncThunk<AuthApiResponse, UserLogin, { rejectValue: { message: string } }>(
  "auth/loginUser",
  async (user, { rejectWithValue }) => {
    try {
      const res = await axios.post<AuthApiResponse>(import.meta.env.VITE_LOGIN_URL as string, user, {
        withCredentials: true,
      });
      return res.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const backendMessage = (error.response.data as AuthApiResponse)?.message || "Login failed";
        return rejectWithValue({ message: backendMessage });
      }
      return rejectWithValue({ message: "Network error. Please check your connection." });
    }
  }
);

export default loginUser;
