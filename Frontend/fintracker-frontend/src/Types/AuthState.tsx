interface AuthState {
    isAuthenticated: boolean;      
    jwtToken: string;
    message: string;
    isError: boolean;
}

export default AuthState;