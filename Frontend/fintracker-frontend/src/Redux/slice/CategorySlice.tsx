import { createSlice } from "@reduxjs/toolkit";
import { CategoryType } from "../../Types/category";



interface CategoryState {
    categories: CategoryType[];
}

const initialState: CategoryState = {
    categories: [],
};


const categorySlice = createSlice({
    name: "category",
    initialState,
    reducers: {
        removeCategories: (state) => {
            state.categories = [];
        }
    },
    
});

export const { removeCategories } = categorySlice.actions;
export default categorySlice.reducer;

