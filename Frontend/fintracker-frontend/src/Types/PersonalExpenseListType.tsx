interface link {
    rel: string;
    href: string;
}

type ExpenseCore = {
  expenseId?: string;
  title: string;
  description: string;
  amount: number;
  expenseDate: string;
  categoryId: string;

};

export type AddExpensePayload = ExpenseCore & {
  isReceipt: boolean;
  receiptFile?: File;
};

export type PersonalExpense = ExpenseCore & {
 
  categoryName: string;
  categoryColor: string;
  receiptId: number | null;
  isReceipt: boolean;
  receiptUrl: string | null;
};


export interface PersonalExpenseListType{
    expenseList: PersonalExpense[];
}

export interface pageType{
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface PersonalExpenseResponseType{
    status: string,
    data: {
        content: PersonalExpense[];
        links: link[];
        page: pageType;
    }
}


export default PersonalExpenseResponseType;