import AccountCircleIcon from '@mui/icons-material/AccountCircleOutlined';
import NotificationIcon from '@mui/icons-material/NotificationsNoneOutlined';


const TopBar = () => {
    return <div className="flex justify-items-end my-2 items-center
    ">
        <div>
            <h1 className="text-2xl font-bold">Fintracker</h1>
        </div>
        <div className='mr-0 ml-auto flex items-center gap-4'>
            <NotificationIcon sx={{ width:'28px' , height:'30px'}} />
            <AccountCircleIcon sx={{ width:'30px' , height:'30px'}} />
        </div>

    </div>
}

export default TopBar