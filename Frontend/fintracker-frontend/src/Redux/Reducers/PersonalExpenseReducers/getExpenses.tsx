import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import PersonalExpenseResponseType from "../../../Types/PersonalExpenseListType";


type ApiStatus = "success" | "failure";

export type ErrorPayload = {
  message: string;
  status: string | ApiStatus | undefined;
  code?: number;
};

const getExpenses = createAsyncThunk<
    PersonalExpenseResponseType,
    void,
    { rejectValue: ErrorPayload }
>(
        "personalExpense/getExpenses",
    async (_,{rejectWithValue,getState}) => {
         try{
            let state=getState() as any;
            console.log("Current query params in state:", state.personalExpenseReducer
            ?.queryParams);
            let page = state.personalExpenseReducer.queryParams?.page || 0;
            let size = state.personalExpenseReducer.queryParams?.size || 10;
            console.log("Fetching expenses with page:", page, "and size:", size);
            page = Math.max(0, page);
            size = Math.min(Math.max(1, size), 100); // mirror server validation


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