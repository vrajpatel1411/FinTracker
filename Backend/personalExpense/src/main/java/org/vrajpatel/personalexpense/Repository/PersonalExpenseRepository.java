package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.model.User;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface PersonalExpenseRepository extends JpaRepository<PersonalExpenseModel, UUID> {

//    @Query("""
//  select p
//  from PersonalExpenseModel p
//  where p.userId = :userId
//    and p.deleted = false
//""")
    @Query("""
    select new org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto(
        p.expenseId,
        p.title,
        p.description,
        p.amount,
        p.expenseDate,
        c.categoryId,
        c.categoryName,
        c.categoryColor,
        r.receiptId,
        r.receiptFileUrl
    )
    
    from PersonalExpenseModel p 
    left join p.category c
    left join p.receipt r
    where p.userId = :userId
    and p.deleted = false
""")
    Page<PersonalExpenseDto> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        Select sum(p.amount) from PersonalExpenseModel p where p.userId = :userId and p.deleted = false and p.expenseDate=:todayDate
    """)
    Double findTodayExpense(@Param("userId") UUID userId, @Param("todayDate") LocalDate todayDate);

    @Query("""
    Select sum(p.amount) from PersonalExpenseModel p where p.userId = :userId and p.expenseDate between :fromDate and :toDate and p.deleted = false
""")
    Double findMonthlyExpense(@Param("userId") UUID userId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
    SELECT c.categoryName, sum(p.amount)
    FROM PersonalExpenseModel p
    JOIN p.category c
    WHERE p.userId = :userId
    AND p.deleted = false
    AND p.expenseDate BETWEEN :firstDay AND :lastDay
    GROUP BY c.categoryName
    ORDER BY COUNT(p) DESC
""")
    List<Object[]> findCategoryTransactionCount(
            @Param("userId") UUID userId,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay
    );

    List<PersonalExpenseModel> user(User user);

    @Query("""
        Select 
            c.categoryName,sum(p.amount) as total
            from PersonalExpenseModel p
            JOIN p.category c
            WHERE p.userId = :userId
            AND p.deleted = false
            AND p.expenseDate BETWEEN :firstDay AND :lastDay
            GROUP BY c.categoryName
            ORDER BY total DESC
            limit 1 
""")
    List<Object[]> findTopCategory(UUID userId, LocalDate firstDay, LocalDate lastDay);

    @Query("""
        Select Count(*) from PersonalExpenseModel p where p.userId = :userId and p.deleted = false and p.expenseDate between :firstDate and :lastDate
""")
    Long getTotalTransactionInAMonth(@Param("userId") UUID uuid,@Param("firstDate") LocalDate firstDate,@Param("lastDate") LocalDate lastDate);

    @Query(value = """
    SELECT 
        d.expense_date::date,
        COALESCE(sum(p.amount), 0) as total
    FROM 
        generate_series(
            CAST(:givenDate AS date) - INTERVAL '6 days',
            CAST(:givenDate AS date),
            INTERVAL '1 day'
        ) AS d(expense_date)
    LEFT JOIN personal_expenses p 
        ON p.expense_date = d.expense_date
        AND p.user_id = CAST(:userId AS uuid)
        AND p.deleted = false
    GROUP BY d.expense_date
    ORDER BY d.expense_date
    """, nativeQuery = true)
    List<Object[]> getExpenseCountForLast7Days(
            @Param("userId") String userId,
            @Param("givenDate") LocalDate givenDate
    );

}
