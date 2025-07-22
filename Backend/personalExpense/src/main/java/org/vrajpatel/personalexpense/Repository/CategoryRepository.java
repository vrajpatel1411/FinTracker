package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vrajpatel.personalexpense.model.CategoriesModel;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoriesModel, UUID> {
}


