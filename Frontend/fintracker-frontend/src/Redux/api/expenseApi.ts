import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { CategoryType } from "../../Types/category";
import { AnalyticsType } from "../../Types/AnalyticsType";
import PersonalExpenseResponseType, { AddExpensePayload, PersonalExpenseData } from "../../Types/PersonalExpenseListType";

export const expenseApi = createApi({
    reducerPath: "expenseApi",
    baseQuery: fetchBaseQuery({
        baseUrl: import.meta.env.VITE_PERSONAL_EXPENSE_URL as string,
        credentials: "include",
    }),
    tagTypes: ["Expenses","Analytics","Categories"],
    endpoints:(builder)=>({
        getCategories: builder.query<CategoryType[],void>({
            query: () => "/category/",
            providesTags: ["Categories"],
        }),
        getAnalytics: builder.query<AnalyticsType, string>({
            query: (date) => `/analytics?date=${date}`,
            providesTags: ["Analytics"],    
        }),

        getExpenses: builder.query<PersonalExpenseData, { page: number; size: number }>({
            query: ({ page, size }) => {
                const safePage = Math.max(0, page);
                const safeSize = Math.min(Math.max(1, size), 100);
                return `/?page=${safePage}&size=${safeSize}`;
            },
            transformResponse: (response: PersonalExpenseResponseType) => response.data!,
            providesTags: ["Expenses"],
        }),

        deleteExpense: builder.mutation<void, string>({
            query: (id) => ({
                url: `/expense/${id}`,
                method: "DELETE",
            }),
            invalidatesTags: ["Expenses", "Analytics"],
        }),

        addExpense: builder.mutation<void, AddExpensePayload>({
            queryFn: async (args, _api, _extraOptions, baseQuery) => {
                const { file, ...jsonPayload } = args;
                const result = await baseQuery({
                    url: "/",
                    method: "POST",
                    body: jsonPayload,
                });

                if (result.error) return { error: result.error };

                const response = result.data as { status: string; data?: { receiptId?: number; receiptUrl?: string } };
                if (response.data?.receiptId && response.data?.receiptUrl && file) {
                    await fetch(response.data.receiptUrl, {
                        method: "PUT",
                        body: file,
                        headers: { "Content-Type": args.fileType ?? "" },
                    });
                }

                return { data: undefined };
            },
            invalidatesTags: ["Expenses", "Analytics"],
        }),

        editExpense: builder.mutation<void, AddExpensePayload>({
            queryFn: async (args, _api, _extraOptions, baseQuery) => {
                const { file, ...jsonPayload } = args;

                const result = await baseQuery({
                    url: `/expense/${args.expenseId}`,
                    method: "PATCH",
                    body: jsonPayload,
                });

                if (result.error) return { error: result.error };

                const response = result.data as { status: string; data?: { receiptId?: number; receiptUrl?: string } };

                if (response.data?.receiptId && response.data?.receiptUrl && file) {
                    await fetch(response.data.receiptUrl, {
                        method: "PUT",
                        body: file,
                        headers: { "Content-Type": args.fileType ?? "" },
                    });
                }

                return { data: undefined };
            },
            invalidatesTags: ["Expenses", "Analytics"],
        }),

    })
})

export const {
    useGetCategoriesQuery,
    useGetAnalyticsQuery,
    useGetExpensesQuery,
    useDeleteExpenseMutation,
    useAddExpenseMutation,
    useEditExpenseMutation,
}= expenseApi;