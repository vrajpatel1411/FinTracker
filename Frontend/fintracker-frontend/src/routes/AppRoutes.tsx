import { Route, Routes } from 'react-router'
import RedirectRoute from './RedirectRoute'
import RedirectHandler from '../routes/RedirectRoute'
import React from 'react';
import Home from '../Pages/HomePage'
import PrivateRoute from './PrivateRoute'



const LoginUser = React.lazy(() => import('../Pages/LoginPage'));
const RegisterUser = React.lazy(() => import('../Pages/RegisterUserPage'));

const CustomRoutes = () => {
  return (
    <div>
        <Routes>
            <Route path='/oauth2/redirect' element={<RedirectHandler />} />
            <Route path="/" element={<RedirectRoute />} />
            <Route path="/register" element={<React.Suspense fallback={<div>Loading...</div>}>
                  <RegisterUser />
                </React.Suspense>} />
            
              <Route path="/login" element={
                <React.Suspense fallback={<div>Loading...</div>}>
                  <LoginUser />
                </React.Suspense>
              } />

            <Route element={<PrivateRoute/>}>
              <Route path="/home" element={<Home />} />
              
            </Route>
        </Routes>
    </div>
  )
}

export default CustomRoutes