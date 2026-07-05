import { Stack, styled } from "@mui/material";

const SignUpContainer = styled(Stack)(({ theme }) => ({
    minHeight: "100vh",
    padding: theme.spacing(3),
    [theme.breakpoints.up("sm")]: { padding: theme.spacing(4) },
    backgroundColor: "#0f0f14",
    backgroundImage:
        "radial-gradient(circle at 20% 20%, rgba(0,150,137,0.08), transparent 40%), radial-gradient(circle at 80% 80%, rgba(86,55,135,0.08), transparent 40%)",
}));

export default SignUpContainer;
