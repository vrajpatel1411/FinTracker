
import AddIcon from '@mui/icons-material/Add';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../Redux/hooks';
import { PersonalExpense } from '../../Types/PersonalExpenseListType';
import AddExpense from './AddExpense';
import { Button, ListItemIcon, ListItemText, MenuItem, MenuList, Paper, useMediaQuery } from '@mui/material';
import { setPage, setSize } from '../../Redux/slice/PersonalExpenseSlice';
import { useDeleteExpenseMutation, useGetCategoriesQuery, useGetExpensesQuery } from '../../Redux/api/expenseApi';

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(amount);
const formatDate = (iso: string) => {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  };

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
  const { data : categories=[]} = useGetCategoriesQuery();
 
  const totalPages = expensesData?.page.totalPages ?? 0;
  const totalElements = expensesData?.page.totalElements ?? 0;
  const expenseList = expensesData?.content ?? [];
  const [deleteExpenseMutation] = useDeleteExpenseMutation();
  
  const [modal,setModal]=useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const [selectedExpenseId, setSelectedExpenseId] = useState<string | null>(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
  const [isEdit,setIsEdit]=useState(false);
  const [editExpenseData,setEditExpenseData]=useState<PersonalExpense | null>(null);

  const changeModal= useCallback(() =>{
    setModal(modal=>!modal);
  },[])

  const resetEdit=useCallback(()=>{
    setIsEdit(false);
    setEditExpenseData(null);
  },[])

  const toggleMenu = useCallback((e:React.MouseEvent , expenseId: string | undefined) => {
    e.stopPropagation();
    if (!expenseId) return;
    setSelectedExpenseId(prev => (prev === expenseId ? null : expenseId));
    setDeleteConfirmId(null);
  },[])

  useEffect(() => {
    if (!selectedExpenseId) return;
    const handler = (e: MouseEvent) => {
      if(!menuRef.current?.contains(e.target as Node)){
        setSelectedExpenseId(null);
        setDeleteConfirmId(null);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [selectedExpenseId]);

  const canPrev = page > 0;
  const canNext = totalPages > 0 && page < totalPages - 1;

  const editExpense = useCallback((expense: PersonalExpense) => {
    setIsEdit(true);
    setModal(true);
    setEditExpenseData(expense);
  },[])
  const isMobile = useMediaQuery('(max-width: 640px)');
  return (
    <div className="mx-auto w-full max-w-6xl px-4">
      <AddExpense open={modal} onClose={changeModal} categories={categories} isEdit={isEdit} onExitEdit={resetEdit} expense={isEdit ? editExpenseData : null} />
      <div className="flex flex-col w-full sm:w-auto">
        <div className="-m-1.5 overflow-x-auto">
          <div className="p-1.5 min-w-full inline-block align-middle">
            <div className="border border-zinc-700 rounded-lg divide-y divide-zinc-700">
              <div className="flex flex-wrap py-3 px-4 items-center gap-3">
                <Button
                  onClick={changeModal}
                  variant="contained"
                  startIcon={<AddIcon />}
                  size="small"
                  sx={{ fontFamily: "'Poppins', 'Inter', sans-serif", letterSpacing: "0.025em" }}
                >
                  <span className="hidden sm:inline">Add Expense</span>
                  <span className="sm:hidden">Add</span>
                </Button>
                <div className="ml-auto flex items-center gap-2">
                  <label className="text-sm text-gray-400">Rows:</label>
                  <select
                    className="bg-surface-base border border-zinc-700 rounded px-2 py-1 text-sm text-white"
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
              {isMobile && (
                <div className="divide-y divide-zinc-800">
                  {isLoading && <p className="px-4 py-6 text-center text-zinc-500">Loading…</p>}
                  {!isLoading && isError && <p className="px-4 py-6 text-center text-red-400">{errorMessage}</p>}
                  {!isLoading && !isError && expenseList.length === 0 && <p className="px-4 py-6 text-center text-zinc-500">No expenses found.</p>}
                  {!isLoading && !isError && expenseList.map((expense) => (
                    <div key={expense.expenseId} className="flex items-center justify-between px-4 py-3 bg-surface-card hover:bg-surface-card-hover transition-colors">
                      <div className="flex flex-col gap-0.5 min-w-0 flex-1">
                        <span className="text-sm font-semibold text-white truncate">{expense.title}</span>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs text-zinc-500">{expense.categoryName}</span>
                          <span className="text-xs text-zinc-600">·</span>
                          <span className="text-xs text-zinc-500">{formatDate(expense.expenseDate.replace(/"/g, ''))}</span>
                          {expense.receiptId && <a href={expense.receiptUrl} className="text-xs text-receipt-yes">receipt</a>}
                        </div>
                        {expense.description && <span className="text-xs text-zinc-600 truncate">{expense.description}</span>}
                      </div>
                      <div className="flex items-center gap-3 ml-3 shrink-0">
                        <span className="text-sm font-bold text-brand-glow tabular-nums">{formatCurrency(expense.amount)}</span>
                        <div className="relative">
                          <MoreHorizIcon onClick={(e) => toggleMenu(e, expense.expenseId)} className="cursor-pointer text-zinc-400 hover:text-white" />
                          {selectedExpenseId === expense.expenseId && (
                            <Paper ref={menuRef} sx={{ width: 160, backgroundColor: '#2a2a30', position: 'absolute', mt: 1, right: 0, zIndex: 10 }}>
                              <MenuList>
                                <MenuItem sx={{ ':hover': { backgroundColor: '#3d3d45' } }} onClick={() => editExpense(expense)}>
                                  <ListItemIcon><EditOutlinedIcon fontSize="small" /></ListItemIcon>
                                  <ListItemText>Edit</ListItemText>
                                </MenuItem>
                                {deleteConfirmId === expense.expenseId ? (
                                  <MenuItem disableRipple sx={{ gap: 1, ':hover': { backgroundColor: 'transparent' } }}>
                                    <span className="text-xs text-zinc-300">Delete?</span>
                                    <button onClick={() => { void deleteExpenseMutation(expense.expenseId ?? ''); setSelectedExpenseId(null); setDeleteConfirmId(null); }} className="text-xs px-2 py-0.5 rounded bg-red-600 text-white hover:bg-red-700">Yes</button>
                                    <button onClick={() => setDeleteConfirmId(null)} className="text-xs px-2 py-0.5 rounded bg-zinc-600 text-white hover:bg-zinc-500">No</button>
                                  </MenuItem>
                                ) : (
                                  <MenuItem sx={{ ':hover': { backgroundColor: '#3d3d45' } }} onClick={() => setDeleteConfirmId(expense.expenseId ?? '')}>
                                    <ListItemIcon><DeleteOutlineIcon fontSize="small" /></ListItemIcon>
                                    <ListItemText>Delete</ListItemText>
                                  </MenuItem>
                                )}
                              </MenuList>
                            </Paper>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            {!isMobile && <div className="overflow-visible">
              <table className="min-w-full divide-y divide-zinc-700/60">
                <thead className="bg-surface-base">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider">Item</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider">Category</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider">Date</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider">Amount</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider">Receipt</th>
                    <th scope="col" className="px-6 py-3 text-start text-xs font-medium text-zinc-500 uppercase tracking-wider"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800/60">
                  {isLoading && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-zinc-500">Loading…</td>
                      </tr>
                  )}
                  {!isLoading && isError && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-red-400">{errorMessage}</td>
                      </tr>
                  )}
                  {!isLoading && !isError && expenseList.length === 0 && (
                      <tr>
                        <td colSpan={6} className="px-6 py-6 text-center text-zinc-500">No expenses found.</td>
                      </tr>
                    )}
                  {!isLoading && !isError && expenseList.map((expense)=>{
                    return (<tr key={expense.expenseId} className='bg-surface-card hover:bg-surface-card-hover transition-colors duration-200'>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800">
                      <div>
                        <div className=" text-base tracking-wide text-white">{expense.title}</div>
                        <div className=" text-sm text-gray-400">{expense.description}</div>
                      </div>
                      </td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">{expense.categoryName}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-white whitespace-nowrap">{formatDate(expense.expenseDate.replace(/"/g, ''))}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-brand-glow font-bold tabular-nums">{formatCurrency(expense.amount)}</td>
                    <td className="px-6 py-3 text-base tracking-wide text-white">
                      {expense.receiptId ? (
                        <span className="text-receipt-yes"><a href={expense?.receiptUrl}>view</a></span>
                      ) : ( <span className="text-receipt-no">No</span>)}
                    </td>
                    
                    <td className="px-6 py-3 text-base tracking-wide text-white relative " >
                      
                      <MoreHorizIcon onClick={(e) => toggleMenu(e, expense.expenseId)} className="cursor-pointer hover:text-gray-400" />
                      {selectedExpenseId === expense.expenseId && (
                        <Paper ref={menuRef} sx={{ width: 200, maxWidth: '100%', backgroundColor: '#2a2a30', position: 'absolute', mt: 1, right: 4, zIndex: 10 }}>
                          <MenuList>
                            <MenuItem sx={{ ':hover': { backgroundColor: '#3d3d45' } }} onClick={() => editExpense(expense)}>
                              <ListItemIcon>✏️</ListItemIcon>
                              <ListItemText>Edit</ListItemText>
                            </MenuItem>
                            {deleteConfirmId === expense.expenseId ? (
                              <MenuItem disableRipple sx={{ gap: 1, ':hover': { backgroundColor: 'transparent' } }}>
                                <span className="text-xs text-zinc-300">Delete?</span>
                                <button onClick={() => { void deleteExpenseMutation(expense.expenseId ?? ''); setSelectedExpenseId(null); setDeleteConfirmId(null); }} className="text-xs px-2 py-0.5 rounded bg-red-600 text-white hover:bg-red-700">Yes</button>
                                <button onClick={() => setDeleteConfirmId(null)} className="text-xs px-2 py-0.5 rounded bg-zinc-600 text-white hover:bg-zinc-500">No</button>
                              </MenuItem>
                            ) : (
                              <MenuItem sx={{ ':hover': { backgroundColor: '#3d3d45' } }} onClick={() => setDeleteConfirmId(expense.expenseId ?? '')}>
                                <ListItemIcon>🗑️</ListItemIcon>
                                <ListItemText>Delete</ListItemText>
                              </MenuItem>
                            )}
                          </MenuList>
                        </Paper>
                      )}

                    </td>
                      
                  </tr>
                  )
                })}

                </tbody>
              </table>
            </div>}
            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2 px-4 py-3 bg-surface-base z-0">
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