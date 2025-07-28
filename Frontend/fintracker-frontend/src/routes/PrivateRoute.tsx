import {  useSelector } from "react-redux"
import { RootState } from "../Redux/Store"
// import Cookie from "js-cookie"
import { Navigate, Outlet} from "react-router"
// import validateUser from "../Redux/Reducers/validateUser"

const PrivateRoute = () => {
  const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
  
  

  return isAuthenticated ?(
    <Outlet/>
  ) : <Navigate to="/login" />
  
}

export default PrivateRoute