import { Route, Routes } from 'react-router'
import RedirectRoute from './RedirectRoute'
import RegisterUser from './RegisterUser'
import RedirectHandler from './RedirectHandler'
import LoginUser from './LoginUser'
import Home from './Home'
import PrivateRoute from './PrivateRoute'

const CustomRoutes = () => {
  return (
    <div>
        <Routes>
            <Route path='/oauth2/redirect' element={<RedirectHandler />} />
            <Route path="/" element={<RedirectRoute />} />
            <Route path="/register" element={<RegisterUser />} />
            <Route path="/login" element={<LoginUser />} />
            <Route element={<PrivateRoute/>}>
              <Route path="/home" element={<Home />} />
              
            </Route>
        </Routes>
    </div>
  )
}

export default CustomRoutes