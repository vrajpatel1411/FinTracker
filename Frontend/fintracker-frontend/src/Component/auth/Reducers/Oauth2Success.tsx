import { createAsyncThunk } from "@reduxjs/toolkit";



interface Oauth2Response {
  status: boolean;
  message: string | null;
}

const Oauth2Success = createAsyncThunk<
  Oauth2Response,     // ✅ Return type
  Oauth2Response      // ✅ Argument type
>("auth/Oauth2Success", async ({status,message}) => {

return {
    status: status,
    message: message
  };
  
});

export default Oauth2Success;
