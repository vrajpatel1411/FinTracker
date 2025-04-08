interface AuthState {
    isAuthenticated: boolean;      
    message: string;
    isError: boolean;
    isValidUser: boolean;
}

export default AuthState;