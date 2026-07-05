// src/Component/personalexpense/DashboardCardSkeleton.tsx
// Drop-in replacement. Tailwind shimmer instead of injected style tag.

export function DashboardCardsSkeleton() {
    return (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="bg-surface-card border border-zinc-700/50 rounded-lg p-5 min-h-[110px] flex flex-col gap-2.5">
                    <Shim w="w-24" h="h-3" />
                    <Shim w="w-28" h="h-7" />
                    <Shim w="w-20" h="h-3" />
                </div>
            ))}
        </div>
    );
}

export function ExpenseRowsSkeleton({ rows = 5 }: { rows?: number }) {
    return (
        <div className="bg-surface-card border border-zinc-700/50 rounded-2xl overflow-hidden">
            {Array.from({ length: rows }).map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-5 py-3.5 border-t border-zinc-700/60 first:border-t-0">
                    <Shim w="flex-1" h="h-4" />
                    <Shim w="w-20" h="h-4" />
                    <Shim w="w-20" h="h-4" />
                    <Shim w="w-16" h="h-4" />
                </div>
            ))}
        </div>
    );
}

function Shim({ w, h }: { w: string; h: string }) {
    return (
        <div
            className={`${w} ${h} rounded animate-shimmer`}
            style={{
                background: "linear-gradient(90deg, #1e1e26 25%, #28283a 50%, #1e1e26 75%)",
                backgroundSize: "600px 100%",
            }}
        />
    );
}
