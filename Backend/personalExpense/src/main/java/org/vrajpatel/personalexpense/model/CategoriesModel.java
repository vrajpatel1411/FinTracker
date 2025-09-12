package org.vrajpatel.personalexpense.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "categories")
public class CategoriesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id", updatable = false, nullable = false)
    private UUID categoryId;

    // Ownership
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Display info
    @Column(name = "name", nullable = false)
    private String categoryName;

    @Column(name = "color")
    private String categoryColor;

    // Lifecycle
    @Column(name = "is_system_seed", nullable = false)
    private boolean systemSeed;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
