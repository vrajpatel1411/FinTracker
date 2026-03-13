// PersonalExpenseDashboard.tsx
import { lazy, Suspense, useEffect } from 'react';
import DashBoardCards from './DashBoardCards';
import ExpenseCharts from './ExpenseCharts';
import { ExpenseRowsSkeleton } from './DashboardCardSkeleton';
import { useSelector } from 'react-redux';
import { RootState } from '../../Redux/Store';
import { useAppDispatch } from '../../Redux/hooks';
import { getAnalytics } from '../../Redux/Reducers/AnalyticsReducers/getAnalytics';

const ExpenseList = lazy(() => import('./ExpenseList'));

function ExpenseListFallback() {
  return (
    <div className="mx-auto w-full max-w-6xl px-4">
      <div className="rounded-2xl border border-zinc-700/50 bg-[#17171c] overflow-hidden">
        {/* Toolbar skeleton */}
        <div className="flex items-center gap-3 px-5 py-4 border-b border-zinc-700/50 bg-[#13131a]">
          <div className="skeleton-shimmer h-9 w-48 rounded-lg" />
          <div className="skeleton-shimmer h-9 w-32 rounded-lg" />
          <div className="ml-auto skeleton-shimmer h-9 w-24 rounded-lg" />
        </div>
        {/* Table skeleton */}
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="bg-[#13131a] border-b border-zinc-700/50">
                {['Item', 'Category', 'Date', 'Amount', 'Receipt', ''].map((h, i) => (
                  <th key={i} className="px-6 py-3.5 text-left text-[11px] font-semibold text-zinc-500 uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-800/60">
              <ExpenseRowsSkeleton rows={5} />
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

const PersonalExpenseDashboard = () => {

  const analyticsState = useSelector((state: RootState) => state.analyticsReducer);
  const dispatch = useAppDispatch();

  const isLoading = analyticsState.isLoading;
  const todaySpending =analyticsState.analyticsData?.todayExpense || 0;
  const monthlySpending = analyticsState.analyticsData?.monthlyExpense || 0;
  const transactionCount =analyticsState.analyticsData?.totalTransactions || 0;
  const category = analyticsState.analyticsData?.category || null;
  const dailyExpenses = analyticsState.analyticsData
    ? Object.entries(analyticsState.analyticsData.dailyExpenseDTO?.dailyExpense ?? {}).map(
        ([date, amount]) => ({ date, amount: amount})
      )
    : null;

const categories = analyticsState.analyticsData
    ? Object.entries(analyticsState.analyticsData.categoriesDTO?.categories ?? {}).map(
        ([name, value]) => ({ name, value: value  })
      )
    : null;

  useEffect(() => {
    dispatch(getAnalytics());
  },[]);

  return (
    <div className="flex flex-col gap-5 py-6">
      {/* Header */}
      <div className="mx-auto w-full max-w-6xl px-4">
        <h1 className="text-xl font-bold text-white tracking-tight">Personal Expenses</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Track and manage your personal spending</p>
      </div>

      {/* Cards */}
      <DashBoardCards isLoading={isLoading} category={category} totalSpending={todaySpending} monthlySpending={monthlySpending} transactionsCount={transactionCount}  />

      {/* Charts */}
      <ExpenseCharts isLoading={isLoading} categories={categories} dailyExpenses={dailyExpenses}/>

      {/* Expense Table */}
      <Suspense fallback={<ExpenseListFallback />}>
        <ExpenseList />
      </Suspense>
    </div>
  );
};

export default PersonalExpenseDashboard;