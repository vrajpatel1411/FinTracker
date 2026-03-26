import { useEffect } from "react"
import { useNavigate } from "react-router"


const RedirectRoute = () => {
    const navigate = useNavigate()
    useEffect(()=>{
        void navigate('/register')
    },)

  return (
    <div>Redirecting to Register Page ... </div>
  )
}

export default RedirectRoute