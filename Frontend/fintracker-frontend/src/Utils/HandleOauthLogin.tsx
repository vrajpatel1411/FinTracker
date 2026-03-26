const HandleOauthLogin=(provider:string)=>{
    const baseUrl=import.meta.env.VITE_OAUTH2_URL as string
    const OAUTH2_REDIRECT_URI = import.meta.env.VITE_OAUTH2_REDIRECT_URI as string ;
    switch(provider){
        case "google":
            window.location.href=baseUrl+"google"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        case "facebook":
            window.location.href=baseUrl+"facebook"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        case "github":
            window.location.href=baseUrl+"github"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        default:
            break;
    }



}

export default HandleOauthLogin;