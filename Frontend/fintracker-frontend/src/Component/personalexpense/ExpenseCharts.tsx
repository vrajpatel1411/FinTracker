// ExpenseCharts.tsx
import React from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Tooltip,
  Legend,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Sector,
  PieSectorShapeProps,
} from 'recharts';
import { ChartsSkeleton } from './DashboardCardSkeleton';
import { categoriesType, dailyExpensesType } from "../../Types/AnalyticsType";

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

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    name?: string | number;
    value?: string | number;
    color?: string;
  }>;
  label?: string | number;
}
const CustomTooltip= (props: CustomTooltipProps) => {
  const { active, payload, label } = props;
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#1e1e28] border border-zinc-600 rounded-lg px-3 py-2 text-sm shadow-xl">
      {label && <p className="text-zinc-400 mb-1 text-xs">{label}</p>}
      {payload.map((p, i) => (          // ← p is now typed as Payload<ValueType, NameType>
        <p key={i} className="text-white font-semibold">
          {p.name ? `${String(p.name)}: ` : ''}
          {currency(Number(p.value ?? 0))}
        </p>
      ))}
    </div>
  );
};

const renderPieShape = (
  pieData: { fill?: string }[]
) => (props: PieSectorShapeProps) => {
  const fill = pieData[props.index ?? 0]?.fill;
  return <Sector {...props} fill={fill} />;
};

interface CustomLegendProps {
  payload?: Array<{
    value?: string | number;
    color?: string;
    type?: string;
  }>;
}

const CustomLegend = ({ payload }: CustomLegendProps) => (
  <div className="flex flex-wrap justify-center gap-x-3 gap-y-1 mt-2">
    {payload?.map((entry, i) => (
      <span key={i} className="flex items-center gap-1 text-[11px] text-zinc-400">
        <span
          className="inline-block w-2 h-2 rounded-full"
          style={{ backgroundColor: entry.color ?? 'transparent' }}
        />
        {String(entry.value ?? '')}
      </span>
    ))}
  </div>
);

// ── Props ─────────────────────────────────────────────────────────────────────
interface ExpenseChartsProps {
  isLoading?: boolean;
  categories: categoriesType[] | undefined | null  ;
  dailyExpenses: dailyExpensesType[] | undefined | null ;
}

const ExpenseCharts: React.FC<ExpenseChartsProps> = ({ isLoading,categories, dailyExpenses }) => {
  if (isLoading) return <ChartsSkeleton />;

  const LineChartData = (dailyExpenses ?? []).map(({ date, amount }) => {
  const [_year, month, day] = date.split('-');
  return { date: `${day}/${month}`, value: amount };
});

const pieData = (categories ?? []).map((c, index) => ({
  name: c.name,
  value: c.value,
  fill: data[index % data.length],
}));

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
                  shape={renderPieShape(pieData)}
                >
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend content={<CustomLegend />} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
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