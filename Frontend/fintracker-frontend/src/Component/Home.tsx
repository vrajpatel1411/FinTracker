import axios from 'axios';
import React, { useEffect } from 'react'
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router';
import { RootState } from '../Redux/Store';


const Home = () => {
    
    const [data, setData] = React.useState<string | null>(null);
    const navigate = useNavigate()
    const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
    useEffect(() => {
        if(!isAuthenticated ){
          navigate("/login")
        }
        const fetchData = async () => {
            const res= await axios.get("http://localhost:8081/personalexpense/",{
              
                withCredentials: true
              
            })
            if(res){
              console.log(res.data)
                setData(res?.data)
            }
        }
        fetchData()
    },[])


  return (
    <div>{data}</div>
  )
}

export default Home