export interface AnalyticsType {
    categoriesDTO: categoriesDTO;
    dailyExpenseDTO: dailyExpensesDTO;
    expenseSummary: expenseSummaryType;
}

export interface expenseSummaryType {
    monthlyExpense: number;
    todayExpense: number;
    totalTransactions: number;
}

export interface categoriesDTO {
    categories: Record<string, number>;  // ✅ was categoriesType[]
}

export interface categoriesType {
    name: string;
    value: number;
}

export interface dailyExpensesDTO {
    dailyExpense: Record<string, number>;  // ✅ was dailyExpensesType[]
}

export interface dailyExpensesType {
    date: string;
    amount: number;
}