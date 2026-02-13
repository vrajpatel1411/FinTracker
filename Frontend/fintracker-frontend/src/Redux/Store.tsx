import {configureStore} from "@reduxjs/toolkit";
import authReducer from "./slice/AuthSlice";
import personalExpenseReducer from "./slice/PersonalExpenseSlice";
import categoryReducer from "./slice/CategorySlice";

export const store = configureStore({reducer:{
    authReducer: authReducer,
    personalExpenseReducer : personalExpenseReducer,
    categoryReducer: categoryReducer
}});

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;

export default store;