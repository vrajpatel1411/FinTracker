// src/Component/dashboard/TopBar.tsx
// Drop-in replacement. Replaces the AccountCircleOutlined avatar with an
// initial badge that matches the design system. Logout via menu.

import { useState } from "react";
import { useNavigate } from "react-router";
import IconButton from "@mui/material/IconButton";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import Divider from "@mui/material/Divider";
import LogoutIcon from "@mui/icons-material/Logout";
import NotificationsNoneOutlined from "@mui/icons-material/NotificationsNoneOutlined";

// Adjust to your real selector / mutation
import { useAppSelector } from "../../store/hooks";
import { useLogoutMutation } from "../../api/authApi";

export default function TopBar() {
    const navigate = useNavigate();
    const user = useAppSelector((s) => s.auth?.user);
    const [logout] = useLogoutMutation();
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);

    const initial = (user?.email ?? "U")[0]?.toUpperCase();

    const onLogout = async () => {
        setAnchor(null);
        try { await logout().unwrap(); } catch { /* ignore */ }
        navigate("/login");
    };

    return (
        <header className="h-[62px] flex items-center justify-between px-4 lg:px-7 bg-surface-subtle border-b border-zinc-800/50">
            <div className="lg:hidden font-display font-bold text-lg tracking-tight text-fg-1">
                Fin<span className="text-brand">tracker</span>
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
                    <div className="px-3 py-2 text-xs text-fg-4">{user?.email ?? "user@example.com"}</div>
                    <Divider />
                    <MenuItem onClick={onLogout}>
                        <LogoutIcon fontSize="small" sx={{ mr: 1.5 }} /> Logout
                    </MenuItem>
                </Menu>
            </div>
        </header>
    );
}
