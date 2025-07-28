import axios, { AxiosResponse } from 'axios';
import React, { useEffect } from 'react'
// import { useDispatch, } from 'react-redux';
import { useNavigate } from 'react-router';

import validateUser from '../Component/auth/Reducers/validateUser';
import { useAppDispatch } from '../Redux/hooks';


const Home = () => {
    
    const [data, setData] = React.useState<string | null>(null);
    const navigate = useNavigate()
    const dispatch = useAppDispatch();
  const hasValidatedRef = React.useRef(false);

useEffect(() => {
  if (hasValidatedRef.current) return;
  hasValidatedRef.current = true;
       dispatch(validateUser())
    .unwrap()
    .then(() => {
      // Step 2: If valid, fetch personal data
      return axios.get(import.meta.env.VITE_PERSONAL_EXPENSE_URL, {
        withCredentials: true,
      });
    })
    .then((res:AxiosResponse) => {
      if (res.status === 200) {
        setData(res.data);
      } else {
        navigate("/login");
      }
    })
    .catch(() => {
      
      navigate("/login");
    });
    },[])


  return (
    <div>{data}</div>
  )
}

export default Home