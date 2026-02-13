import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

export const getCategories=createAsyncThunk(
        "categories/getCategories",
        async (_, { rejectWithValue }) => {
            try{
                console.log("Getting Categories");
                const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/category/`;
                const res = await axios.get(url,{
                    withCredentials: true
                });

                if(res.data.status === "success"){
                    console.log("Categories fetched successfully:", res.data.data);
                    return res.data.data;
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