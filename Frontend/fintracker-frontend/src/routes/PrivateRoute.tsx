import {  useSelector } from "react-redux"
import { RootState } from "../Redux/Store"
import { Navigate, Outlet} from "react-router"
import TopBar from "../Component/dashboard/TopBar"
import SideBar from "../Component/dashboard/SideBar"
import BottomNav from "../Component/dashboard/BottomNav"

const PrivateRoute = () => {

  const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)

  console.warn("isAuthenticated",isAuthenticated)
  // isAuthenticated ?
  return isAuthenticated ? (
    <div className="bg-surface-page min-h-screen text-white">
    <div className="sticky top-0 bg-surface-card/95 backdrop-blur-md border-b
               border-zinc-800 z-50 text-center p-4">
      <TopBar />
    </div>
    <div className="flex flex-row ">
      <div className="sticky top-[73px] self-start border-r border-zinc-800 bg-surface-card
               w-52 min-h-[calc(100vh-73px)] hidden lg:block">
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