import  { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { useAppDispatch } from '../Redux/hooks';
import { Oauth2Success } from '../Redux/slice/AuthSlice';

const OauthRedirectHandler = () => {
    const [queryParameter]=useSearchParams()
    const navigate=useNavigate();
    const dispatch = useAppDispatch();
    useEffect(()=>{
        const getToken=queryParameter.get('status');
        if(getToken==="true"){
            dispatch(Oauth2Success({status:true,message:null}));
            void navigate("/personal")
        }
        else if(getToken==="false" && queryParameter.get('email')!==null){
            localStorage.setItem("userEmail", queryParameter.get('email') ?? '');
            dispatch(Oauth2Success({status:false,message:"Needs email verification", userEmail: queryParameter.get('email') ?? ''}));
            void navigate("/verify-email");
        }
        else{
            dispatch(Oauth2Success({status:false, message: queryParameter.get('error') ?? "OAuth failed"}));
            // const url="/login?error="+(encodeURIComponent(queryParameter.get('error') || "OAuth failed"));
            void navigate("/login")
        }
  },[queryParameter, dispatch, navigate])

  
  
  return null
}

export default OauthRedirectHandler