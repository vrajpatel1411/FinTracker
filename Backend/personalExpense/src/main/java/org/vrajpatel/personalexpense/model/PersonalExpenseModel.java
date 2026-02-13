package org.vrajpatel.personalexpense.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="personal_expenses")
@Data
public class PersonalExpenseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="expense_id")
    private UUID expenseId;

    @Column(name="title")
    private String title;

    @Column(name="user_id", insertable=false, updatable=false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name="description")
    private String description;

    @Column(name="amount")
    private BigDecimal amount;

    @Column(name="expense_date")
    private Date expenseDate;

    @Column(name="created_at")
    private Date createdAt=new Date();

    @Column(name="updated_at")
    private Date updatedAt=new Date();


    @Column(name="deleted")
    private Boolean deleted=false;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    @JsonIgnore
    private CategoriesModel category;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="receipt_id")
    @JsonIgnore
    private receiptModel receipt;

    @Override
    public String toString() {
        return "PersonalExpenseModel{" +
                "amount=" + amount +
                ", expenseId=" + expenseId +
                ", title='" + title + '\'' +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                ", expenseDate=" + expenseDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                ", category=" + category +
                ", receipt=" + receipt +
                '}';
    }
}
