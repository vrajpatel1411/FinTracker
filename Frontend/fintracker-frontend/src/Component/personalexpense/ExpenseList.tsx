
import AddIcon from '@mui/icons-material/Add';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { useEffect, useMemo, useState } from 'react';
import { useAppDispatch } from '../../Redux/hooks';
import getExpenses from '../../Redux/Reducers/PersonalExpenseReducers/getExpenses';
import { PersonalExpense } from '../../Types/PersonalExpenseListType';
import AddExpense from './AddExpense';



const categories = [
  { id: "food", name: "Food", icon: "🍔", color: "#22c55e" },
  { id: "travel", name: "Travel", icon: "✈️", color: "#06b6d4" },
  // ...
];
const ExpenseList = () =>
 {
  const [page, setPage] = useState(0); // 0-based
  const [size, setSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [expenseList,setExpenseList]=useState<PersonalExpense[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const dispatch = useAppDispatch();
  const [modal,setModal]=useState(false);

  const changeModal= () =>{
    setModal(!modal);
  }
  const canPrev = page > 0;
  const canNext = totalPages > 0 && page < totalPages - 1;

  const fetchExpenses = async (page: number, size: number) => {
    setIsLoading(true);
    setError(null);
    dispatch(getExpenses({ page, size }))
      .unwrap()
      .then((res) => {
        const personalExpenseData : PersonalExpense[] = res.data.content
        setSize(res.data.page.size);
        setPage(res.data.page.number);
        setTotalElements(res.data.page.totalElements);
        setTotalPages(res.data.page.totalPages);
        setExpenseList(personalExpenseData);
      })
      .catch((err) => {
        setError("Failed to fetch expenses: " + err);
        setExpenseList([]);
      })
      .finally(() => setIsLoading(false))
      ;
  };

   useEffect(() => {
    let mounted = true;
    (async () => {
      if (!mounted) return;
      await fetchExpenses(page, size);
    })();
    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  const rows = useMemo(()=>expenseList, [expenseList]);

  const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
  const formatDate = (iso: string) => {
    // Handles "2025-09-10T04:00:00.000+00:00" etc.
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };

  return (
    <div>
      <div className='flex-1 w-[25%]'>
            <AddExpense open={modal} onClose={changeModal} categories={categories}/>
      </div>
      <div className="flex flex-col">
        <div className="-m-1.5 overflow-x-auto">
          <div className="p-1.5 min-w-full inline-block align-middle">
            <div className="border border-gray-200 rounded-lg divide-y divide-gray-200">
              <div className="flex flex-row py-3 px-4 items-center gap-4">

                <div className="bg-[#121215] relative max-w-3xs border-2 border-gray-200 rounded-lg shadow-sm">
                  <label htmlFor="hs-table-search" className="sr-only">Search Expense</label>
                  <input type="text" name="hs-table-search" id="hs-table-search" className="py-1.5 sm:py-2 px-3 ps-9 block w-full border-gray-200 shadow-2xs rounded-lg sm:text-sm focus:z-10 focus:border-blue-500 focus:ring-blue-500 disabled:opacity-50 disabled:pointer-events-none" placeholder="Search Expense"/>
                  <div className="absolute inset-y-0 start-0 flex items-center pointer-events-none ps-3 text-zinc-400">
                    {/* Icon for search input */}
                    <svg className="size-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="11" cy="11" r="8"></circle>
                      <path d="m21 21-4.3-4.3"></path>
                    </svg>
                  </div>
                  
                </div>
                <div onClick={()=> changeModal()} className='p-1 bg-[#009689] rounded-lg shadow-sm font-poppins text-small tracking-wide text-white flex items-center hover:bg-[#005a52] cursor-pointer'>
                  <AddIcon/>
                    <button className=' p-1.5 '> Add Expense</button>
                </div>
                <div className="ml-auto flex items-center gap-2">
                  <label className="text-sm text-gray-400">Rows:</label>
                  <select
                    className="bg-[#121215] border border-gray-200 rounded px-2 py-1 text-sm text-white"
                    value={size}
                    onChange={(e) => {
                      const v = Number(e.target.value);
                      setPage(0);
                      setSize(Number.isFinite(v) && v > 0 ? v : 5);
                    }}
                  >
                    {[5, 10, 20].map((opt) => (
                      <option key={opt} value={opt}>{opt}</option>
                    ))}
                  </select>
                </div>
              </div>
            <div className="overflow-hidden">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-[#121215]">
                  <tr>
                    
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase">Item</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase">Category</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase">Date</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase">Amount</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase">Receipt</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-gray-400 uppercase"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {isLoading && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-gray-400">Loading…</td>
                      </tr>
                  )}
                  {!isLoading && error && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-red-400">{error}</td>
                      </tr>
                  )}
                  {!isLoading && !error && rows.length === 0 && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-gray-400">No expenses found.</td>
                      </tr>
                    )}
                  {!isLoading && !error && rows.map((expense)=>{
                    return (<tr key={expense.expenseId} className='bg-[#17171c] hover:bg-[#1f1f24] transition-colors duration-200'>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800">
                      <div>
                        <div className=" text-base tracking-wide text-white">{expense.title}</div>
                        <div className=" text-sm text-gray-400">{expense.description}</div>
                      </div>
                      </td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">{expense.categoryName}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">{formatDate(expense.expenseDate.replace(/"/g, ''))}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">{formatCurrency(expense.amount)}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">
                      {expense.receiptId ? (
                        <span className="text-green-500">Yes</span>
                      ) : ( <span className="text-red-500">No</span>)}
                    </td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">
                      <MoreHorizIcon className="cursor-pointer hover:text-gray-400" />
                    </td>
                  </tr>
                  )
                })}

                </tbody>
              </table>
            </div>
            <div className="flex items-center justify-between px-4 py-3 bg-[#121215]">
                <div className="text-sm text-gray-400">
                  Page <span className="text-white">{(page ?? 0) + 1}</span> of{" "}
                  <span className="text-white">{totalPages}</span> •{" "}
                  <span className="text-white">{totalElements}</span> results
                </div>

                <div className="flex items-center gap-2">
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => setPage(0)}
                    disabled={!canPrev}
                  >
                    First
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={!canPrev}
                  >
                    Prev
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => setPage((p) => (page!=totalPages ?Math.min(p+1,totalPages) : p))}
                    disabled={!canNext}
                  >
                    Next
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => setPage(Math.max(0, totalPages - 1))}
                    disabled={!canNext}
                  >
                    Last
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  )
}

export default ExpenseList