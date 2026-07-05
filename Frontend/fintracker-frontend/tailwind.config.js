/** @type {import('tailwindcss').Config} */
// NOTE: This file is NOT used. The project uses @tailwindcss/vite (Tailwind v4),
// which ignores tailwind.config.js. All design tokens live in src/index.css via @theme {}.
export default {
    content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
    darkMode: "class",
    theme: {
        extend: {
            colors: {
                brand: {
                    DEFAULT: "#009689",
                    dark: "#005a52",
                    glow: "#14b8a6",
                    accent: "#563787",
                },
                surface: {
                    page: "#0f0f14",
                    base: "#121215",
                    subtle: "#13131a",
                    card: "#17171c",
                    "card-alt": "#1d1d24",
                    "card-hover": "#1f1f24",
                    tooltip: "#1e1e28",
                    elevated: "#2a2a30",
                    "menu-hover": "#3d3d45",
                },
                fg: {
                    1: "#f4f4f5",
                    2: "#d4d4d8",
                    3: "#a1a1aa",
                    4: "#71717a",
                    5: "#52525b",
                },
                positive: "#34d399",
                negative: "#f87171",
                warning: "#F2994A",
                "receipt-yes": "#10b981",
                "receipt-no": "#ef4444",
                "chart-1": "#009689",
                "chart-2": "#8b5cf6",
                "chart-3": "#f59e0b",
                "chart-4": "#3b82f6",
                "chart-5": "#ef4444",
                "chart-6": "#10b981",
            },
            fontFamily: {
                sans: ["Inter", "system-ui", "sans-serif"],
                display: ["Poppins", "Inter", "system-ui", "sans-serif"],
                mono: ["ui-monospace", "SFMono-Regular", "monospace"],
            },
            borderRadius: {
                xs: "4px",
            },
            keyframes: {
                shimmer: {
                    "0%": { backgroundPosition: "-600px 0" },
                    "100%": { backgroundPosition: "600px 0" },
                },
            },
            animation: {
                shimmer: "shimmer 1.5s ease-in-out infinite",
            },
            width: {
                58: "14.5rem",
            },
        },
    },
    plugins: [],
};
