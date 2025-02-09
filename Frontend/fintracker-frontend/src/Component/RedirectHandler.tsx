import React, { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import Oauth2Success from '../Redux/Reducers/oauth2Success';
import { useDispatch } from 'react-redux';

const RedirectHandler = () => {
    const [queryParameter]=useSearchParams()
    const [token, setToken] = React.useState<string | null>(null);
    const [error, setError] = React.useState<string | null>(null);
    const navigate=useNavigate();
    const dispatch = useDispatch();
    useEffect(()=>{
        const getToken=queryParameter.get('token');
        const getError=queryParameter.get('error');
        if(getToken){
            setToken(getToken);   
            dispatch(Oauth2Success(getToken));
        }
        if(getError){
            setError(getError)
        }
  },[queryParameter,dispatch])
  if(token){
        navigate("/home");
  }
  if(error){
      navigate("/login?error="+error);
  }
  
  return null
}

export default RedirectHandler