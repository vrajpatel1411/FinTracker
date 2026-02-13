import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { AddExpensePayload, PersonalExpense } from "../../../Types/PersonalExpenseListType";
import getExpenses from "./getExpenses";




export interface AddExpenseResponse {
    status:string,
    data: PersonalExpense
}
type ApiStatus = "success" | "failure";


export const addExpense=createAsyncThunk(
        "personalExpense/addExpense",
    async (args:AddExpensePayload,{dispatch,rejectWithValue}) => {
        try{
            console.log("Adding expense with args:", args);
            const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/`;
            const res = await axios.post(url,args,{
                withCredentials: true
            });

            console.log("Add expense response:", res.data);
            if(res?.data.status=="success"){
                dispatch(getExpenses()); // Refresh the list after adding
                return "Expense added successfully";
            }
            else{
                return rejectWithValue({
                    message: res.data.error || "Failed to add expense",
                    status: res.data.success as ApiStatus,
                });
            }
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