package com.powercoding.controller;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private UserRepository userRepository;

    // GET Progress
    @GetMapping("/{userId}/{language}")
    public ProgressResponse getProgress(@PathVariable Long userId, @PathVariable String language) {
    // Check if user exists
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    // Find or create progress
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

    // POST: save progress
        @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody ProgressResponse progressResponse) {
        System.out.println("PROGRESS SAVE: " + progressResponse);
        User user = userRepository.findById(progressResponse.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Try to find existing progress for this user+language
        Optional<Progress> optional = progressRepo.findByUserAndLanguage(user, progressResponse.getLanguage());

        Progress progress;
        if (optional.isPresent()) {
            progress = optional.get();
        } else {
            progress = new Progress();
            progress.setUser(user);
            progress.setLanguage(progressResponse.getLanguage());
        }

        // Update fields
        progress.setXp(progressResponse.getXp());
        progress.setStreak(progressResponse.getStreak());
        progress.setLives(progressResponse.getLives());
        progress.setLessonProgress(progressResponse.getLessonProgress());
        progress.setLivesTimestamp(LocalDateTime.ofInstant(
            Instant.ofEpochMilli(progressResponse.getLivesTimestamp()), ZoneId.systemDefault()
            )
        );
        progress.setLastActivityDate(LocalDateTime.ofInstant(
            Instant.ofEpochMilli(progressResponse.getLastActivityDate()),
            ZoneId.systemDefault()
            )
        );

        progressRepo.save(progress);
        return ResponseEntity.ok().build();
    }


    // POST: Update Activity/Handle Streaks 
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

    // POST: Use a life 
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

    // Helper: Upsert progress by userId/language
    private Progress getOrCreateProgress(Long userId, String language) {
    User user = userRepository.findById(userId)
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


    //Helper: Replenish lives if enough time has passed
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

    // Helper: Convert Progress to ProgressResponse for the app 
    private ProgressResponse convertToResponse(Progress progress) {
        ProgressResponse response = new ProgressResponse();
        response.setUserId(progress.getUser().getId());
        response.setLanguage(progress.getLanguage());
        response.setXp(progress.getXp());
        response.setStreak(progress.getStreak());
        response.setLives(progress.getLives());
        response.setLessonProgress(progress.getLessonProgress());

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
