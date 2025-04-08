import { createAsyncThunk } from "@reduxjs/toolkit";

const Oauth2Success = createAsyncThunk(
    "auth/Oauth2Success",
    async (status: boolean) => {
        return status;
    }    
)

export default Oauth2Success;
