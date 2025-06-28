package com.powercoding.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.powercoding.model.Progress;
import com.powercoding.model.User;
import com.powercoding.repository.ProgressRepository;
import com.powercoding.repository.UserRepository;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/{userId}")
    public List<Progress> getProgress(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElse(null);
        return progressRepo.findByUser(user);
    }

    @PostMapping("/update")
    public Progress update(@RequestBody Progress progress) {
        return progressRepo.save(progress);
    }
}
