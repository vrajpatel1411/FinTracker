// PersonalExpensePage.tsx
import { useEffect } from 'react';
import PersonalExpenseDashboard from '../Component/personalexpense/PersonalExpenseDashboard';
import { useAppDispatch } from '../Redux/hooks';
import { getCategories } from '../Redux/Reducers/CategoryReducers/getCategories';
import { removeCategories } from '../Redux/slice/CategorySlice';

const PersonalExpensePage = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getCategories());

    return () => {
      dispatch(removeCategories());
    };
  }, []); // ← empty dep array: run once on mount, cleanup on unmount

  return (
    <div className="min-h-screen bg-[#0f0f14] text-white">
      <PersonalExpenseDashboard />
    </div>
  );
};

export default PersonalExpensePage;