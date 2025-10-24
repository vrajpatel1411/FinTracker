interface AuthState {
    isAuthenticated: boolean;      
    message: string;
    isError: boolean;
    needEmailVerification: boolean; // Indicates if email verification is needed after registration
    userEmail?: string; // Optional, can be used to store user's email if needed
}

export default AuthState;