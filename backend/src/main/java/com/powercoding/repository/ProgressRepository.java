package com.powercoding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.powercoding.model.Progress;
import com.powercoding.model.User;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByUserAndLanguage(User user, String language);
    
    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.language = :language")
    Optional<Progress> findByUserIdAndLanguage(@Param("userId") Long userId, @Param("language") String language);
}