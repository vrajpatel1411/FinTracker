import { createAsyncThunk } from "@reduxjs/toolkit";

const Oauth2Success = createAsyncThunk<
  boolean,     // ✅ Return type
  boolean      // ✅ Argument type
>("auth/Oauth2Success", async (status: boolean) => {
  return status;
});

export default Oauth2Success;
