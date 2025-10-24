import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import PersonalExpenseResponseType from "../../../Types/PersonalExpenseListType";
type ApiStatus = "success" | "failure";
type ErrorPayload = {
  message: string;
  status: string | ApiStatus | undefined;
  code?: number;
};
type Args = {
  page?: number;
  size?: number;
};
const getExpenses = createAsyncThunk<
   PersonalExpenseResponseType,
    Args,
    {rejectValue: ErrorPayload}>(
        "personalExpense/getExpenses",
    async (args,{rejectWithValue}) => {
         try{
           const page = Math.max(0, args.page ?? 0);
            const size = Math.min(Math.max(1, args.size ?? 10), 100); // mirror server validation


            const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/?page=${page}&size=${size}`;
            const res = await axios.get(url,{
                withCredentials: true
            });

            if(res?.data.status === "success"){
                return res.data
            }
            return rejectWithValue({
                message: res.data.error ?? "Failed to fetch expenses",
                status: res.data.status,
            });
        }
        catch(err){
           if (axios.isAxiosError(err)) {
                const code = err.response?.status;
                const data = err.response?.data as Partial<{ error: string; status:ApiStatus }> | undefined;

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


export default getExpenses;