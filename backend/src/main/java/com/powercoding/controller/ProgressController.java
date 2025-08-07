package com.powercoding.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.powercoding.model.Progress;
import com.powercoding.model.ProgressResponse;
import com.powercoding.model.User;
import com.powercoding.repository.ProgressRepository;
import com.powercoding.repository.UserRepository;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {
    private static final int MAX_LIVES = 5;
    private static final long LIFE_REPLENISH_MINUTES = 30;

    @Autowired
    private ProgressRepository progressRepo;

    @Autowired
    private UserRepository userRepo;

    // --- 1. GET Progress ---
    @GetMapping("/{userId}/{language}")
    public ProgressResponse getProgress(@PathVariable Long userId, @PathVariable String language) {
    // 1. Check if user exists
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    // 2. Find or create progress
    Progress progress = progressRepo.findByUserIdAndLanguage(userId, language)
            .orElseGet(() -> {
                Progress p = new Progress(user, language);
                p.setXp(0);
                p.setStreak(0);
                p.setLives(5);
                p.setLivesTimestamp(java.time.LocalDateTime.now());
                return progressRepo.save(p);
            });

    checkAndReplenishLives(progress);
    return convertToResponse(progress);
}

    // --- 2. POST: Universal Save/Upsert Progress (NEW!) ---
    @PostMapping
    public ProgressResponse saveProgress(@RequestBody ProgressResponse req) {
        // Find or create user and progress
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Progress progress = progressRepo.findByUserAndLanguage(user, req.getLanguage())
                .orElse(new Progress(user, req.getLanguage()));

        // Set all fields from request
        progress.setXp(req.getXp());
        progress.setStreak(req.getStreak());
        progress.setLives(req.getLives());

        if (req.getLivesTimestamp() != 0L) {
            progress.setLivesTimestamp(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(req.getLivesTimestamp()), ZoneId.systemDefault()));
        }
        if (req.getLastActivityDate() != 0L) {
            progress.setLastActivityDate(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(req.getLastActivityDate()), ZoneId.systemDefault()));
        }

        // Save and return response
        progressRepo.save(progress);
        return convertToResponse(progress);
    }

    // --- 3. POST: Update Activity/Handle Streaks ---
    @PostMapping("/update-activity")
    public ProgressResponse updateActivity(
            @RequestParam Long userId,
            @RequestParam String language,
            @RequestParam boolean activityCompleted) {

        Progress progress = getOrCreateProgress(userId, language);

        // Check and replenish lives first
        checkAndReplenishLives(progress);

        LocalDateTime now = LocalDateTime.now();
        if (activityCompleted) {
            // If new day, increment streak
            if (progress.getLastActivityDate() == null ||
                progress.getLastActivityDate().toLocalDate().isBefore(now.toLocalDate())) {
                progress.setStreak(progress.getStreak() + 1);
            }
            progress.setLastActivityDate(now);
        } else {
            // If user missed a day, reset streak
            if (progress.getLastActivityDate() != null &&
                progress.getLastActivityDate().toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
                progress.setStreak(0);
            }
        }
        progressRepo.save(progress);
        return convertToResponse(progress);
    }

    // --- 4. POST: Use a life ---
    @PostMapping("/use-life")
    public ProgressResponse useLife(
            @RequestParam Long userId,
            @RequestParam String language) {

        Progress progress = getOrCreateProgress(userId, language);
        checkAndReplenishLives(progress);

        if (progress.getLives() > 0) {
            progress.setLives(progress.getLives() - 1);
            progress.setLivesTimestamp(LocalDateTime.now());
            progressRepo.save(progress);
        }

        return convertToResponse(progress);
    }

    // --- Helper: Upsert progress by userId/language ---
    private Progress getOrCreateProgress(Long userId, String language) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return progressRepo.findByUserAndLanguage(user, language)
            .orElseGet(() -> {
                Progress p = new Progress(user, language);
                p.setXp(0);
                p.setStreak(0);
                p.setLives(5);
                p.setLivesTimestamp(LocalDateTime.now());
                return progressRepo.save(p);
            });
}


    // --- Helper: Replenish lives if enough time has passed ---
    private void checkAndReplenishLives(Progress progress) {
        if (progress.getLives() >= MAX_LIVES) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = progress.getLivesTimestamp() != null ? progress.getLivesTimestamp() : now;

        long minutesSinceLastUpdate = Duration.between(lastUpdate, now).toMinutes();
        int livesToAdd = (int) (minutesSinceLastUpdate / LIFE_REPLENISH_MINUTES);

        if (livesToAdd > 0) {
            int newLives = Math.min(MAX_LIVES, progress.getLives() + livesToAdd);
            progress.setLives(newLives);

            // Set timestamp to now minus the leftover minutes
            long remainingMinutes = minutesSinceLastUpdate % LIFE_REPLENISH_MINUTES;
            progress.setLivesTimestamp(now.minusMinutes(remainingMinutes));

            progressRepo.save(progress);
        }
    }

    // --- Helper: Convert Progress to ProgressResponse for the app ---
    private ProgressResponse convertToResponse(Progress progress) {
        ProgressResponse response = new ProgressResponse();
        response.setUserId(progress.getUser().getId());
        response.setLanguage(progress.getLanguage());
        response.setXp(progress.getXp());
        response.setStreak(progress.getStreak());
        response.setLives(progress.getLives());

        if (progress.getLivesTimestamp() != null) {
            response.setLivesTimestamp(progress.getLivesTimestamp()
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (progress.getLastActivityDate() != null) {
            response.setLastActivityDate(progress.getLastActivityDate()
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return response;
    }
}
