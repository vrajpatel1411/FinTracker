interface link {
    rel: string;
    href: string;
}

export interface PersonalExpense{
    expenseId: number;
    title: string;
    description: string;
    amount: number;
    expenseDate: string;
    categoryId: string;
    categoryName: string;
    categoryColor: string;
    receiptId: null | number;
    receiptUrl: null | string;
}

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