package org.vrajpatel.userauthservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vrajpatel.userauthservice.model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUserId(UUID Id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);


}
