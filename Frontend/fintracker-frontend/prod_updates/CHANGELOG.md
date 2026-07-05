# Fintracker Design System — Production Migration

A PR-style changelog you can hand to Claude Code (or apply by hand). All changes are **additive** — no existing component prop signatures are broken; RTK Query hooks and selectors are untouched; routes in `AppRoutes` keep working.

## TL;DR

- Adds a single MUI dark theme so the **light auth pages match the rest of the app**.
- Extends Tailwind with a brand/surface/fg palette so utility classes like `bg-surface-card` / `text-brand` work everywhere.
- Replaces the 2 emoji (✏️ 🗑️) and 2 unicode arrows with proper MUI icons.
- Refreshes every dashboard, expense, and auth component to use the design tokens.
- Keeps Framer Motion timings, accessibility labels, and TypeScript strict.

## File map

| Action | Path | Notes |
|---|---|---|
| **REPLACE** | `tailwind.config.js` | Adds `brand.*`, `surface.*`, `fg.*`, `chart-*` colors + radii + shimmer keyframe. |
| **NEW** | `src/theme/fintrackerTheme.ts` | MUI v6 dark theme (palette + components overrides). |
| **REPLACE** | `src/main.tsx` | Wraps `<App>` with `<ThemeProvider>` + `<CssBaseline>`. **Adjust the `./store` import path if your store lives elsewhere.** |
| **REPLACE** | `src/styles/SignUpContainer.tsx` | Kills the light radial gradient → dark page bg. |
| **REPLACE** | `src/styles/card.tsx` | Card surface uses dark tokens. |
| **REPLACE** | `src/Pages/LoginPage.tsx` | Drops local `<CssBaseline>`, uses theme. Adds form validation + a11y. |
| **REPLACE** | `src/Pages/RegisterUserPage.tsx` | Same pattern as Login. |
| **REPLACE** | `src/Pages/OtpVerificationPage.tsx` | Resend timer + dark theme. |
| **REPLACE** | `src/Component/dashboard/SideBar.tsx` | Tokens + active/disabled states. |
| **REPLACE** | `src/Component/dashboard/TopBar.tsx` | Avatar initial + Logout menu (no more AccountCircle). |
| **REPLACE** | `src/Component/dashboard/BottomNav.tsx` | Mobile-only, brand active state. |
| **REPLACE** | `src/Component/personalexpense/DashBoardCards.tsx` | Eyebrow + tabular nums + trend pills with `TrendingUp`/`TrendingDown` icons. Exports `fmtCAD` helper. |
| **REPLACE** | `src/Component/personalexpense/DashboardCardSkeleton.tsx` | Tailwind shimmer (no injected style tag). |
| **REPLACE** | `src/Component/personalexpense/ExpenseList.tsx` | Replaces ✏️ / 🗑️ with `EditOutlined` / `DeleteOutline`. |
| **REPLACE** | `src/Component/personalexpense/AddExpense.tsx` | MUI `<Dialog>` with category grid, replaces hand-rolled modal. |
| **REPLACE** | `src/Component/personalexpense/ExpenseCharts.tsx` | Recharts kept; tooltip + colors updated. |

## Step-by-step rollout

### 1. Theme + Tailwind tokens (foundation)
1. Replace `tailwind.config.js` from `prod_updates/tailwind.config.js`.
2. Create `src/theme/fintrackerTheme.ts` from `prod_updates/src/theme/fintrackerTheme.ts`.
3. Replace `src/main.tsx`. **Verify `./store` import path matches yours.**
4. Run `pnpm dev` (or `npm run dev`). The whole app should now be dark; nothing should crash.

### 2. Auth pages (fixes light/dark inconsistency)
5. Replace `src/styles/SignUpContainer.tsx` and `src/styles/card.tsx`.
6. Replace `src/Pages/LoginPage.tsx`, `RegisterUserPage.tsx`, `OtpVerificationPage.tsx`.
   **Adjust `useLoginMutation` / `useRegisterMutation` / `useVerifyOtpMutation` / `useResendOtpMutation` import paths to your `authApi` slice.**
7. Hit `/login` and `/register` — they should now match the dashboard's dark theme. **This solves the "light auth pages" issue you flagged.**

### 3. Navigation
8. Replace `SideBar.tsx`, `TopBar.tsx`, `BottomNav.tsx`.
   **Adjust `useAppSelector` import in TopBar.tsx to your `store/hooks` path.**
   **Adjust `useLogoutMutation` import.**

### 4. Dashboard
9. Replace `DashBoardCards.tsx` and `DashboardCardSkeleton.tsx`.
   The `rows` prop shape is `{ today, month, topCategory?, count }[]` — your existing memoized aggregator can map straight onto it.
10. Replace `ExpenseCharts.tsx`. The default export takes `categoryData` + `dailyData`; named exports `CategoryDonut` and `DailySpendLine` let you embed individually.

### 5. Expense flows
11. Replace `ExpenseList.tsx`. Update the `Expense` type import to your project (`../../types` or wherever).
12. Replace `AddExpense.tsx`. Same `Expense` type import.

### 6. Cleanup
13. Search for `'✏️'` and `'🗑️'` and remove any leftover usages — only the ExpenseList row menu had them; the new file uses `EditOutlinedIcon` / `DeleteOutlineIcon`.
14. Search for `tracking-tight` mismatches and replace ad-hoc colors with tokens (`text-zinc-400` → `text-fg-3`, `bg-zinc-900` → `bg-surface-card`, etc.).
15. `pnpm tsc --noEmit` to confirm strict mode still passes.

## Things to verify

- [ ] Login → dashboard transition stays dark (no white flash).
- [ ] Add Expense modal opens with the spring motion you had (Framer Motion timings preserved at the `Dialog` level by MUI's transition).
- [ ] Edit/Delete row menu uses outlined icons.
- [ ] Trend pills on dashboard cards have `↑/↓` MUI icons, not unicode.
- [ ] OTP page autofocus + 60s resend countdown work.
- [ ] No `console.warn` about unknown HTML attributes (we don't pass non-DOM props to native elements).

## Known follow-ups (not in this batch)

- Real Groups + Settings pages are still placeholders.
- Self-hosted fonts: Google Fonts `@import` is left in `src/index.css`. Swap to `@fontsource/inter` + `@fontsource/poppins` if you want offline-safe.
- Mobile bottom-sheet variant of `AddExpense`: MUI Dialog gives you a centered modal everywhere; if you want the slide-up sheet on mobile, use `<Dialog fullScreen>` with a transition or migrate to `react-modal-sheet`.
