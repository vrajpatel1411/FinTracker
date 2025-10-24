import { lazy, Suspense } from 'react';
import DashBoardCards from './DashBoardCards';
import ExpenseCharts from './ExpenseCharts';
const ExpenseList = lazy(() => import("./ExpenseList"));


function ExpenseListSkeleton() {
  return (
    <div className="border rounded-lg p-4 text-gray-400">Loading expenses…</div>
  );
}




const PersonalExpenseDashboard = () => {
  
  return (
    <div className='flex flex-col gap-4 p-4 w-[100%]'>
        <div>
          <DashBoardCards />
        </div>
        <div>
          <ExpenseCharts  />
        </div>
        <Suspense fallback={<ExpenseListSkeleton />}>
            <ExpenseList  />
        </Suspense>
    </div>
  )
}

export default PersonalExpenseDashboard