import { Box, IconButton } from "@mui/material";
import HandleOauthLogin from "../../Utils/HandleOauthLogin";
import { FacebookIcon, GithubIcon, GoogleIcon } from "../../Utils/CustomIcons";


const SocialMediaButtons= () => {

return (<Box sx={{ display: 'flex', justifyContent:"center", alignItems:"center", flexDirection: 'row', gap: 2 }}>
            <IconButton   onClick={()=>HandleOauthLogin("google")}><GoogleIcon /></IconButton>
            <IconButton   onClick={()=>HandleOauthLogin("facebook")}><FacebookIcon /></IconButton>
            <IconButton onClick={()=>HandleOauthLogin("github")}><GithubIcon /></IconButton>
            </Box>)
            }

export default SocialMediaButtons;