package org.vrajpatel.personalexpense.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name="categories")
public class CategoriesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="category_id")
    private UUID categoryId;

    @Column(name="name")
    private String categoryName;

    @Column(name="description")
    private String categoryDescription;

    @Column(name="logo_url")
    private String logoUrl;

    @Column(name="created_at")
    private Date createdAt;
}
