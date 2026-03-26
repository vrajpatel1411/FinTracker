export interface AuthState {
    isAuthenticated: boolean;
    message: string;
    isError: boolean;
    needEmailVerification: boolean;
    email?: string;
}

export type User = {
    email: string | null;
    password: string | null;
    firstName: string | null;
    lastName: string | null;
};

// UserLogin is just the login-relevant fields of User — no separate file needed
export type UserLogin = Pick<User, 'email' | 'password'>;

// Unifies LoginResponse, RegisterResponse, ResendOtpResponse — all had the same shape
export interface AuthApiResponse {
    status: boolean;
    message: string;
    needEmailVerification?: boolean;
    email?: string;
}

// Moved out of VerifyOTP.tsx — types don't belong in reducer files
export type VerifyOTPType = {
    otp: string;
    userEmail: string;
};