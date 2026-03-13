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
  fileName?: string;
  fileType?: string;
  fileLength?: number;
  file?: File;
  deleteReceipt?: boolean; // Flag to indicate if existing receipt should be deleted (used in edit mode) 
//   receipt: File | string; // Base64 string for the receipt image
};

export type PersonalExpense = ExpenseCore & {
 
  categoryName: string;
  categoryColor: string;
  receiptId: number | null;
  isReceipt: boolean;
  receiptUrl: string | undefined;
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