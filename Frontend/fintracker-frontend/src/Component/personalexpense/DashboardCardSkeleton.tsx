import type { CSSProperties } from 'react';

const shimmerBase: CSSProperties = {
  background: "linear-gradient(90deg, #1e1e26 25%, #28283a 50%, #1e1e26 75%)",
  backgroundSize: "600px 100%",
};

function Shim({ className, style }: { className?: string; style?: CSSProperties }) {
  return (
    <div
      className={`rounded animate-shimmer ${className ?? ""}`}
      style={{ ...shimmerBase, ...style }}
    />
  );
}

export const DashboardCardsSkeleton = () => (
  <div className="mx-auto w-full max-w-6xl px-4">
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {Array.from({ length: 4 }).map((_, i) => (
        <div key={i} className="bg-surface-card border border-zinc-700/50 rounded-xl p-5 min-h-[110px] flex flex-col gap-2.5">
          <Shim className="h-3 w-24" />
          <Shim className="h-7 w-28" />
          <Shim className="h-3 w-20" />
        </div>
      ))}
    </div>
  </div>
);

export const ExpenseRowsSkeleton = ({ rows = 6 }: { rows?: number }) => (
  <>
    {Array.from({ length: rows }).map((_, i) => (
      <tr key={i} className="bg-surface-card border-b border-zinc-800">
        <td className="px-6 py-4">
          <div className="flex flex-col gap-1.5">
            <Shim className="h-3.5" style={{ width: 120 }} />
            <Shim className="h-3" style={{ width: 80 }} />
          </div>
        </td>
        <td className="px-6 py-4"><Shim className="h-3.5" style={{ width: 72 }} /></td>
        <td className="px-6 py-4"><Shim className="h-3.5" style={{ width: 80 }} /></td>
        <td className="px-6 py-4"><Shim className="h-3.5" style={{ width: 56 }} /></td>
        <td className="px-6 py-4"><Shim className="h-3.5" style={{ width: 32 }} /></td>
        <td className="px-6 py-4"><Shim className="h-5 w-5 rounded-full" /></td>
      </tr>
    ))}
  </>
);

export const ChartsSkeleton = () => (
  <div className="mx-auto w-full max-w-6xl px-4">
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <div className="rounded-2xl border border-zinc-700/50 p-5 bg-surface-card flex flex-col gap-4">
        <div className="flex justify-between">
          <Shim className="h-3.5" style={{ width: 100 }} />
          <Shim className="h-3" style={{ width: 60 }} />
        </div>
        <div className="flex justify-center items-center h-48">
          <Shim style={{ width: 140, height: 140, borderRadius: '50%' }} />
        </div>
      </div>
      <div className="rounded-2xl border border-zinc-700/50 p-5 bg-surface-card lg:col-span-2 flex flex-col gap-4">
        <div className="flex justify-between">
          <Shim className="h-3.5" style={{ width: 80 }} />
          <Shim className="h-3" style={{ width: 60 }} />
        </div>
        <div className="flex items-end gap-2 h-48 pt-4">
          {Array.from({ length: 7 }).map((_, i) => (
            <Shim
              key={i}
              className="flex-1 rounded-sm"
              style={{ height: `${40 + Math.sin(i) * 30 + 40}px` }}
            />
          ))}
        </div>
      </div>
    </div>
  </div>
);
