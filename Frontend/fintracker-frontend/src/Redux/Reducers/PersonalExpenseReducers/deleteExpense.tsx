import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import getExpenses from "./getExpenses";






export const deleteExpense=createAsyncThunk
<
string,
{id:string|undefined},
{rejectValue: {message:string,code?:number}}
>
(
        "personalExpense/deleteExpense",
    async (args,{dispatch,rejectWithValue}) => {
        try{
            // console.log("Adding expense with args:", args);
            const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/expense/${args.id}`;
            const res = await axios.delete(url,{
                withCredentials: true
            });

            console.log("delete expense", res.data);
            if(res?.data.status=="success" && !res?.data.error){
                dispatch(getExpenses()).unwrap; // Refresh the list after adding
                return "Expense Deleted successfully";
            }
            else{
                return rejectWithValue({
                    message: res.data.error || "Failed to Delete expense",
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