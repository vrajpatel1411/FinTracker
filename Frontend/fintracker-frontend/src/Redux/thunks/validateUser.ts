import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

interface ValidateResponse {
  success: boolean;
  message?: string;
}

interface RejectPayload {
  message: string;
}

const validateUser = createAsyncThunk<ValidateResponse, void, { rejectValue: RejectPayload }>(
  "auth/validateUser",
  async (_, thunkAPI) => {
    try {
      const response = await axios.get<ValidateResponse>(import.meta.env.VITE_VALIDATE_URL as string, {
        withCredentials: true,
      });
      if (response.data?.success === true) {
        return response.data;
      }
      return thunkAPI.rejectWithValue({ message: response.data?.message ?? "Invalid session" });
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        return thunkAPI.rejectWithValue({ message: error.message || "Session invalid" });
      }
      return thunkAPI.rejectWithValue({ message: "Unknown validation error" });
    }
  }
);

export default validateUser;
