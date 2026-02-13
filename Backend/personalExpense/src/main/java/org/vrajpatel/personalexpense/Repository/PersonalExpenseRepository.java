package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

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
    public Page<PersonalExpenseDto> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
