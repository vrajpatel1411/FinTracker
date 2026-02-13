import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { AddExpensePayload, PersonalExpense } from "../../Types/PersonalExpenseListType";

import { addExpense } from "../../Redux/Reducers/PersonalExpenseReducers/addExpense";
import { useAppDispatch } from "../../Redux/hooks";
import { CategoryType } from "../../Redux/slice/CategorySlice";
import { editExpense } from "../../Redux/Reducers/PersonalExpenseReducers/editExpense";

function classNames(...c: (string | false | null | undefined)[]) {
  return c.filter(Boolean).join(" ");
}

interface AddExpenseProps {
  open: boolean;
  onClose: () => void;
  categories: CategoryType[];
  currency?: string; // default "CAD"
  defaultDate?: string;  // ISO "YYYY-MM-DD" (fallback: today)
  isEdit: Boolean;
  onExitEdit?:()=>void;
  expense:PersonalExpense | null; 
}

function AddExpense({
  open,
  onClose,
  categories,
  currency = "CAD",
  defaultDate,
  isEdit,
  onExitEdit,
  expense
}: AddExpenseProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState(0);
  const [category, setCategory] = useState("");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [isReceipt, setIsReceipt] = useState(false);
  const [receipt, setReceipt] = useState("" as String | null);

  const dispatch = useAppDispatch();

  const titleRef = useRef<HTMLInputElement | null>(null);
  const valid = Boolean(amount!=0 && category && title);

  
  const resetAndClose= ()=>{
    setAmount(0);
    setCategory("");
    setDate(defaultDate || new Date().toISOString().slice(0, 10));
    setTitle("");
    setDescription("");
    setIsReceipt(false);
    setReceipt("");
    onExitEdit?.();
    onClose();
  }
  useEffect(() => {
    if (open && titleRef.current) {
      requestAnimationFrame(() => titleRef.current?.focus());
    }
  }, [open]);

  useEffect(()=>{
    if(isEdit && expense){
      setTitle(expense.title);
      setDescription(expense.description ?? "");
      setAmount(expense.amount);
      setCategory(expense.categoryId);
      setDate(expense.expenseDate.replace(/"/g, ""));
      setIsReceipt(expense.isReceipt);
      setReceipt(expense.receiptUrl);
    }

  }, [open,isEdit, expense]);

  // Close on ESC
  useEffect(() => {
    if (!open) return;
   const onKey = (e: KeyboardEvent) => {
    if (e.key === "Escape") resetAndClose();
  };  
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  // Prevent body scroll when open
  useEffect(() => {
    if (!open) return;
    const original = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = original;
    };
  }, [open]);

  const submit = () => {
    if (!valid) return;
    // onClose?.();
    // Reset

    const expenseData:AddExpensePayload = {
      ...(isEdit && expense ? { expenseId: expense.expenseId } : {}),
      title:title ?? "",
      amount:amount ?? 0,
      categoryId: category ?? "",
      expenseDate: date,
      description: description ?? "",
      isReceipt: isReceipt,
    }
    if(isEdit){
      console.log("Editing expense:", expenseData);
      dispatch(editExpense(expenseData));
      onExitEdit?.();
    }
    else{
      console.log("Adding expense:", expenseData);
      dispatch(addExpense(expenseData));
    }
    // dispatch(addExpense(expenseData));

    setAmount(0);
    setCategory("");
    setDate(defaultDate || new Date().toISOString().slice(0, 10));
    setTitle("");
    setDescription("");
    setIsReceipt(false);
    setReceipt("");
    onClose();
    
  };


  // Animation variants
  const overlayVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1 },
    exit: { opacity: 0 },
  };

  // Slide up on mobile (items-end), scale+fade on md+ (items-center)
  const panelVariants = {
    hidden: { opacity: 0, y: 24, scale: 0.98 },
    visible: { opacity: 1, y: 0, scale: 1 },
    exit: { opacity: 0, y: 24, scale: 0.98 },
  };

  return (
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
          {/* Overlay */}
          <motion.button
            aria-label="Close add expense"
            className="absolute inset-0 bg-black/30 backdrop-blur-sm"
            onClick={resetAndClose}
            initial="hidden"
            animate="visible"
            exit="exit"
            variants={overlayVariants}
            transition={{ duration: 0.45, ease: "easeOut" }}
          />

          {/* Panel */}
          <motion.div
            role="dialog"
            aria-modal="true"
            aria-labelledby="add-expense-title"
            className="relative w-full max-w-lg rounded-t-3xl bg-white p-6 shadow-2xl md:rounded-3xl dark:bg-zinc-900"
            initial="hidden"
            animate="visible"
            exit="exit"
            variants={panelVariants}
            transition={{ type: "spring", stiffness: 180, damping: 24, mass: 0.9}}
          >
            <div className="mb-4 flex items-center justify-between">
              <h3 id="add-expense-title" className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                Add Expense
              </h3>
              <button
                onClick={resetAndClose}
                className="rounded-lg px-2 py-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              >
                ✕
              </button>
            </div>

            {/* form */}
            <div className="grid grid-cols-1 gap-4">
              {/* Title */}
              <div>
                <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Title</label>
                <input
                  ref={titleRef}
                  value={title?? ""}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g., Lunch at Subway"
                  className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                />
              </div>

              {/* Amount + Date */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
                    Amount ({currency})
                  </label>
                  <input
                    type="number"
                    inputMode="decimal"
                    value={amount??0}
                    onChange={(e) => setAmount(e.target.valueAsNumber)}
                    placeholder="0.00"
                    className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                  />
                </div>
                <div>
                  <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Date</label>
                  <input
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value.replace(/"/g, ""))}
                    className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                  />
                </div>
              </div>

              {/* Categories grid */}
                <div>
                <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Category</label>
                <div className="mt-1 grid grid-cols-3 gap-2 sm:grid-cols-4">
                  {categories.map((c) => (
                  <button
                    key={c.categoryId}
                    type="button"
                    onClick={() => setCategory(c.categoryId)}
                    className={classNames(
                    "flex flex-col items-center justify-center gap-1 rounded-xl border px-2 py-2 text-sm transition-colors w-full focus:outline-none overflow-hidden",
                    category === c.categoryId
                      ? "border-teal-600 text-teal-600 dark:border-teal-400 dark:text-teal-300"
                      : "border-zinc-200 text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800"
                    )}
                    style={{
                    backgroundColor: c.categoryColor + "30",
                    ...(category === c.categoryId
                      ? { boxShadow: "0 0 0 2px #14b8a6" }
                      : {}),
                    }}
                    aria-pressed={category === c.categoryId}
                  >
                    <span className="text-xs font-medium truncate">{c.categoryName}</span>
                  </button>
                  ))}
                </div>
                </div>

              {/* Notes */}
              <div>
                <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Notes</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Optional"
                  className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                />
              </div>

              {/* Receipt */}
              <label className="inline-flex items-center gap-2 text-sm text-zinc-700 dark:text-zinc-300">
                <input
                  type="checkbox"
                  checked={isReceipt}
                  onChange={(e) => setIsReceipt(e.target.checked)}
                />
                I have a receipt
              </label>

              {/* Actions */}
              <div className="mt-2 flex justify-end gap-2">
                <button
                  onClick={resetAndClose}
                  className="rounded-xl border border-zinc-300 bg-white px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-300"
                >
                  Cancel
                </button>
                <button
                  onClick={submit}
                  disabled={!valid}
                  className={classNames(
                    "rounded-xl px-4 py-2 text-sm font-semibold text-white shadow-sm",
                    valid ? "bg-teal-600 hover:bg-teal-700" : "bg-teal-300"
                  )}
                >
                  Save Expense
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}

export default AddExpense;
