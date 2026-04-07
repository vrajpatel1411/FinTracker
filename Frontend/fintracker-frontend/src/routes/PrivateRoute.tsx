import {  useSelector } from "react-redux"
import { RootState } from "../Redux/Store"
import { Navigate, Outlet} from "react-router"
import TopBar from "../Component/dashboard/TopBar"
import SideBar from "../Component/dashboard/SideBar"
import BottomNav from "../Component/dashboard/BottomNav"

const PrivateRoute = () => {

  const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
  
  return isAuthenticated ?(
    <div className="bg-black min-h-screen text-white">
    <div className="sticky top-0 bg-[#17171c] border-b-2
               border-zinc-500 z-50 text-center opacity-95 p-4">
      <TopBar />
    </div>
    <div className="flex flex-row ">
      <div className="sticky left-0 border-r-2 bg-[#17171c]
               border-zinc-500 w-56  hidden lg:block">
        <SideBar />
      </div>
      <div className="flex-1 p-4 pb-20 lg:pb-4">
        <Outlet />
      </div>
      <BottomNav />
    </div>
  </div>
  ) : <Navigate to="/login" />
  
}

export default PrivateRoute