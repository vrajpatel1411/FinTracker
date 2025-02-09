import { Dispatch } from "react";

export const ValidateEmail = (email:string, setEmailError:Dispatch<boolean>, setEmailErrorMessage:Dispatch<string>, setValid:Dispatch<boolean>) => {

    if(email === '' || !/\S+@\S+\.\S+/.test(email)){
        setEmailError(true);
        setEmailErrorMessage('Email is required');
        setValid(false);
        return;
    }
    else{
        setEmailError(false);
        setEmailErrorMessage('');
        setValid(true);
        return;
    }
}


export const ValidatePassword = (password:string, setPasswordError:Dispatch<boolean>, setPasswordErrorMessage:Dispatch<string>, setValid:Dispatch<boolean>) => {
    
    if(password === '' || password.length < 6){
        setPasswordError(true);
        setPasswordErrorMessage('Password must be at least 6 characters long');
        setValid(false);
        return;
    }
    else{
        setPasswordError(false);
        setPasswordErrorMessage('');
        setValid(true);
        return;
    }
}

export const ValidateName = (name:string, setNameError:Dispatch<boolean>, setNameErrorMessage:Dispatch<string>, setValid:Dispatch<boolean>) => {
    
    if(name === ''){
        setNameError(true);
        setNameErrorMessage('Name is required');
        setValid(false);
        return;
    }
    else{
        setNameError(false);
        setNameErrorMessage('');
        setValid(true);
        return;
    }
}