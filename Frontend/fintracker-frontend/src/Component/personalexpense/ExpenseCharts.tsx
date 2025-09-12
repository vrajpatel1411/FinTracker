import React from "react";
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
} from "recharts";

// ---- Palette & helpers -----------------------------------------------------
const COLOR_MAP: Record<string, string> = {
  Food: "#22c55e",
  Travel: "#06b6d4",
  Bills: "#f59e0b",
  Groceries: "#a78bfa",
  Health: "#ef4444",
  Misc: "#64748b",
  Others: "#94a3b8",      // fallback bucket
  Shopping: "#64748b",    // map to neutral if not in standard cats
};

const colorFor = (name?: string, provided?: string) =>
  provided || (name ? COLOR_MAP[name] : "#94a3b8");

const currency = (n: number) =>
  Number(n).toLocaleString(undefined, { style: "currency", currency: "CAD" });

// ---- Sample data (your existing arrays) ------------------------------------
const data = [
  { name: "Bills", value: 400, color: "#0088FE" },
  { name: "Groceries", value: 300, color: "#00C49F" },
  { name: "Shopping", value: 300, color: "#FFBB28" },
  { name: "Travel", value: 200, color: "#FF8042" },
  { name: "Health", value: 278, color: "#A020F0" },
  { name: "Others", value: 189, color: "#FF4560" },
];

const LineChartData = [
  { date: "19/08", value: 100 },
  { date: "20/08", value: 120 },
  { date: "21/08", value: 90 },
  { date: "22/08", value: 150 },
  { date: "23/08", value: 80 },
  { date: "24/08", value: 200 },
  { date: "25/08", value: 170 },
];

// ---- Component -------------------------------------------------------------
const ExpenseCharts: React.FC = () => {
  // Normalize pie colors to match app palette but keep provided as fallback
  const pieData = (data ?? []).map((d) => ({
    ...d,
    color: colorFor(d?.name, d?.color),
  }));

  return (
    <div className="mx-auto my-0 w-full max-w-6xl px-4">
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Category Split card */}
        <div className="rounded-2xl border-2 border-zinc-500 p-4 shadow-sm bg-zinc-900">
          <div className="mb-2 flex items-center justify-between">
            <h4 className="text-sm font-semibold">Category Split</h4>
            <span className="text-xs text-zinc-500">This month</span>
          </div>

          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={70}
                  innerRadius={45}
                  paddingAngle={2}
                  labelLine={false}
                >
                  {pieData.map((entry, i) => (
                    <Cell key={`cell-${i}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(v: number, _n, p) =>
                    [currency(v), p?.payload?.name ?? ""]
                  }
                />
                <Legend verticalAlign="bottom" height={24} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Daily Spend card */}
        <div className="rounded-2xl border-2 border-zinc-500  p-4 shadow-sm  bg-zinc-900 lg:col-span-2">
          <div className="mb-2 flex items-center justify-between">
            <h4 className="text-sm font-semibold">Daily Spend</h4>
            <span className="text-xs text-zinc-500">Last 7 days</span>
          </div>

          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={LineChartData}
                margin={{ top: 10, right: 16, left: 0, bottom: 0 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip
                  formatter={(v: number) => currency(v)}
                  labelFormatter={(label) => `Date: ${label}`}
                />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#14b8a6"   // teal to match wireframe
                  strokeWidth={2}
                  dot={false}
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
