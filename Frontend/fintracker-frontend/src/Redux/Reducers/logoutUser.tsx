import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

const logoutUser = createAsyncThunk("auth/logout", async () => {
    await axios.post(
        import.meta.env.VITE_LOGOUT_URL as string,
        {},
        { withCredentials: true }
    );
});

export default logoutUser;