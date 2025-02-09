package org.vrajpatel.userauthservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vrajpatel.userauthservice.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUserId(Long Id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
