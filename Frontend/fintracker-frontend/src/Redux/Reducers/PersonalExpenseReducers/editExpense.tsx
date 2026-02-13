import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import getExpenses from "./getExpenses";
import { AddExpensePayload } from "../../../Types/PersonalExpenseListType";






export const editExpense=createAsyncThunk
<
string,
AddExpensePayload,
{rejectValue: {message:string,code?:number}}
>
(
    "personalExpense/editExpense",
    async (args,{dispatch,rejectWithValue}) => {
        try{
            // console.log("Adding expense with args:", args);
            const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/expense/${args.expenseId}`;
            const res = await axios.patch(url,args,{
                withCredentials: true
            });

            console.log("Updated expense", res.data);
            if(res?.data.status=="success" && !res?.data.error){
                dispatch(getExpenses()).unwrap; // Refresh the list after adding
                return "Expense Updated successfully";
            }
            else{
                return rejectWithValue({
                    message: res.data.error || "Failed to Update expense",
                });
            }
        }
        catch(err){

              if (axios.isAxiosError(err)) {
                const code = err.response?.status;
                const data = err.response?.data as Partial<{ error: string }> | undefined;
                return rejectWithValue({
                message: data?.error ?? err.message ?? "Request failed",
                code,
                });
            }

            return rejectWithValue({
                message: err instanceof Error ? err.message : "Unknown error",
            });
        }
    }
    )