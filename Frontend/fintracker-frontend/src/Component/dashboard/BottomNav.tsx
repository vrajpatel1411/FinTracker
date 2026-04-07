import { NavLink } from "react-router";
import PersonIcon from '@mui/icons-material/Person';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import GroupIcon from '@mui/icons-material/Group';

const BottomNav = () => (
     <div className="fixed bottom-0 left-0 right-0 z-50 flex justify-around items-center
                    bg-[#17171c] border-t border-zinc-700 h-16 lg:hidden">
        <NavLink to="/personal" className={({ isActive }) =>
            `flex flex-col items-center gap-0.5 text-xs ${isActive ? 'text-[#009689]' : 'text-zinc-400'}`}>
            <PersonIcon fontSize="small" />
            <span>Personal Expense</span>
        </NavLink>
        <NavLink to="/receipt" className={({ isActive }) =>
            `flex flex-col items-center gap-0.5 text-xs ${isActive ? 'text-[#009689]' : 'text-zinc-400'}`}>
            <ReceiptLongIcon fontSize="small" />
            <span>Receipt</span>
        </NavLink>
        
        <NavLink to="/receipt" className={({ isActive }) =>
            `flex flex-col items-center gap-0.5 text-xs ${isActive ? 'text-[#009689]' : 'text-zinc-400'}`}>
            <GroupIcon fontSize="small" />
            <span>Group</span>
        </NavLink>
    </div>
)

export default BottomNav;