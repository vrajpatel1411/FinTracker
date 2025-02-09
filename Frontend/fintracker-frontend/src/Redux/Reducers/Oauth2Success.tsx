import { createAsyncThunk } from "@reduxjs/toolkit";

const Oauth2Success = createAsyncThunk(
    "auth/Oauth2Success",
    async (jwtToken: string) => {
        return jwtToken;
    }    
)

export default Oauth2Success;
