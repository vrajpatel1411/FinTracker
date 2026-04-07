import { useEffect, useRef, useState, useCallback } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { AddExpensePayload, PersonalExpense } from "../../Types/PersonalExpenseListType";
import { CategoryType } from "../../Types/category";
import { useAddExpenseMutation, useEditExpenseMutation } from "../../Redux/api/expenseApi";

function cn(...classes: (string | false | null | undefined)[]) {
  return classes.filter(Boolean).join(" ");
}

const today = () => new Date().toISOString().slice(0, 10);

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

interface FormErrors {
  title?: string;
  amount?: string;
  categoryId?: string;
  date?: string;
  receipt?: string;
}



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

const validateForm = (form: FormState): FormErrors => {
  const errors: FormErrors = {};
  if (!form.title.trim()) errors.title = "Title is required";
  if(!form.amount || form.amount <= 0) errors.amount = "Amount must be greater than 0";
  if (!form.categoryId) errors.categoryId = "Please select a category";
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
   if (!form.date) {
    errors.date = "Date is required";
  } else if (!dateRegex.test(form.date)) {
    errors.date = "Invalid date format (use YYYY-MM-DD)";
  } else if (isNaN(new Date(form.date).getTime())) {
    errors.date = "Please enter a valid date";
  }
  if(form.hasReceipt && (!form.receipt || form.receipt.size === 0)) {
    errors.receipt = "Please upload a receipt file";
  }
  return errors;
};

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
  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const titleRef = useRef<HTMLInputElement>(null);
  const [addExpenseMutation] = useAddExpenseMutation();
  const [editExpenseMutation] = useEditExpenseMutation();

  const isValid = Boolean(
                    form.amount > 0 &&
                    form.categoryId &&
                    form.title.trim() &&
                    /^\d{4}-\d{2}-\d{2}$/.test(form.date) &&
                    !isNaN(new Date(form.date).getTime())
                  );
  const patch = useCallback(
    (update: Partial<FormState>) => setForm((prev) => ({ ...prev, ...update })),
    []
  );

  const reset = useCallback(() => {
    setForm({ ...INITIAL_FORM, date: defaultDate ?? today() });
    setFormErrors({});
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

  useEffect(() => {
    if (open) requestAnimationFrame(() => titleRef.current?.focus());
  }, [open]);

  
  useEffect(() => {
    if (!open || !isEdit || !expense) return;
    patch({
      title: expense.title,
      description: expense.description ?? "",
      amount: expense.amount,
      categoryId: expense.categoryId,
      date: expense.expenseDate.replace(/"/g, ""),
      hasReceipt: expense.receiptId !== null,
      receipt: expense.receiptUrl ? new File([], expense.receiptUrl) : null,
      receiptPreview: expense.receiptUrl ?? "",
    });
  }, [open, isEdit, expense,patch]);

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
    const validationErrors = validateForm(form);
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      return;
    }
    setFormErrors({});

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
    void (isEdit ? editExpenseMutation(payload) : addExpenseMutation(payload));
    if (isEdit) onExitEdit?.();
    reset();
  };
  return (
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-50 flex items-end justify-center md:items-center">
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
                  onChange={(e) => {
                              patch({ title: e.target.value })
                              setFormErrors((prev)=>({...prev, title: undefined}))
                            }}
                  placeholder="e.g., Lunch at Subway"
                />
                {formErrors.title && <p className="mt-1 text-xs text-red-400">{formErrors.title}</p>}
              </div>

              {/* Amount + Date */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <FieldLabel>Amount ({currency})</FieldLabel>
                  <TextInput
                    type="number"
                    inputMode="decimal"
                    value={String(form.amount)}
                    onChange={(e) => {
                      patch({ amount: e.target.valueAsNumber || 0 });
                      setFormErrors((prev) => ({ ...prev, amount: undefined }));
                    }}
                    placeholder="0.00"
                  />
                  {formErrors.amount && <p className="mt-1 text-xs text-red-400">{formErrors.amount}</p>}
                </div>
                <div>
                  <FieldLabel>Date</FieldLabel>
                  <TextInput
                    type="date"
                    value={form.date}
                    onChange={(e) => {
                      patch({ date: e.target.value.replace(/"/g, "") });
                      setFormErrors((prev) => ({ ...prev, date: undefined }));
                    }}
                  />
                  {formErrors.date && <p className="mt-1 text-xs text-red-400">{formErrors.date}</p>}
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
                      onClick={() => {
                        patch({ categoryId: c.categoryId })
                        setFormErrors((prev) => ({ ...prev, categoryId: undefined }));
                      }}
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
                {formErrors.categoryId && <p className="mt-1 text-xs text-red-400">{formErrors.categoryId}</p>}
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
                      deleteReceipt: !checked && isEdit && expense?.receiptId !== null,
                      ...(!checked ? { receipt: null, receiptPreview: "" } : {}),
                    });
                  }}
                />
                I have a receipt
              </label>

              {/* Receipt uploader */}
              {form.hasReceipt && (
                <>                <ReceiptUploader
                  receipt={form.receipt}
                  receiptPreview={form.receiptPreview}
                  onChange={handleReceiptFile}
                  onRemove={() => patch({ receipt: null, receiptPreview: "" })}
                />
                {formErrors.receipt && (
                  <p className="mt-1 text-xs text-red-400">{formErrors.receipt}</p>
                )
                }
                </>
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