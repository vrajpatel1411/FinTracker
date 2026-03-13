package org.vrajpatel.personalexpense.Service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Exception.Types.UserNotFoundError;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.responseDto.AnalyticsDTO;
import org.vrajpatel.personalexpense.responseDto.CategoriesDTO;
import org.vrajpatel.personalexpense.responseDto.DailyExpenseDTO;
import org.vrajpatel.personalexpense.responseDto.ExpenseSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

@Service
public class AnalyticsService {

    private final Logger log = Logger.getLogger(AnalyticsService.class.getName());

    private final PersonalExpenseRepository personalExpenseRepository;

    private final Executor analyticsTaskExecutor;

    AnalyticsService(PersonalExpenseRepository personalExpenseRepository, @Qualifier("analyticsExecutor") Executor analyticsTaskExecutor) {
        this.personalExpenseRepository = personalExpenseRepository;
        this.analyticsTaskExecutor = analyticsTaskExecutor;
    }


    @Cacheable(value="analytics", key="#userId + '_' + #todayDate")
    public AnalyticsDTO generateAnalytics(String userEmail, String userId, LocalDate todayDate) throws UserNotFoundError {
        if(userId == null || userId.isEmpty()) {
            throw new UserNotFoundError("User Id or User Email is empty");
        }
        CompletableFuture<ExpenseSummary> expenseSummaryFuture =
                CompletableFuture.supplyAsync(() -> this.getExpenseSummary(userId, todayDate),analyticsTaskExecutor);

        CompletableFuture<CategoriesDTO> categoriesFuture =
                CompletableFuture.supplyAsync(() -> this.getTopCategoriesWithOthers(userId, todayDate),analyticsTaskExecutor);

        CompletableFuture<DailyExpenseDTO> dailyExpenseFuture =
                CompletableFuture.supplyAsync(() -> this.getSevenDaysExpense(userId, todayDate),analyticsTaskExecutor);
       CompletableFuture.allOf(expenseSummaryFuture, categoriesFuture, dailyExpenseFuture);
        AnalyticsDTO analyticsDTO = new AnalyticsDTO();
        analyticsDTO.setExpenseSummary(expenseSummaryFuture.join());
        analyticsDTO.setCategoriesDTO(categoriesFuture.join());
        analyticsDTO.setDailyExpenseDTO(dailyExpenseFuture.join());
        return analyticsDTO;
    }

    private ExpenseSummary getExpenseSummary(String userId, LocalDate todayDate)  {
        LocalDate firstDayOfMonth = todayDate.withDayOfMonth(1);
        LocalDate lastDayOfMonth = todayDate.withDayOfMonth(todayDate.lengthOfMonth());
        return this.personalExpenseRepository.findSummary(UUID.fromString(userId),todayDate,firstDayOfMonth,lastDayOfMonth);
    }

    // Service
    public CategoriesDTO getTopCategoriesWithOthers(String userId, LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());

        List<Object[]> all = personalExpenseRepository.findCategoryWiseSpending(
                UUID.fromString(userId), firstDay, lastDay
        );
        if (all.isEmpty()) return new CategoriesDTO();
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        all.stream()
                .limit(5)
                .forEach(row -> result.put((String) row[0], (BigDecimal) row[1]));
        result.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEachOrdered(e -> {
                    result.remove(e.getKey());
                    result.put(e.getKey(), e.getValue());
                });
        if (all.size() > 5) {
            BigDecimal othersCount = all.stream()
                    .skip(5)
                    .map(row -> (BigDecimal) row[1])  // Fix: cast to BigDecimal not double
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put("Others", othersCount);
        }
        CategoriesDTO dto=new CategoriesDTO();
        dto.setCategories(result);
        return dto;
    }

    public DailyExpenseDTO getSevenDaysExpense(String userId, LocalDate date){
        List<Object[]> result = this.personalExpenseRepository.getExpenseCountForLast7Days(userId,date);
        Map<LocalDate, BigDecimal> finalResult = new LinkedHashMap<>();
        result.forEach(row -> finalResult.put((LocalDate) row[0], ((BigDecimal) row[1])));
        DailyExpenseDTO dto=new DailyExpenseDTO();
        dto.setDailyExpense(finalResult);
        return dto;
    }
}

