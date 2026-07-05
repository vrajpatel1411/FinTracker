import { useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../Redux/hooks';
import { useNavigate } from 'react-router';
import { logout } from '../../Redux/slice/AuthSlice';
import LogoutIcon from '@mui/icons-material/Logout';
import { expenseApi } from '../../Redux/api/expenseApi';
import { Divider, IconButton,  Menu, MenuItem,  } from '@mui/material';
import logoutUser from '../../Redux/thunks/logoutUser';
import NotificationsNoneOutlined from "@mui/icons-material/NotificationsNoneOutlined";


const TopBar = () => {
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const email = useAppSelector((state) => state.authReducer?.email);
  const initial = (email===' ' ? email :"U")[0]?.toUpperCase();
 
  const handleLogout = () => {
    setAnchor(null);
    void dispatch(logoutUser()).finally(() => {
      dispatch(logout());
      dispatch(expenseApi.util.resetApiState());
      void navigate("/login");
    });
  };

  return (
    <header className="h-15.5 flex items-center justify-between px-4 lg:px-7 bg-surface-subtle border-b border-zinc-800/50">
            <div className="font-display font-bold text-lg tracking-tight text-fg-1">
                Fin<span className="text-brand ">Tracker</span>
            </div>
            <div className="flex-1" />
            <div className="flex items-center gap-2">
                <IconButton size="small" aria-label="Notifications" sx={{ color: "#d4d4d8" }}>
                    <NotificationsNoneOutlined fontSize="small" />
                </IconButton>
                <IconButton
                    onClick={(e) => setAnchor(e.currentTarget)}
                    aria-label="Account menu"
                    sx={{
                        width: 36, height: 36, p: 0,
                        bgcolor: "#009689", color: "#fff",
                        fontFamily: "'Inter', sans-serif", fontSize: 14, fontWeight: 600,
                        "&:hover": { bgcolor: "#005a52" },
                    }}
                >
                    {initial}
                </IconButton>
                <Menu
                    anchorEl={anchor}
                    open={!!anchor}
                    onClose={() => setAnchor(null)}
                    anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                    transformOrigin={{ vertical: "top", horizontal: "right" }}
                    slotProps={{ paper: { sx: { minWidth: 220 } } }}
                >
                    <div className="px-3 py-2 text-xs text-fg-4">{ email===' ' ? email : "user@example.com"}</div>
                    <Divider />
                    <MenuItem onClick={handleLogout}>
                        <LogoutIcon fontSize="small" sx={{ mr: 1.5 }} /> Logout
                    </MenuItem>
                </Menu>
            </div>
        </header>
  );
};

export default TopBar;
