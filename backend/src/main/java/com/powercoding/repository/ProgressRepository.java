package com.powercoding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.powercoding.model.Progress;
import com.powercoding.model.User;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUser(User user);
}
