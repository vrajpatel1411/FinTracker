import { createAsyncThunk } from "@reduxjs/toolkit";
import { User } from "../../Types/auth";
import axios from "axios";
import { AuthApiResponse } from "../../Types/auth";


const registerUser = createAsyncThunk<AuthApiResponse,User,{rejectValue:{message:string}}>(
                        "auth/registerUser",
                        async (user,{rejectWithValue}) => {
                            try{
                                const res = await axios.post<AuthApiResponse>(import.meta.env.VITE_REGISTER_URL as string, user,{
                                    withCredentials: true
                                });
                                return res.data;
                            }
                            catch(error){
                                if (axios.isAxiosError(error) && error.response) {
                                    return rejectWithValue({ message: error.message ?? "Registration failed" });
                                }
                                return rejectWithValue({ message: "Network error" });
                            }
                        }
                    )

export default registerUser;