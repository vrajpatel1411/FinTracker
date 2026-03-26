
import AddIcon from '@mui/icons-material/Add';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { useEffect,useRef, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../Redux/hooks';
import { PersonalExpense } from '../../Types/PersonalExpenseListType';
import AddExpense from './AddExpense';
import { ListItemIcon, ListItemText, MenuItem, MenuList, Paper } from '@mui/material';
import { setPage, setSize } from '../../Redux/slice/PersonalExpenseSlice';
import { useDeleteExpenseMutation, useGetCategoriesQuery, useGetExpensesQuery } from '../../Redux/api/expenseApi';

const ExpenseList = () =>
{
  const page = useAppSelector((state)=>state.personalExpenseReducer.queryParams.page) ?? 0;
  const size = useAppSelector((state)=>state.personalExpenseReducer.queryParams.size) ?? 10; 
  const dispatch = useAppDispatch();
  const { data: expensesData, isLoading, isError, error } = useGetExpensesQuery({ page, size });
  const errorMessage = isError
    ? 'status' in (error ?? {})
        ? (error as { data?: { error?: string } })?.data?.error ?? "Failed to fetch expenses"
        : "Failed to fetch expenses"
    : "";
  const { data: categories = [] } = useGetCategoriesQuery();
  const totalPages = expensesData?.page.totalPages ?? 0;
  const totalElements = expensesData?.page.totalElements ?? 0;
  const expenseList = expensesData?.content ?? [];
  const [deleteExpenseMutation] = useDeleteExpenseMutation();
  
  const [modal,setModal]=useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const iconRef = useRef<SVGSVGElement | null>(null);
  const [selectedExpenseId, setSelectedExpenseId] = useState<string | null>(null);
  const [isEdit,setIsEdit]=useState(false);
  const [editExpenseData,setEditExpenseData]=useState<PersonalExpense | null>(null);
  const changeModal= () =>{
    setModal(!modal);
  }
  const resetEdit=()=>{
    setIsEdit(false);
    setEditExpenseData(null);
  }
  const toggleMenu = (expenseId: string | undefined) => {
    if (!expenseId) return;
    setSelectedExpenseId(prev => (prev === expenseId ? null : expenseId));
  };
  useEffect(() => {
  if (!selectedExpenseId) return;
  const handler = (e: MouseEvent) => {
    const target = e.target as Node;
    const clickedInsideMenu = menuRef.current?.contains(target);
    const clickedOnIcon = iconRef.current?.contains(target);
    if (!clickedInsideMenu && !clickedOnIcon) {
      setSelectedExpenseId(null);
    }
  };
  document.addEventListener("mousedown", handler);
  return () => document.removeEventListener("mousedown", handler);
}, [selectedExpenseId]);

  const canPrev = page > 0;
  const canNext = totalPages > 0 && page < totalPages - 1;

  const editExpense = (expense: PersonalExpense) => {
    setIsEdit(true);
    setModal(true);
    setEditExpenseData(expense);
  }
  
  
  const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
  const formatDate = (iso: string) => {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };
  return (
    <div>
      <div className='flex-1 w-[25%]'>
            <AddExpense open={modal} onClose={changeModal} categories={categories} isEdit={isEdit} onExitEdit={resetEdit} expense={isEdit ? editExpenseData : null} />
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
                    <svg className="size-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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
                      dispatch(setSize(Number(e.target.value)));
                    }}
                  >
                    {[5, 10, 20].map((opt) => (
                      <option key={opt} value={opt}>{opt}</option>
                    ))}
                  </select>
                </div>
              </div>
            <div className="overflow-visible">
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
                  {!isLoading && isError && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-red-400">{errorMessage}</td>
                      </tr>
                  )}
                  {!isLoading && !isError && expenseList.length === 0 && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-gray-400">No expenses found.</td>
                      </tr>
                    )}
                  {!isLoading && !isError && expenseList.map((expense)=>{
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
                        <span className="text-green-500"><a href={expense?.receiptUrl}>view</a></span>
                      ) : ( <span className="text-red-500">No</span>)}
                    </td>
                    
                    <td className="px-6 py-3 text-base tracking-wide text-white relative " >
                      
                      <MoreHorizIcon ref={iconRef} onClick={()=>toggleMenu(expense?.expenseId)}
                        className="cursor-pointer hover:text-gray-400" 
                      />

                      { 
                        selectedExpenseId===expense.expenseId && (
                        <Paper ref={menuRef} sx={{ width:320,maxWidth: '100%',backgroundColor:'#2a2a30',position:'absolute',mt:1,right:4,rounded:2,zIndex:10 }}>
                          <MenuList>
                            <MenuItem sx={{":hover":{
                              backgroundColor:'#3d3d45',
                            }, rounded:2}} onClick={()=>editExpense(expense)}>
                              <ListItemIcon>
                                ✏️
                              </ListItemIcon>
                              <ListItemText>Edit</ListItemText>
                            </MenuItem>
                            <MenuItem sx={{":hover":{
                              backgroundColor:'#3d3d45',
                            }, rounded:2}} onClick={()=>void deleteExpenseMutation(expense.expenseId ?? '')}>
                              <ListItemIcon>
                                🗑️
                              </ListItemIcon>
                              <ListItemText>Delete</ListItemText>
                            </MenuItem>
                          </MenuList>
                            
                        </Paper>
                        )
                      }

                    </td>
                      
                  </tr>
                  )
                })}

                </tbody>
              </table>
            </div>
            <div className="flex items-center justify-between px-4 py-3 bg-[#121215] z-0">
                <div className="text-sm text-gray-400">
                  Page <span className="text-white">{(page ?? 0) + 1}</span> of{" "}
                  <span className="text-white">{totalPages}</span> •{" "}
                  <span className="text-white">{totalElements}</span> results
                </div>

                <div className="flex items-center gap-2">
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => dispatch(setPage(0))}
                    disabled={!canPrev}
                  >
                    First
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => {
                      const prevpage = Math.max(0, page - 1);
                      dispatch(setPage(prevpage));
                    }}
                    disabled={!canPrev}
                  >
                    Prev
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => 
                      {
                        const nextpage = page!==totalPages ?Math.min(page+1,totalPages-1) : page;
                        dispatch(setPage(nextpage));
                      }
                    }
                    disabled={!canNext}
                  >
                    Next
                  </button>
                  <button
                    className="px-3 py-1 rounded border border-gray-600 text-gray-200 disabled:opacity-50"
                    onClick={() => {
                        const lastpage = Math.max(0, totalPages - 1);
                        dispatch(setPage(lastpage));
                      }
                    }

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