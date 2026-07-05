// src/Component/personalexpense/ExpenseCharts.tsx
// Drop-in replacement. Recharts kept; colors/typography updated to design system.

import { useMemo } from "react";
import {
    ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
    LineChart, Line, XAxis, YAxis, CartesianGrid, Area, AreaChart,
} from "recharts";

const CHART_COLORS = ["#009689", "#8b5cf6", "#f59e0b", "#3b82f6", "#ef4444", "#10b981"];

const tooltipStyle: React.CSSProperties = {
    background: "#1e1e28",
    border: "1px solid #3f3f46",
    borderRadius: 8,
    color: "#f4f4f5",
    fontSize: 12,
    padding: "8px 10px",
    boxShadow: "0 10px 30px -8px rgba(0,0,0,0.6)",
};

export function CategoryDonut({ data }: { data: Array<{ name: string; value: number }> }) {
    return (
        <div className="bg-surface-card border border-zinc-700/50 rounded-2xl p-5">
            <div className="flex items-center justify-between mb-3">
                <h4 className="text-sm font-semibold text-fg-1">By category</h4>
                <span className="text-[11px] text-fg-4 bg-zinc-800 px-2.5 py-0.5 rounded-full">This month</span>
            </div>
            <div style={{ height: 220 }}>
                <ResponsiveContainer>
                    <PieChart>
                        <Pie data={data} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={2}>
                            {data.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                        </Pie>
                        <Tooltip contentStyle={tooltipStyle} />
                        <Legend wrapperStyle={{ fontSize: 12, color: "#a1a1aa" }} />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

export function DailySpendLine({ data }: { data: Array<{ label: string; value: number }> }) {
    const total = useMemo(() => data.reduce((s, d) => s + d.value, 0), [data]);
    return (
        <div className="bg-surface-card border border-zinc-700/50 rounded-2xl p-5">
            <div className="flex items-center justify-between mb-3">
                <h4 className="text-sm font-semibold text-fg-1">Daily spend</h4>
                <span className="text-[11px] text-fg-4 bg-zinc-800 px-2.5 py-0.5 rounded-full">Last 7 days · {total.toFixed(0)}</span>
            </div>
            <div style={{ height: 220 }}>
                <ResponsiveContainer>
                    <AreaChart data={data} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
                        <defs>
                            <linearGradient id="spendFill" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stopColor="#14b8a6" stopOpacity={0.3} />
                                <stop offset="100%" stopColor="#14b8a6" stopOpacity={0} />
                            </linearGradient>
                        </defs>
                        <CartesianGrid stroke="rgba(63,63,70,0.5)" vertical={false} />
                        <XAxis dataKey="label" stroke="#71717a" fontSize={11} tickLine={false} axisLine={false} />
                        <YAxis stroke="#71717a" fontSize={11} tickLine={false} axisLine={false} />
                        <Tooltip contentStyle={tooltipStyle} />
                        <Area type="monotone" dataKey="value" stroke="#14b8a6" strokeWidth={2} fill="url(#spendFill)" dot={{ fill: "#14b8a6", r: 3 }} />
                    </AreaChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

// Default export keeps backwards-compat with existing imports.
export default function ExpenseCharts({
    categoryData, dailyData,
}: {
    categoryData: Array<{ name: string; value: number }>;
    dailyData: Array<{ label: string; value: number }>;
}) {
    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <div className="lg:col-span-1"><CategoryDonut data={categoryData} /></div>
            <div className="lg:col-span-2"><DailySpendLine data={dailyData} /></div>
        </div>
    );
}
