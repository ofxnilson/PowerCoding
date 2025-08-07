package com.powercoding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.powercoding.model.Lesson;


public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByLanguageIgnoreCase(String language);
}

