import { useMemo } from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  AreaChart,
  Area,
  CartesianGrid,
  XAxis,
  YAxis,
} from 'recharts';
import { ChartsSkeleton } from './DashboardCardSkeleton';
import { categoriesType, dailyExpensesType } from '../../Types/AnalyticsType';

const CHART_COLORS = ['#009689', '#8b5cf6', '#f59e0b', '#3b82f6', '#ef4444', '#10b981'];

const tooltipStyle: React.CSSProperties = {
  background: '#1e1e28',
  border: '1px solid #3f3f46',
  borderRadius: 8,
  color: '#f4f4f5',
  fontSize: 12,
  padding: '8px 10px',
  boxShadow: '0 10px 30px -8px rgba(0,0,0,0.6)',
};

interface ExpenseChartsProps {
  isLoading?: boolean;
  categories: categoriesType[] | undefined | null;
  dailyExpenses: dailyExpensesType[] | undefined | null;
}

const ExpenseCharts: React.FC<ExpenseChartsProps> = ({ isLoading, categories, dailyExpenses }) => {
  const pieData = useMemo(() =>
    (categories ?? []).map((c, i) => ({
      name: c.name,
      value: c.value,
      fill: CHART_COLORS[i % CHART_COLORS.length],
    })),
    [categories]);

  const areaData = useMemo(() =>
    (dailyExpenses ?? []).map(({ date, amount }) => {
      const [_year, month, day] = date.split('-');
      return { date: `${day}/${month}`, value: amount };
    }),
    [dailyExpenses]);

  if (isLoading) return <ChartsSkeleton />;

  return (
    <div className="mx-auto w-full max-w-6xl px-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">

        {/* Category Donut */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-surface-card hover:border-zinc-500/60 transition-colors duration-200">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-sm font-semibold text-fg-1">By category</h4>
            <span className="text-[11px] text-fg-4 bg-zinc-800 px-2.5 py-0.5 rounded-full">This month</span>
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
                  paddingAngle={2}
                  strokeWidth={0}
                >
                  {pieData.map((entry, i) => (
                    <Cell key={i} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip contentStyle={tooltipStyle} />
                <Legend wrapperStyle={{ fontSize: 12, color: '#a1a1aa' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Daily Spend Area */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-surface-card lg:col-span-2 hover:border-zinc-500/60 transition-colors duration-200">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-sm font-semibold text-fg-1">Daily Spend</h4>
            <span className="text-[11px] text-fg-4 bg-zinc-800 px-2.5 py-0.5 rounded-full">Last 7 days</span>
          </div>
          <div className="h-52">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={areaData} margin={{ top: 8, right: 12, left: -8, bottom: 0 }}>
                <defs>
                  <linearGradient id="spendFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#14b8a6" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="#14b8a6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(63,63,70,0.5)" vertical={false} />
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
                <Tooltip contentStyle={tooltipStyle} />
                <Area
                  type="monotone"
                  dataKey="value"
                  stroke="#14b8a6"
                  strokeWidth={2.5}
                  fill="url(#spendFill)"
                  dot={false}
                  activeDot={{ r: 4, fill: '#14b8a6', strokeWidth: 0 }}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  );
};

export default ExpenseCharts;
