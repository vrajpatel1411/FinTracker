import PersonalExpenseDashboard from '../Component/personalexpense/PersonalExpenseDashboard';
import { useGetCategoriesQuery } from '../Redux/api/expenseApi';

const PersonalExpensePage = () => {

  useGetCategoriesQuery();

  return (
    <div className="min-h-screen bg-[#0f0f14] text-white">
      <PersonalExpenseDashboard />
    </div>
  );
};

export default PersonalExpensePage;