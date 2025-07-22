package org.vrajpatel.personalexpense.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name="personal_expenses")
@RequiredArgsConstructor
public class PersonalExpenseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="expense_id")
    private UUID expenseId;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name="amount")
    private Double amount;

    @Column(name="expense_date")
    private Date expenseDate;

    @Column(name="created_at")
    private Date createdAt;

    @Column(name="updated_at")
    private Date updatedAt;

    @Column(name="deleted")
    private Boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoriesModel category;
}
