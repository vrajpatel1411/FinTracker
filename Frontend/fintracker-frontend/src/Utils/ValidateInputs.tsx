import { Dispatch } from "react";

export const ValidateEmail = (email: string, setEmailError: Dispatch<boolean>, setEmailErrorMessage: Dispatch<string>) => {
    if (email === '' || !/\S+@\S+\.\S+/.test(email)) {
        setEmailError(true);
        setEmailErrorMessage('Email is required');
    } else {
        setEmailError(false);
        setEmailErrorMessage('');
    }
};

export const ValidatePassword = (password: string, setPasswordError: Dispatch<boolean>, setPasswordErrorMessage: Dispatch<string>) => {
    if (password === '' || password.length < 6) {
        setPasswordError(true);
        setPasswordErrorMessage('Password must be at least 6 characters long');
    } else {
        setPasswordError(false);
        setPasswordErrorMessage('');
    }
};

export const ValidateName = (name: string, setNameError: Dispatch<boolean>, setNameErrorMessage: Dispatch<string>) => {
    if (name === '') {
        setNameError(true);
        setNameErrorMessage('Name is required');
    } else {
        setNameError(false);
        setNameErrorMessage('');
    }
};