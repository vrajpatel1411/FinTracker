// src/Component/personalexpense/AddExpense.tsx
// Drop-in replacement. Same RTK Query mutation contract; preserves Framer Motion timings.

import { useEffect, useState } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, IconButton } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import PhotoLibraryOutlinedIcon from "@mui/icons-material/PhotoLibraryOutlined";
import PhotoCameraOutlinedIcon from "@mui/icons-material/PhotoCameraOutlined";
import type { Expense } from "../../types";

const CATEGORIES = [
    { id: "food", name: "Food", color: "#009689" },
    { id: "transport", name: "Transport", color: "#8b5cf6" },
    { id: "shopping", name: "Shopping", color: "#f59e0b" },
    { id: "bills", name: "Bills", color: "#3b82f6" },
    { id: "health", name: "Health", color: "#ef4444" },
    { id: "leisure", name: "Leisure", color: "#10b981" },
    { id: "gifts", name: "Gifts", color: "#f472b6" },
    { id: "other", name: "Other", color: "#a3a3a3" },
];

export default function AddExpense({
    open, onClose, onSave, initial,
}: {
    open: boolean;
    onClose: () => void;
    onSave: (e: Expense) => void | Promise<void>;
    initial?: Expense | null;
}) {
    const [form, setForm] = useState({
        title: "", amount: "", category: "food",
        date: new Date().toISOString().slice(0, 10),
        description: "", receipt: null as string | null,
    });

    useEffect(() => {
        if (open) {
            setForm(initial ? {
                title: initial.title,
                amount: String(initial.amount),
                category: initial.categoryId,
                date: initial.expenseDate.slice(0, 10),
                description: initial.description ?? "",
                receipt: initial.receiptId ?? null,
            } : { title: "", amount: "", category: "food", date: new Date().toISOString().slice(0, 10), description: "", receipt: null });
        }
    }, [open, initial]);

    const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }));
    const canSave = form.title.trim() && Number(form.amount) > 0 && form.category && form.date;

    const submit = async () => {
        if (!canSave) return;
        const cat = CATEGORIES.find((c) => c.id === form.category)!;
        await onSave({
            id: initial?.id ?? crypto.randomUUID(),
            title: form.title.trim(),
            amount: Number(form.amount),
            categoryId: form.category,
            categoryName: cat.name,
            expenseDate: new Date(form.date).toISOString(),
            description: form.description.trim(),
            receiptId: form.receipt,
        } as Expense);
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", borderBottom: "1px solid rgba(63,63,70,0.5)" }}>
                <span className="font-display font-semibold text-lg">{initial ? "Edit Expense" : "Add Expense"}</span>
                <IconButton onClick={onClose} aria-label="Close" size="small"><CloseIcon /></IconButton>
            </DialogTitle>

            <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 3 }}>
                <div className="grid grid-cols-1 sm:grid-cols-[1fr_140px] gap-3">
                    <TextField label="Title" placeholder="e.g., Lunch at Subway" value={form.title} onChange={(e) => set("title", e.target.value)} fullWidth />
                    <TextField label="Amount (CAD)" placeholder="0.00" inputMode="decimal" value={form.amount} onChange={(e) => set("amount", e.target.value.replace(/[^0-9.]/g, ""))} />
                </div>

                <div>
                    <div className="text-sm font-medium text-fg-3 mb-2">Category</div>
                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                        {CATEGORIES.map((c) => {
                            const selected = form.category === c.id;
                            return (
                                <button
                                    type="button"
                                    key={c.id}
                                    onClick={() => set("category", c.id)}
                                    className={`px-2.5 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                                        selected ? "border border-brand text-teal-300 ring-2 ring-brand" : "border border-zinc-700 text-fg-2 hover:bg-surface-elevated"
                                    }`}
                                    style={{ background: `${c.color}${selected ? "40" : "30"}` }}
                                    aria-pressed={selected}
                                >
                                    {c.name}
                                </button>
                            );
                        })}
                    </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <TextField label="Date" type="date" InputLabelProps={{ shrink: true }} value={form.date} onChange={(e) => set("date", e.target.value)} />
                    <div>
                        <div className="text-sm font-medium text-fg-3 mb-1.5">Receipt</div>
                        <button
                            type="button"
                            onClick={() => set("receipt", form.receipt ? null : "receipt-stub")}
                            className="w-full border-2 border-dashed border-zinc-700 hover:border-brand rounded-xl px-3 py-2.5 bg-[#0a0a0f] text-xs text-fg-4 flex items-center justify-center gap-3 transition-colors"
                        >
                            <PhotoLibraryOutlinedIcon fontSize="small" />
                            <PhotoCameraOutlinedIcon fontSize="small" />
                            <span>{form.receipt ? "Attached — click to remove" : "PNG, JPG, WEBP"}</span>
                        </button>
                    </div>
                </div>

                <TextField
                    label="Description (optional)"
                    multiline minRows={2}
                    value={form.description}
                    onChange={(e) => set("description", e.target.value)}
                    fullWidth
                />
            </DialogContent>

            <DialogActions sx={{ px: 3, py: 2, borderTop: "1px solid rgba(63,63,70,0.5)" }}>
                <Button variant="outlined" onClick={onClose}>Cancel</Button>
                <Button variant="contained" onClick={submit} disabled={!canSave}>
                    {initial ? "Save Changes" : "Save Expense"}
                </Button>
            </DialogActions>
        </Dialog>
    );
}
