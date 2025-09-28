package com.adrin.fc.repository;

import com.adrin.fc.entity.User;
import com.adrin.fc.enums.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    Page<User> findByRole(Role role, Pageable pageable);
}
