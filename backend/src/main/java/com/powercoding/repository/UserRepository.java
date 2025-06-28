package com.powercoding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.powercoding.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
