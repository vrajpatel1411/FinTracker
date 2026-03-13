package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.vrajpatel.personalexpense.model.ReceiptModel;

import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<ReceiptModel, UUID> {
}
