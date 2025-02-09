import { Route, Routes } from 'react-router'
import RedirectRoute from './RedirectRoute'
import RegisterUser from './RegisterUser'
import RedirectHandler from './RedirectHandler'
import LoginUser from './LoginUser'
import Home from './Home'

const CustomRoutes = () => {
  return (
    <div>
        <Routes>
            <Route path='/oauth2/redirect' element={<RedirectHandler />} />
            <Route path="/" element={<RedirectRoute />} />
            <Route path="/register" element={<RegisterUser />} />
            <Route path="/login" element={<LoginUser />} />
            <Route path="/home" element={<Home />} />
        </Routes>
    </div>
  )
}

export default CustomRoutes