import axios from 'axios';
import React, { useEffect } from 'react'
import { RootState } from '../Redux/Store';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router';

const Home = () => {
    const {isAuthenticated}=useSelector((state:RootState)=> state.authReducer)
    const [data, setData] = React.useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(()=>{
      if( localStorage.getItem("jwtToken")==null && !isAuthenticated ){

        navigate("/login")
      }
    },[isAuthenticated,navigate])

    useEffect(() => {
      
        const fetchData = async () => {
            const res= await axios.get("http://localhost:8081/personalexpense/",{
                headers:{
                    Authorization: `Bearer ${localStorage.getItem("jwtToken")}`
                }
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