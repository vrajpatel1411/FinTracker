import {  useSelector } from "react-redux"
import { RootState } from "../Redux/Store"
// import Cookie from "js-cookie"
import { Outlet, useNavigate } from "react-router"
// import validateUser from "../Redux/Reducers/validateUser"

const PrivateRoute = () => {
  const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
  // const dispatch = useDispatch()
  const navigate = useNavigate()
  if(!isAuthenticated ){
    navigate("/login")
  }
  
  
  return (
    <Outlet/>
  )
  
}

export default PrivateRoute