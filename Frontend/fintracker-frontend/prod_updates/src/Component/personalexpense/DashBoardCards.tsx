// src/Component/personalexpense/DashBoardCards.tsx
// Drop-in replacement. Same props/types as before.

import { useMemo } from "react";
import { TrendingUp, TrendingDown } from "@mui/icons-material";
import { DashboardCardsSkeleton } from "./DashboardCardSkeleton";

type Card = {
    id: string;
    eyebrow: string;
    amount: number | string;
    currency?: string;
    category?: string;
    minorText?: string;
    pct?: number;
    trend?: "up" | "down";
};

const fmt = (n: number) =>
    new Intl.NumberFormat("en-CA", { style: "currency", currency: "CAD" }).format(n);

export default function DashBoardCards({
    rows,
    loading,
}: {
    rows: ReadonlyArray<{ today: number; month: number; topCategory?: { name: string; amount: number }; count: number }>;
    loading?: boolean;
}) {
    const cards: Card[] = useMemo(() => {
        const r = rows[0] ?? { today: 0, month: 0, count: 0 };
        return [
            { id: "today", eyebrow: "TODAY'S SPEND", currency: "CA$", amount: r.today.toFixed(2), minorText: "vs yesterday:", pct: 5, trend: "up" },
            { id: "month", eyebrow: "THIS MONTH", currency: "CA$", amount: r.month.toFixed(2), minorText: "this month", pct: 12, trend: "up" },
            { id: "top", eyebrow: "TOP CATEGORY", category: r.topCategory?.name ?? "—", currency: "CA$", amount: (r.topCategory?.amount ?? 0).toFixed(2), minorText: "of monthly spend" },
            { id: "count", eyebrow: "TRANSACTIONS", amount: r.count, minorText: "this month" },
        ];
    }, [rows]);

    if (loading) return <DashboardCardsSkeleton />;

    return (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {cards.map((c) => (
                <div
                    key={c.id}
                    className="bg-surface-card hover:bg-surface-card-alt border border-zinc-700/50 hover:border-zinc-500/80 rounded-lg p-4 lg:p-5 transition-colors duration-200 flex flex-col gap-1.5 min-h-[110px]"
                >
                    <span className="text-[11px] font-semibold tracking-widest uppercase text-fg-4">{c.eyebrow}</span>
                    <div className="flex items-baseline gap-1">
                        {c.category && <span className="text-base font-semibold text-brand-glow mr-1">{c.category}</span>}
                        {c.currency && <span className="text-sm font-medium text-fg-3">{c.currency}</span>}
                        <span className="text-2xl font-bold tracking-tight text-fg-1 tabular-nums">{c.amount}</span>
                    </div>
                    <div className="flex items-center gap-2 flex-wrap">
                        {c.minorText && <span className="text-xs text-fg-4">{c.minorText}</span>}
                        {c.pct != null && (
                            <span
                                className={`inline-flex items-center gap-0.5 text-xs font-semibold px-2 py-0.5 rounded-full ${
                                    c.trend === "up" ? "bg-positive/15 text-positive" : "bg-negative/15 text-negative"
                                }`}
                            >
                                {c.trend === "up" ? <TrendingUp sx={{ fontSize: 12 }} /> : <TrendingDown sx={{ fontSize: 12 }} />}
                                {c.pct}%
                            </span>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
}

export { fmt as fmtCAD };
