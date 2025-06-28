package com.powercoding.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.powercoding.model.Lesson;
import com.powercoding.repository.LessonRepository;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    @Autowired
    private LessonRepository lessonRepo;

    @GetMapping("/{language}")
    public List<Lesson> getByLanguage(@PathVariable String language) {
        return lessonRepo.findByLanguage(language);
    }
}
