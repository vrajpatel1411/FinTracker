// src/styles/card.tsx
// REPLACES the original — uses the new dark card tokens.

import { styled } from "@mui/material";
import MuiCard from "@mui/material/Card";

const Card = styled(MuiCard)(({ theme }) => ({
    display: "flex",
    flexDirection: "column",
    alignSelf: "center",
    width: "100%",
    padding: theme.spacing(4),
    gap: theme.spacing(2),
    margin: "auto",
    backgroundColor: "#17171c",
    border: "1px solid rgba(63,63,70,0.5)",
    borderRadius: 16,
    boxShadow:
        "0 20px 50px -20px rgba(0,0,0,0.8), 0 8px 20px -8px rgba(0,0,0,0.5)",
    [theme.breakpoints.up("sm")]: { width: "450px" },
}));

export default Card;
