import AccountCircleIcon from '@mui/icons-material/AccountCircleOutlined';
import NotificationIcon from '@mui/icons-material/NotificationsNoneOutlined';
import { useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../Redux/hooks';
import { useNavigate } from 'react-router';
import { logout } from '../../Redux/slice/AuthSlice';
import LogoutIcon from '@mui/icons-material/Logout';
import { expenseApi } from '../../Redux/api/expenseApi';
import { Divider, ListItemIcon, ListItemText, Menu, MenuItem, Typography } from '@mui/material';
import logoutUser from '../../Redux/Reducers/logoutUser';


const TopBar = () => {

    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const email=useAppSelector((state) => state.authReducer.email);

    const handleOpen = (e: React.MouseEvent<SVGElement>) => {setAnchorEl(e.currentTarget as unknown as HTMLElement)};
    const handleClose = () => setAnchorEl(null);
    const handleLogout = () => {
        handleClose();
        void dispatch(logoutUser())
            .finally(() => {
                dispatch(logout());
                dispatch(expenseApi.util.resetApiState());
                void navigate("/login");
            });
    }
    return <div className="flex justify-items-end my-2 items-center
    ">
        <div>
            <h1 className="text-lg sm:text-2xl font-bold">Fintracker</h1>
        </div>
        <div className='mr-0 ml-auto flex items-center gap-4'>
            <NotificationIcon sx={{ width:'28px' , height:'30px'}} />
            <AccountCircleIcon sx={{ width:'30px' , height:'30px', cursor: "pointer"}} onClick={handleOpen}/>
            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
                slotProps={{
                    paper: {
                        sx: {backgroundColor: "#1f1f27", border: '1px solid #3f3f46', color: 'white', minWidth: 200},
                    }
                }}
                >
                    <MenuItem disabled>
                        <Typography variant="body2" sx={{color: "#9ca3af"}}>{email}</Typography>
                    </MenuItem>
                    <Divider sx={{ borderColor: '#3f3f46' }} />
                    <MenuItem onClick={handleLogout} sx={{ '&:hover': { backgroundColor: '#2a2a35' } }}>
                        <ListItemIcon><LogoutIcon sx={{ color: 'white' }} /></ListItemIcon>
                        <ListItemText>Logout</ListItemText>
                    </MenuItem>

                </Menu>
        </div>

    </div>
}

export default TopBar