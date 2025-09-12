// slice/personalExpenseSlice.ts
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import type PersonalExpenseResponseType from "../../Types/PersonalExpenseListType";
import getExpenses from "../Reducers/PersonalExpenseReducers/getExpenses"; 

type FetchStatus = "idle" | "loading" | "succeeded" | "failed";


interface PersonalExpenseState {
  status: FetchStatus;
  data: PersonalExpenseResponseType | null;
  isLoading: boolean;
  isError: boolean;
  message: string;
}

const initialState: PersonalExpenseState = {
  status: "idle",
  data: null,
  isLoading: false,
  isError: false,
  message: "",
};

const personalExpenseSlice = createSlice({
  name: "personalExpense",
  initialState,
  reducers: {},
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
        state.data = action.payload;
        // If your API includes a message/status field, adjust as needed:
        state.message ="Successfully fetched expenses";
      }
    );
    builder.addCase(
      getExpenses.rejected,
      (
        state,
        action: PayloadAction<{ message: string } | undefined>
      ) => {
        state.isLoading = false;
        state.isError = true;
        state.status = "failed";
        state.data = null;
        state.message = action.payload?.message ?? "Failed to fetch expenses";
      }
    );
  },
});

export default personalExpenseSlice.reducer;
