import  { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import Oauth2Success from '../Component/auth/Reducers/Oauth2Success';
import { useAppDispatch } from '../Redux/hooks';

const OauthRedirectHandler = () => {
    const [queryParameter]=useSearchParams()
 
    const navigate=useNavigate();
    const dispatch = useAppDispatch();
    useEffect(()=>{
        const getToken=queryParameter.get('status');
        if(getToken=="true"){
            dispatch(Oauth2Success({status:true,message:null}));
            navigate("/home")
        }
        else{
            dispatch(Oauth2Success({status:false, message: queryParameter.get('error') || "OAuth failed"}));
            const url="/login?error="+(encodeURIComponent(queryParameter.get('error') || "OAuth failed"));
            navigate(url)
        }
  },[queryParameter, dispatch, navigate])

  
  
  return null
}

export default OauthRedirectHandler