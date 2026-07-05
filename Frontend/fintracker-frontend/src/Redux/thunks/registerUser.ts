import { createAsyncThunk } from "@reduxjs/toolkit";
import { User, AuthApiResponse } from "../../Types/auth";
import axios from "axios";

const registerUser = createAsyncThunk<AuthApiResponse, User, { rejectValue: { message: string } }>(
  "auth/registerUser",
  async (user, { rejectWithValue }) => {
    try {
      const res = await axios.post<AuthApiResponse>(import.meta.env.VITE_REGISTER_URL as string, user, {
        withCredentials: true,
      });
      return res.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const backendMessage = (error.response.data as AuthApiResponse)?.message || "Registration failed";
        return rejectWithValue({ message: backendMessage });
      }
      return rejectWithValue({ message: "Network error. Please check your connection." });
    }
  }
);

export default registerUser;
