import { createAsyncThunk} from "@reduxjs/toolkit";
import UserLogin from "../../Types/UserLogin";
import axios from "axios";
import { AuthApiResponse } from "../../Types/auth";




const loginUser = createAsyncThunk<AuthApiResponse,UserLogin, {rejectValue: {message: string}}>
                (
                    "auth/loginUser",
                    async (user,{ rejectWithValue }) => {
                        try{
                            const res = await axios.post<AuthApiResponse>(import.meta.env.VITE_LOGIN_URL as string, user,{
                                withCredentials: true
                            });
                    
                            return res.data;
                        }
                        catch(error){
                            if(axios.isAxiosError(error) && error.response){

                                return rejectWithValue({ message: error.message ?? "Login failed" });
                            }
                            else{
                                return rejectWithValue({ message: "Network error" });
                            }
                        }
                    }
            )

export default loginUser;