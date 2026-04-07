import React from 'react';
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
      className={`inline-flex items-center gap-0.5 text-xs font-semibold px-1.5 py-0.5 rounded-full ${
        isUp
          ? 'bg-emerald-500/15 text-emerald-400'
          : 'bg-red-500/15 text-red-400'
      }`}
    >
      {isUp ? '↑' : '↓'} {pct}%
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

const DashBoardCards: React.FC<DashBoardCardsProps> = ({ isLoading,category,totalSpending,monthlySpending,transactionsCount  }) => {
  if (isLoading) return <DashboardCardsSkeleton />;
    const DashBoardCardItems: CardItem[] = [
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
  ];
  return (
    <div className="mx-auto w-full max-w-5xl px-4">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {DashBoardCardItems.map((item, index) => (
          <div
            key={index}
            className="group flex flex-col gap-1.5 rounded-xl p-3 sm:p-5 bg-[#17171c] border border-zinc-700/50
                       hover:border-zinc-500/80 hover:bg-[#1d1d24] transition-all duration-200 w-full"
          >
            {/* Title */}
            <p className="text-[11px] font-semibold tracking-widest text-zinc-500 uppercase">
              {item.title}
            </p>

            {/* Main value */}
            <div className="flex items-baseline gap-1 mt-1">
              {item.category && (
                <span className="text-base font-semibold text-teal-400">
                  {item.category}
                </span>
              )}
              {item.currency && (
                <span className="text-sm font-medium text-zinc-400">{item.currency}</span>
              )}
              <span className="text-xl sm:text-2xl font-bold tracking-tight text-white">
                {item?.amount?.toLocaleString()}
              </span>
            </div>

            {/* Footer */}
            <div className="flex items-center gap-2 mt-1 flex-wrap">
              {item.minorText && (
                <span className="text-xs text-zinc-500">{item.minorText}</span>
              )}
              <TrendBadge trend={item.trend} pct={item.percentageChange} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DashBoardCards;