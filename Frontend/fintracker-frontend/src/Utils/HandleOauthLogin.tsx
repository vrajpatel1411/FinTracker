const HandleOauthLogin=(provider:string)=>{
    const baseUrl="http://localhost:8080/oauth2/authorize/";
    const OAUTH2_REDIRECT_URI = 'http://localhost:5173/oauth2/redirect'
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