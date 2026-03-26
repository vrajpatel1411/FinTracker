// slice/personalExpenseSlice.ts
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface ExpenseQueryParams {
  page: number;
  size: number;
}

interface PersonalExpenseState {
  queryParams: ExpenseQueryParams;
}

const initialState: PersonalExpenseState = {
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
      const size= action.payload;
      state.queryParams.size = Number.isFinite(size) && size>0 ? size:5; // Ensure size is positive, default to 10
      state.queryParams.page=0; // Reset to first page when size changes
    },

    setQueryParams(state, action: PayloadAction<ExpenseQueryParams>) {
      const { page, size } = action.payload;
      state.queryParams.page = Math.max(0, page);
      state.queryParams.size = Number.isFinite(size) && size > 0 ? size : 10;
    }
  }
});

export const { setPage, setSize, setQueryParams } = personalExpenseSlice.actions;
export default personalExpenseSlice.reducer;
