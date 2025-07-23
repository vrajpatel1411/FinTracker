import  { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import Oauth2Success from '../Redux/Reducers/Oauth2Success';
import { useAppDispatch } from '../Redux/hooks';

const RedirectHandler = () => {
    const [queryParameter]=useSearchParams()
 
    const navigate=useNavigate();
    const dispatch = useAppDispatch();
    useEffect(()=>{
        const getToken=queryParameter.get('success');
        console.log("type=>",getToken)
        
        if(getToken){
       
            dispatch(Oauth2Success(true));
            navigate("/home")
        }
        else{
            console.log("error in redirect handler")
            console.log("error=>",queryParameter.get('error'))
            dispatch(Oauth2Success(false));
            navigate("/login")
        }
       

  },[])

  
  
  return null
}

export default RedirectHandler