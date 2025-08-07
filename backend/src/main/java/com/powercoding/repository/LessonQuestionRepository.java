package com.powercoding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.powercoding.model.LessonQuestion;

public interface LessonQuestionRepository extends JpaRepository<LessonQuestion, Long> {
}
