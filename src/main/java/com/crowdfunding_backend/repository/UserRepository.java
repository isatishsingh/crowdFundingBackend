package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
}