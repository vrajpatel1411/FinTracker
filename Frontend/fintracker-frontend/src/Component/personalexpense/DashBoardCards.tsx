import React, { useMemo } from 'react';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import { DashboardCardsSkeleton } from './DashboardCardSkeleton';
import { categoriesType } from '../../Types/AnalyticsType';

interface CardItem {
  title: string;
  amount: number;
  currency?: string;
  category?: string;
  minorText?: string;
  percentageChange?: string;
  trend?: 'up' | 'down';
}

interface TrendBadgeProps {
  trend?: 'up' | 'down';
  pct?: string;
}

const TrendBadge: React.FC<TrendBadgeProps> = ({ trend, pct }) => {
  if (!pct) return null;
  const isUp = trend === 'up';
  return (
    <span
      className={`inline-flex items-center gap-0.5 text-xs font-semibold px-2 py-0.5 rounded-full ${
        isUp ? 'bg-positive/15 text-positive' : 'bg-negative/15 text-negative'
      }`}
    >
      {isUp
        ? <TrendingUpIcon sx={{ fontSize: 12 }} />
        : <TrendingDownIcon sx={{ fontSize: 12 }} />}
      {pct}%
    </span>
  );
};

interface DashBoardCardsProps {
  isLoading?: boolean;
  totalSpending: number;
  monthlySpending: number;
  transactionsCount: number;
  category: categoriesType | null;
}

const DashBoardCards: React.FC<DashBoardCardsProps> = ({ isLoading, category, totalSpending, monthlySpending, transactionsCount }) => {
  const items = useMemo<CardItem[]>(() => [
    {
      title: "TODAY'S SPEND",
      amount: totalSpending,
      currency: 'CA$',
      minorText: 'vs yesterday:',
      percentageChange: '5',
      trend: 'up',
    },
    {
      title: 'THIS MONTH',
      amount: monthlySpending,
      currency: 'CA$',
      minorText: 'vs last month:',
      percentageChange: '10',
      trend: 'up',
    },
    {
      title: 'TOP CATEGORY',
      category: category?.name ?? 'N/A',
      amount: category?.value ?? 0,
      currency: 'CA$',
      minorText: "of today's spend",
      percentageChange: '20',
    },
    {
      title: 'TRANSACTIONS',
      amount: transactionsCount,
      minorText: 'Personal only',
    },
  ], [totalSpending, monthlySpending, category, transactionsCount]);

  if (isLoading) return <DashboardCardsSkeleton />;

  return (
    <div className="mx-auto w-full max-w-6xl px-4">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {items.map((item, index) => (
          <div
            key={index}
            className="flex flex-col gap-1.5 rounded-xl p-3 sm:p-5 bg-surface-card border border-zinc-700/50
                       hover:border-zinc-500/80 hover:bg-surface-card-alt transition-all duration-200 w-full min-h-[110px]"
          >
            <span className="text-[11px] font-semibold tracking-widest text-fg-4 uppercase">
              {item.title}
            </span>
            <div className="flex items-baseline gap-1 mt-1">
              {item.category && (
                <span className="text-base font-semibold text-brand-glow mr-1">{item.category}</span>
              )}
              {item.currency && (
                <span className="text-sm font-medium text-fg-3">{item.currency}</span>
              )}
              <span className="text-xl sm:text-2xl font-bold tracking-tight text-fg-1 tabular-nums">
                {item?.amount?.toLocaleString()}
              </span>
            </div>
            <div className="flex items-center gap-2 mt-1 flex-wrap">
              {item.minorText && <span className="text-xs text-fg-4">{item.minorText}</span>}
              <TrendBadge trend={item.trend} pct={item.percentageChange} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DashBoardCards;
