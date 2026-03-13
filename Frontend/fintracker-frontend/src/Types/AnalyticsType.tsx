export interface AnalyticsType {
    categoriesDTO: categoriesDTO;
    category: Record<string, number> | null;
    dailyExpenseDTO: dailyExpensesDTO;
    monthlyExpense: number;
    todayExpense: number;
    totalTransactions: number;
}

export interface categoriesDTO {
    categories: categoriesType[];
}
export interface categoriesType {
    name: string;
    value: number;

}

export interface dailyExpensesDTO {
    dailyExpense: dailyExpensesType[];
}
export interface dailyExpensesType {
    date: string;
    amount: number;
}