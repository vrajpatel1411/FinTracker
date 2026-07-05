import { useCallback, useEffect, useMemo, useRef, useState } from "react";
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
    <label className="block text-sm font-medium text-fg-2 mb-1">
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
      className="w-full rounded-xl border border-zinc-700 bg-surface-base px-3 py-2 text-sm text-fg-1 outline-none focus:border-brand"
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
  const triggerUpload = (id: string) => document.getElementById(id)?.click();

  return (
    <div>
      <FieldLabel>Upload Receipt</FieldLabel>
      <div
        role="button"
        tabIndex={0}
        aria-label="Upload receipt — click to open gallery"
        onClick={() => triggerUpload("receipt-upload")}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); triggerUpload("receipt-upload"); } }}
        className="mt-1 w-full rounded-xl border-2 border-dashed border-zinc-700 bg-surface-base px-3 py-6 text-center cursor-pointer hover:border-brand transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
      >
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
            <img src={receiptPreview} alt="Receipt preview" className="max-h-32 rounded-lg object-contain" />
            <button
              onClick={(e) => { e.stopPropagation(); onRemove(); }}
              className="text-xs text-red-400 hover:text-red-300"
            >
              Remove
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-4 text-zinc-500">
            <div className="flex gap-6">
              {[
                { id: "receipt-upload", label: "Gallery", path: "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" },
                { id: "receipt-camera", label: "Camera", path: "M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9zM15 13a3 3 0 11-6 0 3 3 0 016 0z" },
              ].map(({ id, label, path }) => (
                <button
                  key={id}
                  type="button"
                  onClick={(e) => { e.stopPropagation(); triggerUpload(id); }}
                  className="flex flex-col items-center gap-1 hover:text-brand transition-colors"
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
  if (!form.amount || form.amount <= 0) errors.amount = "Amount must be greater than 0";
  if (!form.categoryId) errors.categoryId = "Please select a category";
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
  if (!form.date) {
    errors.date = "Date is required";
  } else if (!dateRegex.test(form.date)) {
    errors.date = "Invalid date format (use YYYY-MM-DD)";
  } else if (isNaN(new Date(form.date).getTime())) {
    errors.date = "Please enter a valid date";
  }
  if (form.hasReceipt && (!form.receipt || form.receipt.size === 0)) {
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
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const titleRef = useRef<HTMLInputElement>(null);
  const [addExpenseMutation] = useAddExpenseMutation();
  const [editExpenseMutation] = useEditExpenseMutation();

  const isValid = useMemo(() => Boolean(
    form.amount > 0 &&
    form.categoryId &&
    form.title.trim() &&
    /^\d{4}-\d{2}-\d{2}$/.test(form.date) &&
    !isNaN(new Date(form.date).getTime())
  ), [form]);

  const patch = useCallback(
    (update: Partial<FormState>) => setForm((prev) => ({ ...prev, ...update })),
    []
  );

  const reset = useCallback(() => {
    setForm({ ...INITIAL_FORM, date: defaultDate ?? today() });
    setFormErrors({});
    setSubmitError(null);
    onExitEdit?.();
    onClose();
  }, [defaultDate, onClose, onExitEdit]);

  const handleReceiptFile = useCallback(
    (file: File | undefined) => {
      if (!file) { patch({ receipt: null, receiptPreview: "" }); return; }
      patch({ receipt: file, receiptPreview: URL.createObjectURL(file) });
    },
    [patch]
  );

  useEffect(() => {
    const preview = form.receiptPreview;
    return () => { if (preview.startsWith("blob:")) URL.revokeObjectURL(preview); };
  }, [form.receiptPreview]);

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
      receipt: null,
      receiptPreview: expense.receiptUrl ?? "",
    });
  }, [open, isEdit, expense, patch]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") reset(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, reset]);

  useEffect(() => {
    if (!open) return;
    const original = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.body.style.overflow = original; };
  }, [open]);

  const submit = async () => {
    const validationErrors = validateForm(form);
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      return;
    }
    setFormErrors({});
    setSubmitError(null);

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

    setIsSubmitting(true);
    try {
      const mutation = isEdit ? editExpenseMutation : addExpenseMutation;
      await mutation(payload).unwrap();
      if (isEdit) onExitEdit?.();
      reset();
    } catch {
      setSubmitError("Failed to save expense. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
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
          <motion.div
            role="dialog"
            aria-modal="true"
            aria-labelledby="add-expense-title"
            className="relative w-full max-w-lg rounded-t-3xl bg-surface-card p-6 shadow-2xl md:rounded-3xl overflow-y-auto max-h-[90dvh]"
            variants={panelVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            transition={{ type: "spring", stiffness: 180, damping: 24, mass: 0.9 }}
          >
            <div className="mb-4 flex items-center justify-between">
              <h3 id="add-expense-title" className="text-lg font-semibold text-fg-1">
                {isEdit ? "Edit Expense" : "Add Expense"}
              </h3>
              <button
                onClick={reset}
                aria-label="Close dialog"
                className="rounded-lg px-2 py-1 text-zinc-500 hover:bg-surface-elevated"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-1 gap-4">
              <div>
                <FieldLabel>Title</FieldLabel>
                <TextInput
                  inputRef={titleRef}
                  value={form.title}
                  onChange={(e) => { patch({ title: e.target.value }); setFormErrors((p) => ({ ...p, title: undefined })); }}
                  placeholder="e.g., Lunch at Subway"
                />
                {formErrors.title && <p role="alert" className="mt-1 text-xs text-red-400">{formErrors.title}</p>}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <FieldLabel>Amount ({currency})</FieldLabel>
                  <TextInput
                    type="number"
                    inputMode="decimal"
                    value={String(form.amount)}
                    onChange={(e) => { patch({ amount: e.target.valueAsNumber || 0 }); setFormErrors((p) => ({ ...p, amount: undefined })); }}
                    placeholder="0.00"
                  />
                  {formErrors.amount && <p role="alert" className="mt-1 text-xs text-red-400">{formErrors.amount}</p>}
                </div>
                <div>
                  <FieldLabel>Date</FieldLabel>
                  <TextInput
                    type="date"
                    value={form.date}
                    onChange={(e) => { patch({ date: e.target.value.replace(/"/g, "") }); setFormErrors((p) => ({ ...p, date: undefined })); }}
                  />
                  {formErrors.date && <p role="alert" className="mt-1 text-xs text-red-400">{formErrors.date}</p>}
                </div>
              </div>

              <div>
                <FieldLabel>Category</FieldLabel>
                <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
                  {categories.map((c) => (
                    <button
                      key={c.categoryId}
                      type="button"
                      onClick={() => { patch({ categoryId: c.categoryId }); setFormErrors((p) => ({ ...p, categoryId: undefined })); }}
                      aria-pressed={form.categoryId === c.categoryId}
                      className={cn(
                        "flex flex-col items-center justify-center gap-1 rounded-xl border px-2 py-2 text-sm transition-colors w-full focus:outline-none overflow-hidden",
                        form.categoryId === c.categoryId
                          ? "border-brand text-teal-300"
                          : "border-zinc-700 text-fg-2 hover:bg-surface-elevated"
                      )}
                      style={{
                        backgroundColor: c.categoryColor + "30",
                        ...(form.categoryId === c.categoryId ? { boxShadow: "0 0 0 2px #009689" } : {}),
                      }}
                    >
                      <span className="text-xs font-medium truncate">{c.categoryName}</span>
                    </button>
                  ))}
                </div>
                {formErrors.categoryId && <p role="alert" className="mt-1 text-xs text-red-400">{formErrors.categoryId}</p>}
              </div>

              <div>
                <FieldLabel>Notes</FieldLabel>
                <textarea
                  value={form.description}
                  onChange={(e) => patch({ description: e.target.value })}
                  placeholder="Optional"
                  className="w-full rounded-xl border border-zinc-700 bg-surface-base px-3 py-2 text-sm text-fg-1 outline-none focus:border-brand"
                />
              </div>

              <label className="inline-flex items-center gap-2 text-sm text-fg-2 cursor-pointer">
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

              {form.hasReceipt && (
                <>
                  <ReceiptUploader
                    receipt={form.receipt}
                    receiptPreview={form.receiptPreview}
                    onChange={handleReceiptFile}
                    onRemove={() => patch({ receipt: null, receiptPreview: "" })}
                  />
                  {formErrors.receipt && <p role="alert" className="mt-1 text-xs text-red-400">{formErrors.receipt}</p>}
                </>
              )}

              {submitError && (
                <p role="alert" className="text-sm text-red-400 text-center">{submitError}</p>
              )}

              <div className="mt-2 flex justify-end gap-2">
                <button
                  onClick={reset}
                  disabled={isSubmitting}
                  className="rounded-xl border border-zinc-700 bg-surface-base px-4 py-2 text-sm font-medium text-fg-2 hover:bg-surface-elevated disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  onClick={submit}
                  disabled={!isValid || isSubmitting}
                  className={cn(
                    "rounded-xl px-4 py-2 text-sm font-semibold text-white shadow-sm transition-colors",
                    isValid && !isSubmitting ? "bg-brand hover:bg-brand-dark" : "bg-brand/40 cursor-not-allowed"
                  )}
                >
                  {isSubmitting ? "Saving…" : isEdit ? "Update Expense" : "Save Expense"}
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
