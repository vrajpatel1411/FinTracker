import axios from 'axios';
import React, { useEffect } from 'react'

const Home = () => {

    const [data, setData] = React.useState<object | null>(null);

    useEffect(() => {
        const res=  axios.get("http://localhost:8080/api/user/",{
            headers:{
                Authorization: `Bearer ${localStorage.getItem("jwtToken")}`
            }
        })
        if(res){
        console.log(res)
       
        }
    },[data])


  return (
    <div>{data}</div>
  )
}

export default Home