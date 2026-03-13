import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

export const getAnalytics=createAsyncThunk(
        "analytics/getAnalytics",
        async (_, { rejectWithValue }) => {
            try{
                let date = new Date().toISOString().split('T')[0];
                const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/analytics?date=${date}`;
                const res = await axios.get(url,{
                    withCredentials: true
                });

                if(res.status === 200){
                    return res.data;
                }
                throw new Error(res.data.error || "Failed to fetch categories");
            }
            catch(err){
                if (axios.isAxiosError(err)) {
                    const code = err.response?.status;
                    const data = err.response?.data as Partial<{ error: string; status: string }> | undefined;
                    return rejectWithValue({
                    message: data?.error ?? err.message ?? "Request failed",
                    status: data?.status,
                    code,
                    });
                }
                return rejectWithValue({
                    message: err instanceof Error ? err.message : "Unknown error",
                    status: "failure",
                });
            }
        }
    )