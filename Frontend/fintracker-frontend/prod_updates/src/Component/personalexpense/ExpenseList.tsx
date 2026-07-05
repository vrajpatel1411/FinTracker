// src/Component/personalexpense/ExpenseList.tsx
// Drop-in replacement. RTK Query hooks/selectors untouched (just imports).
// Replaces ✏️ / 🗑️ emoji with EditOutlined / DeleteOutline icons.

import { useState } from "react";
import AddIcon from "@mui/icons-material/Add";
import MoreHorizIcon from "@mui/icons-material/MoreHoriz";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import { Menu, MenuItem, IconButton, Button } from "@mui/material";
import { fmtCAD } from "./DashBoardCards";
import type { Expense } from "../../types"; // adjust to your real types

const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "2-digit" });

export default function ExpenseList({
    expenses,
    page, totalPages, onPage,
    onAdd, onEdit, onDelete,
}: {
    expenses: Expense[];
    page: number; totalPages: number; onPage: (p: number) => void;
    onAdd: () => void; onEdit: (e: Expense) => void; onDelete: (e: Expense) => void;
}) {
    return (
        <div className="bg-surface-card border border-zinc-700/50 rounded-2xl overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-700/50">
                <h3 className="text-base font-semibold text-fg-1">Recent expenses</h3>
                <Button
                    onClick={onAdd}
                    variant="contained"
                    startIcon={<AddIcon />}
                    sx={{ fontFamily: "'Poppins', 'Inter', sans-serif", letterSpacing: "0.025em" }}
                >
                    Add Expense
                </Button>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead>
                        <tr>
                            {["Item", "Category", "Date", "Amount", "Receipt", ""].map((h) => (
                                <th key={h} className="text-left px-5 py-2.5 bg-surface-base text-[11px] font-medium uppercase tracking-wider text-fg-4">
                                    {h}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {expenses.length === 0 ? (
                            <tr><td colSpan={6} className="px-5 py-10 text-center text-fg-4 text-sm">No expenses found.</td></tr>
                        ) : expenses.map((e) => (
                            <Row key={e.id} expense={e} onEdit={onEdit} onDelete={onDelete} />
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="flex items-center justify-between px-5 py-3 text-sm text-fg-4 border-t border-zinc-700/50">
                <span>Page {page + 1} of {totalPages}</span>
                <div className="flex gap-2">
                    <Button size="small" variant="outlined" disabled={page === 0} onClick={() => onPage(page - 1)}>Prev</Button>
                    <Button size="small" variant="outlined" disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)}>Next</Button>
                </div>
            </div>
        </div>
    );
}

function Row({ expense, onEdit, onDelete }: { expense: Expense; onEdit: (e: Expense) => void; onDelete: (e: Expense) => void }) {
    const [anchor, setAnchor] = useState<HTMLElement | null>(null);
    const [confirm, setConfirm] = useState(false);

    return (
        <tr className="bg-surface-card hover:bg-surface-card-hover transition-colors duration-200">
            <td className="px-5 py-3.5 border-t border-zinc-700/60">
                <div className="text-fg-1 text-[15px]">{expense.title}</div>
                {expense.description && <div className="text-xs text-fg-3 mt-0.5">{expense.description}</div>}
            </td>
            <td className="px-5 py-3.5 border-t border-zinc-700/60 text-sm text-fg-2">{expense.categoryName}</td>
            <td className="px-5 py-3.5 border-t border-zinc-700/60 text-sm text-fg-2 whitespace-nowrap">{formatDate(expense.expenseDate)}</td>
            <td className="px-5 py-3.5 border-t border-zinc-700/60 text-sm font-bold tabular-nums text-brand-glow">{fmtCAD(expense.amount)}</td>
            <td className="px-5 py-3.5 border-t border-zinc-700/60 text-sm">
                {expense.receiptId
                    ? <a href="#" className="text-receipt-yes hover:underline" onClick={(e) => e.preventDefault()}>view</a>
                    : <span className="text-receipt-no">No</span>}
            </td>
            <td className="px-5 py-3.5 border-t border-zinc-700/60 text-right w-10">
                <IconButton size="small" onClick={(e) => { setAnchor(e.currentTarget); setConfirm(false); }} aria-label="Row actions">
                    <MoreHorizIcon fontSize="small" />
                </IconButton>
                <Menu
                    anchorEl={anchor}
                    open={!!anchor}
                    onClose={() => { setAnchor(null); setConfirm(false); }}
                    anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                    transformOrigin={{ vertical: "top", horizontal: "right" }}
                >
                    <MenuItem onClick={() => { setAnchor(null); onEdit(expense); }}>
                        <EditOutlinedIcon fontSize="small" sx={{ mr: 1.5 }} /> Edit
                    </MenuItem>
                    {confirm ? (
                        <div className="px-3 py-1.5 flex items-center gap-1.5">
                            <span className="text-xs text-fg-2">Delete?</span>
                            <Button size="small" color="error" variant="contained" onClick={() => { onDelete(expense); setAnchor(null); setConfirm(false); }}>Yes</Button>
                            <Button size="small" variant="outlined" onClick={() => setConfirm(false)}>No</Button>
                        </div>
                    ) : (
                        <MenuItem onClick={() => setConfirm(true)} sx={{ color: "#f87171" }}>
                            <DeleteOutlineIcon fontSize="small" sx={{ mr: 1.5 }} /> Delete
                        </MenuItem>
                    )}
                </Menu>
            </td>
        </tr>
    );
}
