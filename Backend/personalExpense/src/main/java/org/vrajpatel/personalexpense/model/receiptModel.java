package org.vrajpatel.personalexpense.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="receipts")
@Data
public class receiptModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="receipt_id")
    private UUID receiptId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User userId;

    @Column(name = "receipt_name", length = 255)
    private String receiptName;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private CategoriesModel category;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    // Your schema uses DATE (not timestamp)
    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(name = "receipt_file_url", columnDefinition = "text")
    private String receiptFileUrl;

    @Column(name = "receipt_description", columnDefinition = "text")
    private String receiptDescription;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name="receipt_type")
    private ReceiptType receiptType;

}
