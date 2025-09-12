//Uncomment the code below to use PrivateRoute with Redux

import {  useSelector } from "react-redux"
import { RootState } from "../Redux/Store"

// import Cookie from "js-cookie"
import { Navigate, Outlet} from "react-router"
import TopBar from "../Component/dashboard/TopBar"
import SideBar from "../Component/dashboard/SideBar"
// import validateUser from "../Redux/Reducers/validateUser"

const PrivateRoute = () => {

  // Uncomment the code below to use PrivateRoute
  const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
  
  

  // return <div className="bg-black min-h-screen text-white">
  //   <div className="sticky top-o bg-[#17171c] border-b-2
  //              border-zinc-500 z-50 text-center opacity-95 p-4">
  //     <TopBar />
  //   </div>
  //   <div className="flex flex-row ">
  //     <div className="sticky left-0 border-r-2 bg-[#17171c]
  //              border-zinc-500 w-1/7 ">
  //       <SideBar />
  //     </div>
  //     <div className="flex-1 p-4">
  //       <Outlet />
  //     </div>
  //   </div>
  // </div>

  // Uncomment the code below to use PrivateRoute
  return isAuthenticated ?(
    <div className="bg-black min-h-screen text-white">
    <div className="sticky top-o bg-[#17171c] border-b-2
               border-zinc-500 z-50 text-center opacity-95 p-4">
      <TopBar />
    </div>
    <div className="flex flex-row ">
      <div className="sticky left-0 border-r-2 bg-[#17171c]
               border-zinc-500 w-1/7 ">
        <SideBar />
      </div>
      <div className="flex-1 p-4">
        <Outlet />
      </div>
    </div>
  </div>
  ) : <Navigate to="/login" />
  
}

export default PrivateRoute