package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ExpenseSummary implements Serializable {
    private Double todayExpense;
    private Double monthlyExpense;
    private Long totalTransactions;

    public ExpenseSummary(Double todayExpense, Double monthlyExpense, Long totalTransactions) {
        this.todayExpense =todayExpense;
        this.monthlyExpense = monthlyExpense;
        this.totalTransactions = totalTransactions;
    }

}
