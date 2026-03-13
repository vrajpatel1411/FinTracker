import { createSlice } from "@reduxjs/toolkit";
import { AnalyticsType } from "../../Types/AnalyticsType";
import { getAnalytics } from "../Reducers/AnalyticsReducers/getAnalytics";

interface AnalyticsState{
    isLoading: boolean;
    analyticsData: AnalyticsType | null;
    error: string | null;

}


const initialState: AnalyticsState = {
    isLoading: false,
    analyticsData: null,
    error: null
}

export const analyticsSlice = createSlice({
    name: "analytics",
    initialState,
    reducers:{},
    extraReducers: (builder) => {
        builder.addCase(getAnalytics.pending, (state) => {
            state.isLoading = true;
        });
        builder.addCase(getAnalytics.fulfilled, (state, action) => {
            state.isLoading = false;
            state.analyticsData = action.payload;
            state.error = null;
        });
        builder.addCase(getAnalytics.rejected, (state, action) => {
            state.isLoading = false;
            state.analyticsData = null;
            state.error = (action.payload as { message: string })?.message || "Failed to fetch analytics";
        });

    }
})

export default analyticsSlice.reducer;