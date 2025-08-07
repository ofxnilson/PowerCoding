package com.powercoding.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.powercoding.model.Lesson;
import com.powercoding.repository.LessonRepository;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins="*")
public class LessonController {
    private final LessonRepository repo;
    public LessonController(LessonRepository repo) { this.repo = repo; }

    @GetMapping("/{language}")
    public List<Lesson> byLang(@PathVariable String language) {
        return repo.findByLanguageIgnoreCase(language);
    }
}
