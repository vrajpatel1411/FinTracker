package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsDTO implements Serializable {
    private ExpenseSummary expenseSummary;
    private CategoriesDTO categoriesDTO;
    private DailyExpenseDTO dailyExpenseDTO;
}
