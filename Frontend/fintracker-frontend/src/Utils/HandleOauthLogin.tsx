const HandleOauthLogin=(provider:string)=>{
    const baseUrl=import.meta.env.VITE_OAUTH2_URL
    const OAUTH2_REDIRECT_URI = import.meta.env.VITE_OAUTH2_REDIRECT_URI ;
    switch(provider){
        case "google":
            console.log("google")
            window.location.href=baseUrl+"google"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        case "facebook":
            console.log("facebook")
            window.location.href=baseUrl+"facebook"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        case "github":
            console.log("github")
            window.location.href=baseUrl+"github"+'?redirect_uri=' + OAUTH2_REDIRECT_URI;
            break;
        default:
            break;
    }



}

export default HandleOauthLogin;