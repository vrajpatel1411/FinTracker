package org.vrajpatel.personalexpense.Service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Exception.Types.UserNotFoundError;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.responseDto.AnalyticsDTO;
import org.vrajpatel.personalexpense.responseDto.CategoriesDTO;
import org.vrajpatel.personalexpense.responseDto.DailyExpenseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@Service
public class AnalyticsService {

    private final Logger log = Logger.getLogger(AnalyticsService.class.getName());

    private final PersonalExpenseRepository personalExpenseRepository;

    AnalyticsService(PersonalExpenseRepository personalExpenseRepository) {
        this.personalExpenseRepository = personalExpenseRepository;
    }


    @Cacheable(value="analytics", key="#userId + '_' + #todayDate")
    public AnalyticsDTO generateAnalytics(String userEmail, String userId, LocalDate todayDate) throws UserNotFoundError {
        log.info("Service Layer : Analytics request received");
        if(userId == null || userId.isEmpty()) {
            throw new UserNotFoundError("User Id or User Email is empty");
        }
        AnalyticsDTO analyticsDTO = new AnalyticsDTO();
        analyticsDTO.setTodayExpense(this.getTodayExpense(userId, todayDate));
        analyticsDTO.setMonthlyExpense(this.getMonthlyExpense(userId,todayDate));
        analyticsDTO.setCategoriesDTO(this.getTopCategoriesWithOthers(userId,todayDate));

        analyticsDTO.setCategory(this.getTopSpendingCategory(userId,todayDate));
        analyticsDTO.setTotalTransactions(this.getTotalTransactions(userId,todayDate));
        analyticsDTO.setDailyExpenseDTO(this.getSevenDaysExpense(userId,todayDate));
        return analyticsDTO;
    }

    private Double getTotalTransactions(String userId, LocalDate todayDate) {
        LocalDate firstDayOfMonth = todayDate.withDayOfMonth(1);
        LocalDate lastDayOfMonth = todayDate.withDayOfMonth(todayDate.lengthOfMonth());

        Long totalCounts=this.personalExpenseRepository.getTotalTransactionInAMonth(UUID.fromString(userId),firstDayOfMonth,lastDayOfMonth);
        return totalCounts.doubleValue();
    }

    public Double getMonthlyExpense(String userId, LocalDate todayDate){
        LocalDate firstDayOfMonth = todayDate.withDayOfMonth(1);
        LocalDate lastDayOfMonth = todayDate.withDayOfMonth(todayDate.lengthOfMonth());

        log.info("getMonthlyExpense: firstDayOfMonth = " + firstDayOfMonth + ", lastDayOfMonth = " + lastDayOfMonth);
        Double value = this.personalExpenseRepository.findMonthlyExpense(UUID.fromString(userId), firstDayOfMonth,lastDayOfMonth);

        return value == null ? 0 : value;
    }
    public Double getTodayExpense(String userId, LocalDate todayDate){

        Double value= this.personalExpenseRepository.findTodayExpense(UUID.fromString(userId), todayDate);
        log.info("Today expense value is "+value);
        return value!=null?value:0;
    }

    // Service
    public CategoriesDTO getTopCategoriesWithOthers(String userId, LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());

        List<Object[]> all = personalExpenseRepository.findCategoryTransactionCount(
                UUID.fromString(userId), firstDay, lastDay
        );

        if (all.isEmpty()) return new CategoriesDTO();

        // Build ordered map with top 5
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        all.stream()
                .limit(5)
                .forEach(row -> result.put((String) row[0], (BigDecimal) row[1]));

        // Merge rest into Others
        if (all.size() > 5) {
            BigDecimal othersCount = all.stream()
                    .skip(5)
                    .map(row -> (BigDecimal) row[1])  // Fix: cast to BigDecimal not double
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.put("Others", othersCount);

            // Re-sort
            result.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .forEachOrdered(e -> {
                        result.remove(e.getKey());
                        result.put(e.getKey(), e.getValue());
                    });
        }

        CategoriesDTO dto=new CategoriesDTO();

        dto.setCategories(result);

        return dto;
    }

    public Map<String, Double> getTopSpendingCategory(String userId, LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());

        List<Object[]> result = personalExpenseRepository.findTopCategory(
                UUID.fromString(userId), firstDay, lastDay
        );

        if (result.isEmpty()) return new HashMap<>();

        // ✅ Get first row from the list
        Object[] topCategory = result.get(0);

        Map<String, Double> map = new LinkedHashMap<>();
        map.put((String) topCategory[0], ((BigDecimal) topCategory[1]).doubleValue());

        return map;
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

