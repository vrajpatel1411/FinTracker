import { createApi, fetchBaseQuery, BaseQueryFn, FetchArgs, FetchBaseQueryError, BaseQueryApi } from "@reduxjs/toolkit/query/react";
import { CategoryType } from "../../Types/category";
import { AnalyticsType } from "../../Types/AnalyticsType";
import PersonalExpenseResponseType, { AddExpensePayload, PersonalExpenseData } from "../../Types/PersonalExpenseListType";

type BaseQuery = BaseQueryFn<string | FetchArgs, object, FetchBaseQueryError>;

async function upsertExpense(
  args: AddExpensePayload,
  baseQuery: BaseQuery,
  api: BaseQueryApi,
  extraOptions: object,
  isEdit: boolean
): Promise<{ data: undefined } | { error: FetchBaseQueryError }> {
  const { file, ...jsonPayload } = args;

  const result = await baseQuery(
    {
      url: isEdit ? `/expense/${args.expenseId}` : "/",
      method: isEdit ? "PATCH" : "POST",
      body: jsonPayload,
    },
    api,
    extraOptions
  );

  if (result.error) return { error: result.error };

  const response = result.data as {
    data?: { receiptId?: number; receiptUrl?: string };
  };

  if (response.data?.receiptId && response.data?.receiptUrl && file) {
    await fetch(response.data.receiptUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": args.fileType ?? "" },
    });
  }

  return { data: undefined };
}

export const expenseApi = createApi({
  reducerPath: "expenseApi",
  baseQuery: fetchBaseQuery({
    baseUrl: import.meta.env.VITE_PERSONAL_EXPENSE_URL as string,
    credentials: "include",
  }),
  tagTypes: ["Expenses", "Analytics", "Categories"],
  endpoints: (builder) => ({
    getCategories: builder.query<CategoryType[], void>({
      query: () => "/category/",
      transformResponse: (response: { data: CategoryType[]; status: string }) => response.data,
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
      query: (id) => ({ url: `/expense/${id}`, method: "DELETE" }),
      invalidatesTags: ["Expenses", "Analytics"],
    }),
    addExpense: builder.mutation<void, AddExpensePayload>({
  queryFn: (args, api, extraOptions, baseQuery) =>
    upsertExpense(args, baseQuery as BaseQuery, api, extraOptions, false),
}),

editExpense: builder.mutation<void, AddExpensePayload>({
  queryFn: (args, api, extraOptions, baseQuery) =>
    upsertExpense(args, baseQuery as BaseQuery, api, extraOptions, true),
}),
  }),
});

export const {
  useGetCategoriesQuery,
  useGetAnalyticsQuery,
  useGetExpensesQuery,
  useDeleteExpenseMutation,
  useAddExpenseMutation,
  useEditExpenseMutation,
} = expenseApi;
