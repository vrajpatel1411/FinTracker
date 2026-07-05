package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Getter @Setter
public class DailyExpenseDTO implements Serializable {

    private Map<LocalDate, BigDecimal> dailyExpense;

    public DailyExpenseDTO() {
        this.dailyExpense = new HashMap<>();
    }
}
