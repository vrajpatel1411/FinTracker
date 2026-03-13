// ExpenseCharts.tsx
import React from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
} from 'recharts';
import { ChartsSkeleton } from './DashboardCardSkeleton';

const COLOR_MAP: Record<string, string> = {
  Food:      '#22c55e',
  Travel:    '#06b6d4',
  Bills:     '#f59e0b',
  Groceries: '#a78bfa',
  Health:    '#ef4444',
  Shopping:  '#64748b',
  Others:    '#94a3b8',
  Misc:      '#64748b',
};

const colorFor = (name?: string, provided?: string) =>
  provided || (name ? COLOR_MAP[name] ?? '#94a3b8' : '#94a3b8');

const currency = (n: number) =>
  Number(n).toLocaleString(undefined, { style: 'currency', currency: 'CAD' });

const data = [
'#f59e0b' ,
  '#a78bfa' ,
  '#64748b' ,
  '#06b6d4' ,
  '#ef4444',
  '#94a3b8' ,
];

const CustomTooltip: React.FC<any> = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#1e1e28] border border-zinc-600 rounded-lg px-3 py-2 text-sm shadow-xl">
      {label && <p className="text-zinc-400 mb-1 text-xs">{label}</p>}
      {payload.map((p: any, i: number) => (
        <p key={i} className="text-white font-semibold">
          {p.name ? `${p.name}: ` : ''}{currency(p.value)}
        </p>
      ))}
    </div>
  );
};

const CustomLegend: React.FC<any> = ({ payload }) => (
  <div className="flex flex-wrap justify-center gap-x-3 gap-y-1 mt-2">
    {payload?.map((entry: any, i: number) => (
      <span key={i} className="flex items-center gap-1 text-[11px] text-zinc-400">
        <span
          className="inline-block w-2 h-2 rounded-full"
          style={{ backgroundColor: entry.color }}
        />
        {entry.value}
      </span>
    ))}
  </div>
);

interface categoriesTypes{
  name: string;
  value: number;
}

interface dailyExpensesTypes{
  date: string;
  amount: number;
}
// ── Props ─────────────────────────────────────────────────────────────────────
interface ExpenseChartsProps {
  isLoading?: boolean;
  categories: categoriesTypes[] | undefined | null  ;
  dailyExpenses: dailyExpensesTypes[] | undefined | null ;
}

const ExpenseCharts: React.FC<ExpenseChartsProps> = ({ isLoading,categories, dailyExpenses }) => {
  if (isLoading) return <ChartsSkeleton />;
  const LineChartData = dailyExpenses?.map(de => {
    const [_, month, day] = de.date.split('-');
    return { date: `${day}/${month}`, value: de.amount };
  }) ?? [];
  
  const pieData = categories?.map((c, index) => ({
    name: c.name,
    value: c.value,
    color: data[index % data.length]
  })) ?? [];

  return (
    <div className="mx-auto w-full max-w-6xl px-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">

        {/* ── Category Split ──────────────────────────────────────────────── */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-[#17171c]
                        hover:border-zinc-500/60 transition-colors duration-200">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-sm font-semibold text-white tracking-wide">Category Split</h4>
            <span className="text-[11px] text-zinc-500 bg-zinc-800 px-2 py-0.5 rounded-full">
              This month
            </span>
          </div>
          <div className="h-52">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="46%"
                  outerRadius={72}
                  innerRadius={46}
                  paddingAngle={3}
                  strokeWidth={0}
                >
                  {pieData.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend content={<CustomLegend />} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* ── Daily Spend ─────────────────────────────────────────────────── */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-[#17171c] lg:col-span-2
                        hover:border-zinc-500/60 transition-colors duration-200">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-sm font-semibold text-white tracking-wide">Daily Spend</h4>
            <span className="text-[11px] text-zinc-500 bg-zinc-800 px-2 py-0.5 rounded-full">
              Last 7 days
            </span>
          </div>
          <div className="h-52">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={LineChartData}
                margin={{ top: 8, right: 12, left: -8, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#14b8a6" stopOpacity={0.25} />
                    <stop offset="100%" stopColor="#14b8a6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#2a2a35" vertical={false} />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: '#71717a' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: '#71717a' }}
                  axisLine={false}
                  tickLine={false}
                  tickFormatter={(v) => `$${v}`}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#14b8a6"
                  strokeWidth={2.5}
                  dot={false}
                  activeDot={{ r: 4, fill: '#14b8a6', strokeWidth: 0 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  );
};

export default ExpenseCharts;