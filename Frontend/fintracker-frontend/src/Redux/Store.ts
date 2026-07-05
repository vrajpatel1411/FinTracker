import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slice/AuthSlice";
import personalExpenseReducer from "./slice/PersonalExpenseSlice";
import { expenseApi } from "./api/expenseApi";

export const store = configureStore({
  reducer: {
    authReducer,
    personalExpenseReducer,
    [expenseApi.reducerPath]: expenseApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(expenseApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
