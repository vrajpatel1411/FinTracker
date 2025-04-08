import  { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import Oauth2Success from '../Redux/Reducers/Oauth2Success';
import { useDispatch } from 'react-redux';

const RedirectHandler = () => {
    const [queryParameter]=useSearchParams()
 
    const navigate=useNavigate();
    const dispatch = useDispatch();
    useEffect(()=>{
        const getToken=queryParameter.get('success');
        console.log(getToken)
        
        if(getToken){
       
            dispatch(Oauth2Success(true));
            navigate("/home")
        }
        else{
            dispatch(Oauth2Success(false));
            navigate("/login")
        }
       

  },[])

  
  
  return null
}

export default RedirectHandler