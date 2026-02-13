// slice/personalExpenseSlice.ts
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import type PersonalExpenseResponseType from "../../Types/PersonalExpenseListType";
import getExpenses from "../Reducers/PersonalExpenseReducers/getExpenses"; 
import { addExpense} from "../Reducers/PersonalExpenseReducers/addExpense";
import { deleteExpense } from "../Reducers/PersonalExpenseReducers/deleteExpense";
import { editExpense } from "../Reducers/PersonalExpenseReducers/editExpense";

type FetchStatus = "idle" | "loading" | "succeeded" | "failed";


interface ExpenseQueryParams {
  page: number;
  size: number;
}

interface PersonalExpenseState {
  status: FetchStatus;
  data: PersonalExpenseResponseType | null;
  isLoading: boolean;
  isError: boolean;
  message: string;
  queryParams: ExpenseQueryParams;
}

const initialState: PersonalExpenseState = {
  status: "idle",
  data: null,
  isLoading: false,
  isError: false,
  message: "",
  queryParams: {
    page: 0,
    size: 10,
  }
};

const personalExpenseSlice = createSlice({
  name: "personalExpense",
  initialState,
  reducers: {
    setPage(state, action: PayloadAction<number>) {
      state.queryParams.page = Math.max(0, action.payload); // Ensure page is not negative
    },

    setSize(state, action: PayloadAction<number>) {
      let size= action.payload;
      state.queryParams.size = Number.isFinite(size) && size>0 ? size:5; // Ensure size is positive, default to 10
      state.queryParams.page=0; // Reset to first page when size changes
    },

    setQueryParams(state, action: PayloadAction<ExpenseQueryParams>) {
      const { page, size } = action.payload;
      state.queryParams.page = Math.max(0, page);
      state.queryParams.size = Number.isFinite(size) && size > 0 ? size : 10;
    }
  }
  ,
  extraReducers: (builder) => {
    builder.addCase(getExpenses.pending, (state) => {
      state.isLoading = true;
      state.isError = false;
      state.status = "loading";
      state.message = "";
    });

    // If your thunk returns PersonalExpenseResponseType on success:
    builder.addCase(
      getExpenses.fulfilled,
      (state, action: PayloadAction<PersonalExpenseResponseType>) => {
        state.isLoading = false;
        state.isError = false;
        state.status = "succeeded";
        console.log("Get expenses fulfilled with payload:", action.payload.data);
        state.data = action.payload;
        // If your API includes a message/status field, adjust as needed:
        state.message ="Successfully fetched expenses";
      }
    );
    builder.addCase(
      getExpenses.rejected,
      (
        state,
        action
      ) => {
        state.isLoading = false;
        state.isError = true;
        state.status = "failed";
        state.data = null;
        state.message = action.payload?.message ?? "Failed to fetch expenses";
      }
    );
    builder.addCase(
      addExpense.pending,(state)=>{
        state.isLoading=true;
      });
    builder.addCase(
      addExpense.fulfilled,(state,action : PayloadAction<string>)=>{
        state.isLoading=false;
        state.isError=false;
        state.message=action.payload;
      });
    builder.addCase(
      deleteExpense.pending,(state)=>{
        state.isLoading=true;
      });
    builder.addCase(
      deleteExpense.fulfilled,(state,action : PayloadAction<string>)=>{
        state.isLoading=false;
        state.isError=false;
        state.message=action.payload;
    });
    builder.addCase(
      addExpense.rejected,(state,action)=>{
        state.isLoading=false;
        state.isError=true;
        state.message=
          action.error.message ??
          "Failed to delete expense"; 
      }
    )
    builder.addCase(
      editExpense.pending,(state)=>{
        state.isLoading=true;
      });
    builder.addCase(
      editExpense.fulfilled,(state,action : PayloadAction<string>)=>{
        state.isLoading=false;
        state.isError=false;
        state.message=action.payload;
    });
    builder.addCase(
      editExpense.rejected,(state,action)=>{
        state.isLoading=false;
        state.isError=true;
        state.message=
          action.error.message ??
          "Failed to update expense"; 
      }
    )
  },
});

export const { setPage, setSize, setQueryParams } = personalExpenseSlice.actions;
export default personalExpenseSlice.reducer;
