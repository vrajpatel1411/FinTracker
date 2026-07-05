package org.vrajpatel.personalexpense.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vrajpatel.personalexpense.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
