import { Route, Routes } from 'react-router'
import RedirectRoute from './RedirectRoute'
import OauthRedirectHandler from '../routes/OauthRedirectHandler'
import React from 'react';
import PersonalExpensePage from '../Pages/PersonalExpensePage'
import PrivateRoute from './PrivateRoute'
import OtpVerificationPage from '../Pages/OtpVerificationPage';



const LoginUser = React.lazy(() => import('../Pages/LoginPage'));
const RegisterUser = React.lazy(() => import('../Pages/RegisterUserPage'));

const CustomRoutes = () => {
  return (
    <div>
        <Routes>
          <Route path="/verify-email" element={<OtpVerificationPage />} />
            <Route path='/oauth2/redirect' element={<OauthRedirectHandler />} />
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
              <Route path="/personal" element={<PersonalExpensePage />} />
              
            </Route>
        </Routes>
    </div>
  )
}

export default CustomRoutes