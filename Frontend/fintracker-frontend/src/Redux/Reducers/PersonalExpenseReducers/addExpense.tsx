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
            const url = `${import.meta.env.VITE_PERSONAL_EXPENSE_URL}/`;
            const res = await axios.post(url,args,{
                withCredentials: true
            });
            if(res?.data.status=="success"){
                if(res.data.data.receiptId){
                    const receiptUrl=res.data.data.receiptUrl;
                    await axios.put(receiptUrl, args.file, {
                        headers: {
                            'Content-Type': args.fileType,
                        },
                        // onUploadProgress: (progressEvent) => {
                        // if (progressEvent.total) {
                        //     const percentage = Math.round(
                        //                             (progressEvent.loaded / progressEvent.total) * 100
                        //         );
                        //     updateProgressBar(percentage);
                        // }
                    })

                }
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