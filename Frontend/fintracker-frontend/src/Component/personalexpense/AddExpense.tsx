// import { useEffect, useRef, useState } from "react";
// import { AnimatePresence, motion } from "framer-motion";
// import { AddExpensePayload, PersonalExpense } from "../../Types/PersonalExpenseListType";

// import { addExpense } from "../../Redux/Reducers/PersonalExpenseReducers/addExpense";
// import { useAppDispatch } from "../../Redux/hooks";
// import { CategoryType } from "../../Redux/slice/CategorySlice";
// import { editExpense } from "../../Redux/Reducers/PersonalExpenseReducers/editExpense";

// function classNames(...c: (string | false | null | undefined)[]) {
//   return c.filter(Boolean).join(" ");
// }

// interface AddExpenseProps {
//   open: boolean;
//   onClose: () => void;
//   categories: CategoryType[];
//   currency?: string; // default "CAD"
//   defaultDate?: string;  // ISO "YYYY-MM-DD" (fallback: today)
//   isEdit: Boolean;
//   onExitEdit?:()=>void;
//   expense:PersonalExpense | null; 
// }

// function AddExpense({
//   open,
//   onClose,
//   categories,
//   currency = "CAD",
//   defaultDate,
//   isEdit,
//   onExitEdit,
//   expense
// }: AddExpenseProps) {
//   const [title, setTitle] = useState("");
//   const [description, setDescription] = useState("");
//   const [amount, setAmount] = useState(0);
//   const [category, setCategory] = useState("");
//   const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
//   const [isReceipt, setIsReceipt] = useState(false);
//   const [receipt, setReceipt] = useState<File | null>(null);
//   const [receiptPreview, setReceiptPreview] = useState<string | undefined>();

//   const dispatch = useAppDispatch();

//   const titleRef = useRef<HTMLInputElement | null>(null);
//   const valid = Boolean(amount!=0 && category && title);

  
//   const resetAndClose= ()=>{
//     setAmount(0);
//     setCategory("");
//     setDate(defaultDate || new Date().toISOString().slice(0, 10));
//     setTitle("");
//     setDescription("");
//     setIsReceipt(false);
//     setReceiptPreview("");
//     onExitEdit?.();
//     onClose();
//   }
//   useEffect(() => {
//     if (open && titleRef.current) {
//       requestAnimationFrame(() => titleRef.current?.focus());
//     }
//   }, [open]);

//   useEffect(()=>{
//     if(isEdit && expense){
//       setTitle(expense.title);
//       setDescription(expense.description ?? "");
//       setAmount(expense.amount);
//       setCategory(expense.categoryId);
//       setDate(expense.expenseDate.replace(/"/g, ""));
//       if(expense.receiptId!=null){
//       setIsReceipt(true);
//       }
//       setReceipt(expense.receiptUrl? new File([], expense.receiptUrl) : null); // Placeholder File for edit mode
//       console.log("Expense in edit mode:", expense.receiptUrl);
//       setReceiptPreview(expense.receiptUrl);
//     }

//   }, [open,isEdit, expense]);

//   // Close on ESC
//   useEffect(() => {
//     if (!open) return;
//    const onKey = (e: KeyboardEvent) => {
//     if (e.key === "Escape") resetAndClose();
//   };  
//     window.addEventListener("keydown", onKey);
//     return () => window.removeEventListener("keydown", onKey);
//   }, [open, onClose]);

//   // Prevent body scroll when open
//   useEffect(() => {
//     if (!open) return;
//     const original = document.body.style.overflow;
//     document.body.style.overflow = "hidden";
//     return () => {
//       document.body.style.overflow = original;
//     };
//   }, [open]);

//   const handleReceiptFile = (file: File | undefined) => {
//     if (!file) {
//       setReceipt(null);
//       setReceiptPreview(undefined);
//       return;
//     }
//     setReceipt(file); // raw File for upload
//     setReceiptPreview(URL.createObjectURL(file)); // preview only
//   };

//   const submit = () => {
//     if (!valid) return;
    

//     let expenseData:AddExpensePayload = {
//       ...(isEdit && expense ? { expenseId: expense.expenseId } : {}),
//       title:title ?? "",
//       amount:amount ?? 0,
//       categoryId: category ?? "",
//       expenseDate: date,
//       description: description ?? "",
//       isReceipt: isReceipt,
//       // receipt: receipt ?? "",
//     }
//     if(isReceipt && receipt){
//         let fileName = receipt.name;
//         let fileType = receipt.type;
//         let fileLength = receipt.size;
//         expenseData={
//           ...expenseData,
//           fileName,
//           fileType,
//           fileLength,
//           file:receipt
//         }
//     }
  

//     //  const formData = new FormData();
//     //   if (isEdit && expense && expense.expenseId) formData.append("expenseId", expense.expenseId);
//     //   formData.append("title", title);
//     //   formData.append("amount", String(amount));
//     //   formData.append("categoryId", category);
//     //   formData.append("expenseDate", date);
//     //   formData.append("description", description ?? "");
//     //   formData.append("hasReceipt", String(isReceipt));
      
//       if (isEdit) {
//         dispatch(editExpense(expenseData));
//         onExitEdit?.();
//       } else {
//         dispatch(addExpense(expenseData));
//       }
//     // if(isEdit){
//     //   console.log("Editing expense:", expenseData);
//     //   dispatch(editExpense(expenseData));
//     //   onExitEdit?.();
//     // }
//     // else{
//     //   console.log("Adding expense:", expenseData);
//     //   dispatch(addExpense(expenseData));
//     // }
//     // dispatch(addExpense(expenseData));

//     setAmount(0);
//     setCategory("");
//     setDate(defaultDate || new Date().toISOString().slice(0, 10));
//     setTitle("");
//     setDescription("");
//     setIsReceipt(false);
//     setReceipt(null);
//     onClose();
    
//   };


//   // Animation variants
//   const overlayVariants = {
//     hidden: { opacity: 0 },
//     visible: { opacity: 1 },
//     exit: { opacity: 0 },
//   };

//   // Slide up on mobile (items-end), scale+fade on md+ (items-center)
//   const panelVariants = {
//     hidden: { opacity: 0, y: 24, scale: 0.98 },
//     visible: { opacity: 1, y: 0, scale: 1 },
//     exit: { opacity: 0, y: 24, scale: 0.98 },
//   };

//   return (
//     <AnimatePresence>
//       {open && (
//         <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
//           {/* Overlay */}
//           <motion.button
//             aria-label="Close add expense"
//             className="absolute inset-0 bg-black/30 backdrop-blur-sm"
//             onClick={resetAndClose}
//             initial="hidden"
//             animate="visible"
//             exit="exit"
//             variants={overlayVariants}
//             transition={{ duration: 0.45, ease: "easeOut" }}
//           />

//           {/* Panel */}
//           <motion.div
//             role="dialog"
//             aria-modal="true"
//             aria-labelledby="add-expense-title"
//             className="relative w-full max-w-lg rounded-t-3xl bg-white p-6 shadow-2xl md:rounded-3xl dark:bg-zinc-900 overflow-y-auto max-h-[90dvh]"
//             initial="hidden"
//             animate="visible"
//             exit="exit"
//             variants={panelVariants}
//             transition={{ type: "spring", stiffness: 180, damping: 24, mass: 0.9}}
//           >
//             <div className="mb-4 flex items-center justify-between">
//               <h3 id="add-expense-title" className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
//                 Add Expense
//               </h3>
//               <button
//                 onClick={resetAndClose}
//                 className="rounded-lg px-2 py-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800"
//               >
//                 ✕
//               </button>
//             </div>

//             {/* form */}
//             <div className="grid grid-cols-1 gap-4">
//               {/* Title */}
//               <div>
//                 <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Title</label>
//                 <input
//                   ref={titleRef}
//                   value={title?? ""}
//                   onChange={(e) => setTitle(e.target.value)}
//                   placeholder="e.g., Lunch at Subway"
//                   className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
//                 />
//               </div>

//               {/* Amount + Date */}
//               <div className="grid grid-cols-2 gap-4">
//                 <div>
//                   <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
//                     Amount ({currency})
//                   </label>
//                   <input

//                     type="number"
//                     inputMode="decimal"
//                     value={amount}
//                     onChange={(e) => setAmount(e.target.valueAsNumber)}
//                     onBlur={(e) => {
//                       if (e.target.value === "" || isNaN(Number(e.target.value))) {
//                         setAmount(0);
//                       }
//                     }}
//                     placeholder="0.00"
//                     className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
//                   />
//                 </div>
//                 <div>
//                   <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Date</label>
//                   <input
//                     type="date"
//                     value={date}
//                     onChange={(e) => setDate(e.target.value.replace(/"/g, ""))}
//                     className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
//                   />
//                 </div>
//               </div>

//               {/* Categories grid */}
//                 <div>
//                 <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Category</label>
//                 <div className="mt-1 grid grid-cols-3 gap-2 sm:grid-cols-4">
//                   {categories.map((c) => (
//                   <button
//                     key={c.categoryId}
//                     type="button"
//                     onClick={() => setCategory(c.categoryId)}
//                     className={classNames(
//                     "flex flex-col items-center justify-center gap-1 rounded-xl border px-2 py-2 text-sm transition-colors w-full focus:outline-none overflow-hidden",
//                     category === c.categoryId
//                       ? "border-teal-600 text-teal-600 dark:border-teal-400 dark:text-teal-300"
//                       : "border-zinc-200 text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800"
//                     )}
//                     style={{
//                     backgroundColor: c.categoryColor + "30",
//                     ...(category === c.categoryId
//                       ? { boxShadow: "0 0 0 2px #14b8a6" }
//                       : {}),
//                     }}
//                     aria-pressed={category === c.categoryId}
//                   >
//                     <span className="text-xs font-medium truncate">{c.categoryName}</span>
//                   </button>
//                   ))}
//                 </div>
//                 </div>

//               {/* Notes */}
//               <div>
//                 <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Notes</label>
//                 <textarea
//                   value={description}
//                   onChange={(e) => setDescription(e.target.value)}
//                   placeholder="Optional"
//                   className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
//                 />
//               </div>

//               {/* Receipt */}
//               <label className="inline-flex items-center gap-2 text-sm text-zinc-700 dark:text-zinc-300">
//                 <input
//                   type="checkbox"
//                   checked={isReceipt}
//                   onChange={(e) => setIsReceipt(e.target.checked)}
//                 />
//                 I have a receipt
//               </label>

//               {
//                 isReceipt && (
//                   <div>
//                     <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Upload Receipt</label>
//                     <div
//                       className="mt-1 w-full rounded-xl border-2 border-dashed border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-950 px-3 py-6 text-center cursor-pointer hover:border-teal-500 transition-colors"
//                       onClick={() => document.getElementById('receipt-upload')?.click()}
//                     >
//                       <input
//                         id="receipt-upload"
//                         type="file"
//                         accept="image/*"
//                         className="hidden"
//                         onChange={(e) => handleReceiptFile(e.target.files?.[0])}
//                       />
//                       <input
//                         id="receipt-camera"
//                         type="file"
//                         accept="image/*"
//                         capture="environment"
//                         className="hidden"
//                         onChange={(e) => handleReceiptFile(e.target.files?.[0])}
//                       />
//                       {receipt ? (
//                         <div className="flex flex-col items-center gap-2">
//                           <img
//                             src={receiptPreview}
//                             alt="Receipt preview"
//                             className="max-h-32 rounded-lg object-contain"
//                           />
//                           <button
//                             onClick={(e) => { 
//                               e.stopPropagation(); 
//                               setReceipt(null); 
//                               setReceiptPreview(undefined); 
//                             }}
//                             className="text-xs text-red-400 hover:text-red-500"
//                           >
//                             Remove
//                           </button>
//                         </div>
//                       ) : (
//                         <div className="flex flex-col items-center gap-4 text-zinc-400 dark:text-zinc-500">
//                           <div className="flex gap-6">
//                             {/* Upload button */}
//                             <button
//                               onClick={(e) => { e.stopPropagation(); document.getElementById('receipt-upload')?.click(); }}
//                               className="flex flex-col items-center gap-1 hover:text-teal-500 transition-colors"
//                             >
//                               <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
//                                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
//                               </svg>
//                               <span className="text-xs">Gallery</span>
//                             </button>

//                             {/* Camera button */}
//                             <button
//                               onClick={(e) => { e.stopPropagation(); document.getElementById('receipt-camera')?.click(); }}
//                               className="flex flex-col items-center gap-1 hover:text-teal-500 transition-colors"
//                             >
//                               <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
//                                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
//                                 <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
//                               </svg>
//                               <span className="text-xs">Camera</span>
//                             </button>
//                           </div>
//                           <span className="text-xs">PNG, JPG, WEBP</span>
//                         </div>
//                       )}
//                     </div>
//                   </div>
//                 )}

//               {/* Actions */}
//               <div className="mt-2 flex justify-end gap-2">
//                 <button
//                   onClick={resetAndClose}
//                   className="rounded-xl border border-zinc-300 bg-white px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-300"
//                 >
//                   Cancel
//                 </button>
//                 <button
//                   onClick={submit}
//                   disabled={!valid}
//                   className={classNames(
//                     "rounded-xl px-4 py-2 text-sm font-semibold text-white shadow-sm",
//                     valid ? "bg-teal-600 hover:bg-teal-700" : "bg-teal-300"
//                   )}
//                 >
//                   Save Expense
//                 </button>
//               </div>
//             </div>
//           </motion.div>
//         </div>
//       )}
//     </AnimatePresence>
//   );
// }

// export default AddExpense;


import { useEffect, useRef, useState, useCallback } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { AddExpensePayload, PersonalExpense } from "../../Types/PersonalExpenseListType";
import { addExpense } from "../../Redux/Reducers/PersonalExpenseReducers/addExpense";
import { useAppDispatch } from "../../Redux/hooks";
import { CategoryType } from "../../Redux/slice/CategorySlice";
import { editExpense } from "../../Redux/Reducers/PersonalExpenseReducers/editExpense";

// ─── Helpers ────────────────────────────────────────────────────────────────

function cn(...classes: (string | false | null | undefined)[]) {
  return classes.filter(Boolean).join(" ");
}

const today = () => new Date().toISOString().slice(0, 10);

// ─── Types ───────────────────────────────────────────────────────────────────

interface AddExpenseProps {
  open: boolean;
  onClose: () => void;
  categories: CategoryType[];
  currency?: string;
  defaultDate?: string;
  isEdit?: boolean;
  onExitEdit?: () => void;
  expense?: PersonalExpense | null;
}

interface FormState {
  title: string;
  description: string;
  amount: number;
  categoryId: string;
  date: string;
  hasReceipt: boolean;
  receipt: File | null;
  receiptPreview: string;
  deleteReceipt: boolean; 
}

const INITIAL_FORM: FormState = {
  title: "",
  description: "",
  amount: 0,
  categoryId: "",
  date: today(),
  hasReceipt: false,
  receipt: null,
  receiptPreview: "",
  deleteReceipt: false,
};

// ─── Animation variants ──────────────────────────────────────────────────────

const overlayVariants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1 },
  exit: { opacity: 0 },
};

const panelVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.98 },
  visible: { opacity: 1, y: 0, scale: 1 },
  exit: { opacity: 0, y: 24, scale: 0.98 },
};

// ─── Sub-components ──────────────────────────────────────────────────────────

function FieldLabel({ children }: { children: React.ReactNode }) {
  return (
    <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
      {children}
    </label>
  );
}

function TextInput({
  value,
  onChange,
  placeholder,
  inputRef,
  ...rest
}: React.InputHTMLAttributes<HTMLInputElement> & {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  inputRef?: React.RefObject<HTMLInputElement>;
}) {
  return (
    <input
      ref={inputRef}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      className="w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
      {...rest}
    />
  );
}

function ReceiptUploader({
  receipt,
  receiptPreview,
  onChange,
  onRemove,
}: {
  receipt: File | null;
  receiptPreview: string;
  onChange: (file: File | undefined) => void;
  onRemove: () => void;
}) {
  return (
    <div>
      <FieldLabel>Upload Receipt</FieldLabel>
      <div
        className="mt-1 w-full rounded-xl border-2 border-dashed border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-950 px-3 py-6 text-center cursor-pointer hover:border-teal-500 transition-colors"
        onClick={() => document.getElementById("receipt-upload")?.click()}
      >
        {/* Hidden inputs */}
        {(["receipt-upload", "receipt-camera"] as const).map((id) => (
          <input
            key={id}
            id={id}
            type="file"
            accept="image/*"
            className="hidden"
            {...(id === "receipt-camera" ? { capture: "environment" } : {})}
            onChange={(e) => onChange(e.target.files?.[0])}
          />
        ))}

        {receipt ? (
          <div className="flex flex-col items-center gap-2">
            <img
              src={receiptPreview}
              alt="Receipt preview"
              className="max-h-32 rounded-lg object-contain"
            />
            <button
              onClick={(e) => { e.stopPropagation(); onRemove(); }}
              className="text-xs text-red-400 hover:text-red-500"
            >
              Remove
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-4 text-zinc-400 dark:text-zinc-500">
            <div className="flex gap-6">
              {[
                {
                  id: "receipt-upload",
                  label: "Gallery",
                  path: "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z",
                },
                {
                  id: "receipt-camera",
                  label: "Camera",
                  path: "M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9zM15 13a3 3 0 11-6 0 3 3 0 016 0z",
                },
              ].map(({ id, label, path }) => (
                <button
                  key={id}
                  onClick={(e) => { e.stopPropagation(); document.getElementById(id)?.click(); }}
                  className="flex flex-col items-center gap-1 hover:text-teal-500 transition-colors"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d={path} />
                  </svg>
                  <span className="text-xs">{label}</span>
                </button>
              ))}
            </div>
            <span className="text-xs">PNG, JPG, WEBP</span>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Main Component ──────────────────────────────────────────────────────────

export default function AddExpense({
  open,
  onClose,
  categories,
  currency = "CAD",
  defaultDate,
  isEdit = false,
  onExitEdit,
  expense,
}: AddExpenseProps) {
  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const titleRef = useRef<HTMLInputElement>(null);
  const dispatch = useAppDispatch();

  const isValid = Boolean(form.amount !== 0 && form.categoryId && form.title);

  // ── Helpers ──

  const patch = useCallback(
    (update: Partial<FormState>) => setForm((prev) => ({ ...prev, ...update })),
    []
  );

  const reset = useCallback(() => {
    setForm({ ...INITIAL_FORM, date: defaultDate || today() });
    onExitEdit?.();
    onClose();
  }, [defaultDate, onClose, onExitEdit]);

  const handleReceiptFile = useCallback(
    (file: File | undefined) => {
      if (!file) {
        patch({ receipt: null, receiptPreview: "" });
        return;
      }
      patch({ receipt: file, receiptPreview: URL.createObjectURL(file) });
    },
    [patch]
  );

  // ── Effects ──

  // Auto-focus title on open
  useEffect(() => {
    if (open) requestAnimationFrame(() => titleRef.current?.focus());
  }, [open]);

  // Populate form in edit mode
  useEffect(() => {
    if (!open || !isEdit || !expense) return;
    patch({
      title: expense.title,
      description: expense.description ?? "",
      amount: expense.amount,
      categoryId: expense.categoryId,
      date: expense.expenseDate.replace(/"/g, ""),
      hasReceipt: expense.receiptId != null,
      receipt: expense.receiptUrl ? new File([], expense.receiptUrl) : null,
      receiptPreview: expense.receiptUrl ?? "",
    });
  }, [open, isEdit, expense]);

  // ESC to close
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") reset(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, reset]);

  // Lock body scroll
  useEffect(() => {
    if (!open) return;
    const original = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.body.style.overflow = original; };
  }, [open]);

  // ── Submit ──

  const submit = () => {
    if (!isValid) return;

    const payload: AddExpensePayload = {
      ...(isEdit && expense ? { expenseId: expense.expenseId } : {}),
      title: form.title,
      amount: form.amount,
      categoryId: form.categoryId,
      expenseDate: form.date,
      description: form.description,
      isReceipt: form.hasReceipt,
      deleteReceipt: form.deleteReceipt, 
      ...(form.hasReceipt && form.receipt && form.receipt.size > 0
        ? {
            fileName: form.receipt.name,
            fileType: form.receipt.type,
            fileLength: form.receipt.size,
            file: form.receipt,
          }
        : {}),
    };
    dispatch(isEdit ? editExpense(payload) : addExpense(payload));
    if (isEdit) onExitEdit?.();

    reset();
  };

  // ── Render ──

  return (
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
          {/* Overlay */}
          <motion.button
            aria-label="Close"
            className="absolute inset-0 bg-black/30 backdrop-blur-sm"
            onClick={reset}
            variants={overlayVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            transition={{ duration: 0.45, ease: "easeOut" }}
          />

          {/* Panel */}
          <motion.div
            role="dialog"
            aria-modal="true"
            aria-labelledby="add-expense-title"
            className="relative w-full max-w-lg rounded-t-3xl bg-white p-6 shadow-2xl md:rounded-3xl dark:bg-zinc-900 overflow-y-auto max-h-[90dvh]"
            variants={panelVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            transition={{ type: "spring", stiffness: 180, damping: 24, mass: 0.9 }}
          >
            {/* Header */}
            <div className="mb-4 flex items-center justify-between">
              <h3 id="add-expense-title" className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                {isEdit ? "Edit Expense" : "Add Expense"}
              </h3>
              <button
                onClick={reset}
                className="rounded-lg px-2 py-1 text-zinc-500 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              >
                ✕
              </button>
            </div>

            {/* Form */}
            <div className="grid grid-cols-1 gap-4">
              {/* Title */}
              <div>
                <FieldLabel>Title</FieldLabel>
                <TextInput
                  inputRef={titleRef}
                  value={form.title}
                  onChange={(e) => patch({ title: e.target.value })}
                  placeholder="e.g., Lunch at Subway"
                />
              </div>

              {/* Amount + Date */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <FieldLabel>Amount ({currency})</FieldLabel>
                  <TextInput
                    type="number"
                    inputMode="decimal"
                    value={String(form.amount)}
                    onChange={(e) => patch({ amount: e.target.valueAsNumber || 0 })}
                    placeholder="0.00"
                  />
                </div>
                <div>
                  <FieldLabel>Date</FieldLabel>
                  <TextInput
                    type="date"
                    value={form.date}
                    onChange={(e) => patch({ date: e.target.value.replace(/"/g, "") })}
                  />
                </div>
              </div>

              {/* Category */}
              <div>
                <FieldLabel>Category</FieldLabel>
                <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
                  {categories.map((c) => (
                    <button
                      key={c.categoryId}
                      type="button"
                      onClick={() => patch({ categoryId: c.categoryId })}
                      aria-pressed={form.categoryId === c.categoryId}
                      className={cn(
                        "flex flex-col items-center justify-center gap-1 rounded-xl border px-2 py-2 text-sm transition-colors w-full focus:outline-none overflow-hidden",
                        form.categoryId === c.categoryId
                          ? "border-teal-600 text-teal-600 dark:border-teal-400 dark:text-teal-300"
                          : "border-zinc-200 text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800"
                      )}
                      style={{
                        backgroundColor: c.categoryColor + "30",
                        ...(form.categoryId === c.categoryId ? { boxShadow: "0 0 0 2px #14b8a6" } : {}),
                      }}
                    >
                      <span className="text-xs font-medium truncate">{c.categoryName}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Notes */}
              <div>
                <FieldLabel>Notes</FieldLabel>
                <textarea
                  value={form.description}
                  onChange={(e) => patch({ description: e.target.value })}
                  placeholder="Optional"
                  className="w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                />
              </div>

              {/* Receipt toggle */}
              <label className="inline-flex items-center gap-2 text-sm text-zinc-700 dark:text-zinc-300 cursor-pointer">
                <input
                  type="checkbox"
                  checked={form.hasReceipt}
                  onChange={(e) => {
                    const checked = e.target.checked;
                    patch({
                      hasReceipt: checked,
                      // If unchecking in edit mode and there was an existing receipt → flag for deletion
                      deleteReceipt: !checked && isEdit && expense?.receiptId != null,
                      // Clear preview if unchecking
                      ...(!checked ? { receipt: null, receiptPreview: "" } : {}),
                    });
                  }}
                />
                I have a receipt
              </label>

              {/* Receipt uploader */}
              {form.hasReceipt && (
                <ReceiptUploader
                  receipt={form.receipt}
                  receiptPreview={form.receiptPreview}
                  onChange={handleReceiptFile}
                  onRemove={() => patch({ receipt: null, receiptPreview: "" })}
                />
              )}

              {/* Actions */}
              <div className="mt-2 flex justify-end gap-2">
                <button
                  onClick={reset}
                  className="rounded-xl border border-zinc-300 bg-white px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-300"
                >
                  Cancel
                </button>
                <button
                  onClick={submit}
                  disabled={!isValid}
                  className={cn(
                    "rounded-xl px-4 py-2 text-sm font-semibold text-white shadow-sm",
                    isValid ? "bg-teal-600 hover:bg-teal-700" : "bg-teal-300 cursor-not-allowed"
                  )}
                >
                  {isEdit ? "Update Expense" : "Save Expense"}
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}