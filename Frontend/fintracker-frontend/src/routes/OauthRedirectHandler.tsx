import  { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import Oauth2Success from '../Redux/Reducers/Oauth2Success';
import { useAppDispatch } from '../Redux/hooks';

const OauthRedirectHandler = () => {
    const [queryParameter]=useSearchParams()
 
    const navigate=useNavigate();
    const dispatch = useAppDispatch();
    useEffect(()=>{

        console.log("OauthRedirectHandler: queryParameter", queryParameter.toString());
        const getToken=queryParameter.get('status');
        if(getToken=="true"){
            dispatch(Oauth2Success({status:true,message:null}));
            navigate("/personal")
        }
        else if(getToken=="false" && queryParameter.get('email')!==null){
            localStorage.setItem("userEmail", queryParameter.get('email') || '');
            dispatch(Oauth2Success({status:false,message:"Needs email verification", userEmail: queryParameter.get('email') || ''}));
            navigate("/verify-email");
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