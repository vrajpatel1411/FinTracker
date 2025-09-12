import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

interface ValidateResponse {
  valid: boolean;
  message?: string;
  // add more fields if needed
}

interface RejectPayload {
  message: string;
}

const validateUser = createAsyncThunk<
  ValidateResponse,         // ✅ Return type on success
  void,                     // ✅ Argument type (no args here)
  {
    rejectValue: RejectPayload;  // ✅ Rejection error payload
  }
>(
  "auth/validateUser",
  async (_, thunkAPI) => {
    try {
      
      const response = await axios.get<ValidateResponse>(
        import.meta.env.VITE_VALIDATE_URL,
        {
          withCredentials: true,
        }
      );

      if (response.data?.valid === true) {
        return response.data;
      } else {
        return thunkAPI.rejectWithValue({
          message: response.data?.message || "Invalid session",
        });
      }
    } catch (error) {
     
      if (axios.isAxiosError(error) && error.response) {
        
        return thunkAPI.rejectWithValue({
          message: error.response.data?.error || "Session invalid",
        });
      }

      return thunkAPI.rejectWithValue({
        message: "Unknown validation error",
      });
    }
  }
);

export default validateUser;
