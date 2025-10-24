import { createAsyncThunk } from "@reduxjs/toolkit";



interface Oauth2Response {
  status: boolean;
  message: string | null;
  userEmail?: string;
}

const Oauth2Success = createAsyncThunk<
  Oauth2Response,     // ✅ Return type
  Oauth2Response      // ✅ Argument type
>("auth/Oauth2Success", async ({status,message,userEmail}) => {

return {
    status: status,
    message: message,
    userEmail: userEmail

  };
  
});

export default Oauth2Success;
