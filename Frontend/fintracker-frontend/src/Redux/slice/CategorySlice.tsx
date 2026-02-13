import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { getCategories } from "../Reducers/CategoryReducers/getCategories";


export interface CategoryType {
    categoryColor: string;
    categoryName: string;
    categoryId: string;
}

interface CategoryState {
    status: boolean;
    categories: CategoryType[];
    isLoading: boolean;
    isError: boolean;
    message: string;
}

const initialState: CategoryState = {
    status: false,
    categories: [],
    isLoading: false,
    isError: false,
    message: "",
};


const categorySlice = createSlice({
    name: "category",
    initialState,
    reducers: {
        removeCategories: (state) => {
            state.status = false;
            state.categories = [];
            state.isLoading = false;
            state.isError = false;
            state.message = "";
        }
    },
    extraReducers: (builder) => {
        builder.addCase(getCategories.pending, (state) => {
            state.isLoading = true;
            state.isError = false;
            state.status = false;
            state.message = "";
        }
        );
        builder.addCase(getCategories.fulfilled, (state, action: PayloadAction<CategoryType[]>) => {
            state.isLoading = false;
            state.isError = false;
            state.status = true;
            state.categories = action.payload;
            state.message = "Categories fetched successfully";
        }
        );
        builder.addCase(getCategories.rejected, (state, action) => {
            state.isLoading = false;
            state.isError = true;
            state.status = false;
            state.categories = [];
            // Try to extract message from action.payload if possible, fallback to default
            state.message = (action.payload as { message?: string } | undefined)?.message ?? "Failed to fetch categories";
        }
        );
    }
});

export const { removeCategories } = categorySlice.actions;
export default categorySlice.reducer;
