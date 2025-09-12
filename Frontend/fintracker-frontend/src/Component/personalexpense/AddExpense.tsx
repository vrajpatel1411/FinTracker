import React, { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";

function classNames(...c: (string | false | null | undefined)[]) {
  return c.filter(Boolean).join(" ");
}

interface AddExpenseProps {
  open: boolean;
  onClose: () => void;
  categories: { id: string; name: string; icon: string; color: string }[];
  currency?: string; // default "CAD"
  defaultDate?: string; // ISO "YYYY-MM-DD" (fallback: today)
}

function AddExpense({
  open,
  onClose,
  categories,
  currency = "CAD",
  defaultDate,
}: AddExpenseProps) {
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState("");
  const [date, setDate] = useState(defaultDate || new Date().toISOString().slice(0, 10));
  const [title, setTitle] = useState("");
  const [note, setNote] = useState("");
  const [receipt, setReceipt] = useState(false);

  const titleRef = useRef<HTMLInputElement | null>(null);
  const valid = Boolean(amount && category && title);

  // Focus first field when modal opens
  useEffect(() => {
    if (open && titleRef.current) {
      requestAnimationFrame(() => titleRef.current?.focus());
    }
  }, [open]);

  // Close on ESC
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose?.();
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
    onClose?.();
    // Reset
    setAmount("");
    setCategory("");
    setDate(defaultDate || new Date().toISOString().slice(0, 10));
    setTitle("");
    setNote("");
    setReceipt(false);
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
            onClick={onClose}
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
                onClick={onClose}
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
                  value={title}
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
                    inputMode="decimal"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value.replace(/[^0-9.]/g, ""))}
                    placeholder="0.00"
                    className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                  />
                </div>
                <div>
                  <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Date</label>
                  <input
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                  />
                </div>
              </div>

              {/* Categories grid */}
              <div>
                <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Category</label>
                <div className="mt-1 grid grid-cols-3 gap-2 sm:grid-cols-6">
                  {categories.map((c) => (
                    <button
                      key={c.id}
                      type="button"
                      onClick={() => setCategory(c.id)}
                      className={classNames(
                        "flex items-center justify-center gap-1 rounded-xl border px-2 py-2 text-sm",
                        category === c.id
                          ? "border-transparent bg-teal-600 text-white"
                          : "border-zinc-200 bg-white text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-300"
                      )}
                      aria-pressed={category === c.id}
                    >
                      <span>{c.icon}</span>
                      <span className="hidden sm:inline">{c.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Notes */}
              <div>
                <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Notes</label>
                <textarea
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="Optional"
                  className="mt-1 w-full rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm outline-none focus:border-teal-500 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100"
                />
              </div>

              {/* Receipt */}
              <label className="inline-flex items-center gap-2 text-sm text-zinc-700 dark:text-zinc-300">
                <input
                  type="checkbox"
                  checked={receipt}
                  onChange={(e) => setReceipt(e.target.checked)}
                />
                I have a receipt
              </label>

              {/* Actions */}
              <div className="mt-2 flex justify-end gap-2">
                <button
                  onClick={onClose}
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
