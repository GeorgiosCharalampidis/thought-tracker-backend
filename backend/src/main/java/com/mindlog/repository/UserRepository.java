package com.mindlog.repository;

import com.mindlog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // No need for custom methods - using inherited findById(Long id) which returns Optional<User>
}