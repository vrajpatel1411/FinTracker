import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

 const validateUser = createAsyncThunk(
  "auth/validateUser",
  async (_, thunkAPI) => {
    try {
      const response = await axios.get("http://localhost:8081/userauthservice/api/auth/validate", {
        withCredentials: true,
      });

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
          message: error.response.data?.message || "Session invalid",
        });
      }

      return thunkAPI.rejectWithValue({
        message: "Unknown validation error",
      });
    }
  }
);

export default validateUser;