package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;

import java.util.UUID;

@Repository
public interface PersonalExpenseRepository extends JpaRepository<PersonalExpenseModel, UUID> {
}
