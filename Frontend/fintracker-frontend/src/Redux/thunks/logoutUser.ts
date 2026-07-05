import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

const logoutUser = createAsyncThunk<void, void, { rejectValue: { message: string } }>(
  "auth/logout",
  async (_, { rejectWithValue }) => {
    try {
      await axios.post(import.meta.env.VITE_LOGOUT_URL as string, {}, { withCredentials: true });
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        return rejectWithValue({ message: error.message || "Logout failed" });
      }
      return rejectWithValue({ message: "Network error during logout" });
    }
  }
);

export default logoutUser;
