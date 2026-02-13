import { useEffect } from 'react';
import PersonalExpenseDashboard from '../Component/personalexpense/PersonalExpenseDashboard';
import { useAppDispatch } from '../Redux/hooks';
import { getCategories } from '../Redux/Reducers/CategoryReducers/getCategories';
import store from '../Redux/Store';
import { removeCategories } from '../Redux/slice/CategorySlice';



const PersonalExpensePage = () => {

  
    
// uncomment the code below for allowing only authenticated users to access this page
//     const [data, setData] = React.useState<string | null>(null);
//     const navigate = useNavigate()
//   const hasValidatedRef = React.useRef(false);

// // useEffect(() => {
// //   if (hasValidatedRef.current) return;
// //   hasValidatedRef.current = true;

// //   const res=axios.get(import.meta.env.VITE_PERSONAL_EXPENSE_URL, {
// //         withCredentials: true,
// //       });

// //   res.then((res:AxiosResponse) => {
// //     if (res.status === 200) {
// //       setData(res.data);
// //     } else {
// //       navigate("/login");
// //     }
// //   })
// //     .catch(() => {
// //       navigate("/login");
// //     });
// // }, [data, navigate]);
const useDispatch=useAppDispatch();

useEffect(()=>{
  useDispatch(getCategories())

  return ()=>{
    useDispatch(removeCategories())
  }
})

  


  return (
    <div>
      <PersonalExpenseDashboard />
    </div>
  )
}

export default PersonalExpensePage