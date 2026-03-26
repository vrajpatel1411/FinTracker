import {configureStore} from "@reduxjs/toolkit";
import authReducer from "./slice/AuthSlice";
import personalExpenseReducer from "./slice/PersonalExpenseSlice";
import categoryReducer from "./slice/CategorySlice";
import { expenseApi } from "./api/expenseApi";
export const store = configureStore({reducer:{
    authReducer: authReducer,
    personalExpenseReducer : personalExpenseReducer,
    categoryReducer: categoryReducer,
    [expenseApi.reducerPath]: expenseApi.reducer
},middleware:(getDefaultMiddleware) => getDefaultMiddleware().concat(expenseApi.middleware)});

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;

export default store;

// getDefaultMiddleware().concat(expenseApi.middleware)
// = keep thunk + serializable + immutable checks
//   + add cache management + invalidation + polling