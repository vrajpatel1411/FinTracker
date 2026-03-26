import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import react from 'eslint-plugin-react'

export default tseslint.config(
  
  { ignores: ['dist'] },

  {
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      ...tseslint.configs.recommendedTypeChecked, // ← enables type-aware rules
    ],

    files: ['**/*.{ts,tsx}'],

    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parserOptions: {
        project: './tsconfig.app.json',          // ← required for type-aware rules
        tsconfigRootDir: import.meta.dirname,
      },
    },

    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
      'react': react,
    },

    settings: {
      react: {
        version: 'detect',
      },
    },

    rules: {
      // ─── Keep your existing rules ───────────────────────────────
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],

      // ─── Caught real bugs in your codebase ──────────────────────

      // getState() as any in getExpenses.ts
      '@typescript-eslint/no-explicit-any': 'error',

      // == instead of === in addExpense, deleteExpense
      'eqeqeq': ['error', 'always'],

      // console.log left in deleteExpense, registerUser, VerifyOTP
      'no-console': ['warn', { allow: ['warn', 'error'] }],

      // missing useEffect deps across your auth pages
      'react-hooks/exhaustive-deps': 'error',

      // setLoading(false) placed after dispatch() synchronously
      '@typescript-eslint/no-floating-promises': 'error',

      // action.error.message instead of action.payload?.message
      '@typescript-eslint/no-unsafe-member-access': 'warn',

      // returning error response as fulfilled payload in loginUser
      '@typescript-eslint/no-unsafe-return': 'warn',


      // ─── Async correctness ───────────────────────────────────────

      // prevents awaiting inside a loop — use Promise.all instead
      'no-await-in-loop': 'error',

      // catches async functions that never actually await anything
      'require-await': 'error',


      // ─── TypeScript strictness ───────────────────────────────────

      // size || 10 breaks when size=0, prefer size ?? 10
      '@typescript-eslint/prefer-nullish-coalescing': 'error',

      // a && a.b → prefer a?.b
      '@typescript-eslint/prefer-optional-chain': 'error',

      // prevents variables declared but never used
      // _ prefix exemption so you can write (_arg) => {} intentionally
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],


      // ─── React ───────────────────────────────────────────────────

      // missing key prop in your ExpenseList .map()
      'react/jsx-key': 'error',

      // defining components inside render causes remounts
      'react/no-unstable-nested-components': 'error',


      // ─── Code quality ────────────────────────────────────────────

      'no-unreachable': 'error',
      'no-fallthrough': 'error',

      // prevents direct param mutation EXCEPT Immer slice state
      'no-param-reassign': ['error', {
        props: true,
        ignorePropertyModificationsFor: ['state'],
      }],
    },
  },

)