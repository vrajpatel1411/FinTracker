// SkeletonLoaders.tsx
import React from 'react';

// Inject shimmer keyframes once
const ShimmerStyle: React.FC = () => (
  <style>{`
    @keyframes shimmer {
      0%   { background-position: -600px 0; }
      100% { background-position:  600px 0; }
    }
    .skeleton-shimmer {
      background: linear-gradient(90deg, #1e1e26 25%, #28283a 50%, #1e1e26 75%);
      background-size: 600px 100%;
      animation: shimmer 1.5s ease-in-out infinite;
    }
  `}</style>
);

interface SkeletonBoxProps {
  className?: string;
  style?: React.CSSProperties;
}

export const SkeletonBox: React.FC<SkeletonBoxProps> = ({ className = '', style }) => (
  <div className={`skeleton-shimmer rounded-md ${className}`} style={style} />
);

// ── Dashboard Cards ─────────────────────────────────────────────────────────

export const DashboardCardSkeleton: React.FC = () => (
  <div className="flex flex-col gap-3 rounded-xl p-5 bg-[#17171c] border border-zinc-700/50 w-full min-h-[110px]">
    <SkeletonBox className="h-3 w-24" />
    <SkeletonBox className="h-8 w-28 mt-1" />
    <SkeletonBox className="h-3 w-20 mt-1" />
  </div>
);

export const DashboardCardsSkeleton: React.FC = () => (
  <>
    <ShimmerStyle />
    <div className="mx-auto w-full max-w-5xl px-4">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <DashboardCardSkeleton key={i} />
        ))}
      </div>
    </div>
  </>
);

// ── Expense Rows ─────────────────────────────────────────────────────────────

export const ExpenseRowsSkeleton: React.FC<{ rows?: number }> = ({ rows = 6 }) => (
  <>
    <ShimmerStyle />
    {Array.from({ length: rows }).map((_, i) => (
      <tr key={i} className="bg-[#17171c] border-b border-zinc-800">
        <td className="px-6 py-4">
          <div className="flex flex-col gap-1.5">
            <SkeletonBox style={{ height: 14, width: 120 }} />
            <SkeletonBox style={{ height: 11, width: 80 }} />
          </div>
        </td>
        <td className="px-6 py-4"><SkeletonBox style={{ height: 14, width: 72 }} /></td>
        <td className="px-6 py-4"><SkeletonBox style={{ height: 14, width: 80 }} /></td>
        <td className="px-6 py-4"><SkeletonBox style={{ height: 14, width: 56 }} /></td>
        <td className="px-6 py-4"><SkeletonBox style={{ height: 14, width: 32 }} /></td>
        <td className="px-6 py-4"><SkeletonBox style={{ height: 20, width: 20, borderRadius: '50%' }} /></td>
      </tr>
    ))}
  </>
);

// ── Chart Skeleton ───────────────────────────────────────────────────────────

export const ChartsSkeleton: React.FC = () => (
  <>
    <ShimmerStyle />
    <div className="mx-auto w-full max-w-6xl px-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {/* Pie chart skeleton */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-[#17171c] flex flex-col gap-4">
          <div className="flex justify-between">
            <SkeletonBox style={{ height: 14, width: 100 }} />
            <SkeletonBox style={{ height: 12, width: 60 }} />
          </div>
          <div className="flex justify-center items-center h-48">
            <SkeletonBox style={{ width: 140, height: 140, borderRadius: '50%' }} />
          </div>
        </div>
        {/* Line chart skeleton */}
        <div className="rounded-2xl border border-zinc-700/50 p-5 bg-[#17171c] lg:col-span-2 flex flex-col gap-4">
          <div className="flex justify-between">
            <SkeletonBox style={{ height: 14, width: 80 }} />
            <SkeletonBox style={{ height: 12, width: 60 }} />
          </div>
          <div className="flex items-end gap-2 h-48 pt-4">
            {Array.from({ length: 7 }).map((_, i) => (
              <SkeletonBox
                key={i}
                style={{
                  flex: 1,
                  height: `${40 + Math.sin(i) * 30 + 40}px`,
                  borderRadius: 4,
                }}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  </>
);