package com.example.powercoding.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.powercoding.R;
import com.example.powercoding.models.LessonQuestion;
import java.util.ArrayList;
import java.util.List;
import com.example.powercoding.api.ProgressService;
import com.example.powercoding.models.ProgressResponse;
import com.example.powercoding.utils.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity {
    private static final String PREF_NAME      = "powerCodingPrefs";
    private static final String KEY_LIVES      = "global_lives";
    private static final String KEY_LIVES_TS   = "global_lives_ts";
    private static final String KEY_STREAK     = "streak";
    private static final String KEY_LAST_DAY   = "last_correct_ts";
    private static final long STREAK_DAY_MS  = 24L * 60 * 60 * 1000;
    private static final long INTERVAL_MS    = 30L * 60 * 1000; // 30min

    // Top‑bar
    private ImageButton backButton, logoutButton;
    private ImageView languageLogo;
    private TextView globalXpText, streakText, livesText;

    // Lesson UI
    private ProgressBar progressBar;
    private TextView questionPrompt, answerStatus;
    private EditText codeInput;
    private ImageButton submitBtn;

    // State
    private List<LessonQuestion> questions = new ArrayList<>();
    private int currentQ = 0, lives = 5, lessonIndex;
    public boolean reviewMode = false; // for review mode, no lives lost and no XP gain
    private String lessonTitle, lessonLang, progressKey;

    @Override
    protected void onCreate(@Nullable Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_lesson);
        lessonIndex = getIntent().getIntExtra("lesson_index", 0);
        SharedPreferences p = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // bind top bar
        backButton   = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);
        languageLogo = findViewById(R.id.languageLogo);
        globalXpText = findViewById(R.id.globalXpText);
        streakText   = findViewById(R.id.streakText);
        livesText    = findViewById(R.id.livesText);

        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(v-> finish());
        logoutButton.setOnClickListener(v-> {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Pull lesson info
        Intent i = getIntent();
        lessonTitle = i.getStringExtra("lesson_title");
        lessonLang  = i.getStringExtra("lesson_language");
        setTitle(lessonTitle);

        // Set the language icon, java = default
        switch (lessonLang) {
            case "Python":
                languageLogo.setImageResource(R.drawable.python);
                break;
            case "JavaScript":
                languageLogo.setImageResource(R.drawable.js);
                break;
            default:
                languageLogo.setImageResource(R.drawable.java);
        }

        // Per‑lesson key
        progressKey = "lesson_progress_" + lessonLang.toLowerCase();

        // regen & show lives / xp / streak
        restoreLives();
        updateLivesDisplay();
        updateGlobalXp();
        updateStreakDisplay();

        // bind lesson UI
        progressBar = findViewById(R.id.progressBar);
        questionPrompt = findViewById(R.id.questionPrompt);
        answerStatus = findViewById(R.id.answerStatus);
        codeInput = findViewById(R.id.codeInput);
        submitBtn = findViewById(R.id.submitBtn);

        // load & restore questions
        questions = getQuestionsForLesson(lessonTitle, lessonLang);
        progressBar.setMax(questions.size());
        currentQ = getSharedPreferences(PREF_NAME,MODE_PRIVATE)
                .getInt("activity_progress_" + lessonLang.toLowerCase() + "_" + lessonIndex, 0);

        if (currentQ >= questions.size()) {
            reviewMode = true; // already completed
            currentQ = 0; // reset to first question
            Toast.makeText(this, "🔁 \n" +
                    "Review mode",
                    Toast.LENGTH_SHORT).show();
        }
        showQuestion();

        submitBtn.setOnClickListener(v-> checkAnswer());
    }

    private void showQuestion() {
        answerStatus.setText("");
        questionPrompt.setText(questions.get(currentQ).getPrompt());
        codeInput.setText("");
        progressBar.setProgress(currentQ);
    }

    private void checkAnswer() {
        String ans = codeInput.getText().toString().trim();
        String correct = questions.get(currentQ).getCorrectAnswer();
        SharedPreferences p = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (ans.equals(correct)) {
            // Handle XP and streak
            if (!reviewMode) {
                // Add XP (10 points per correct answer)
                int xp = p.getInt("xp_" + lessonLang.toLowerCase(), 0) + 10;
                p.edit().putInt("xp_" + lessonLang.toLowerCase(), xp).apply();
                updateGlobalXp();

                // Handle streak - increment if correct answer - 1 per day
                long now = System.currentTimeMillis();
                long last = p.getLong(KEY_LAST_DAY, 0);
                int streak = p.getInt(KEY_STREAK, 0);

                if (now - last >= STREAK_DAY_MS) {
                    streak = (now - last < STREAK_DAY_MS * 2) ? streak + 1 : 1;
                    p.edit()
                            .putInt(KEY_STREAK, streak)
                            .putLong(KEY_LAST_DAY, now)
                            .apply();
                    updateStreakDisplay();
                }
                saveProgressToBackend();
            }

            // Advance to next question
            currentQ++;
            if (!reviewMode) {
                // Save activity progress separately per lesson
                p.edit().putInt("activity_progress_" + lessonLang.toLowerCase() + "_" + lessonIndex, currentQ).apply();

                // Check if ALL activities in current lesson are completed
                if (currentQ >= questions.size()) {
                    int currentLessonProgress = p.getInt("lesson_progress_" + lessonLang.toLowerCase(), 0);

                    // Only update completed lesson progress if the lesson fully done
                    if (lessonIndex == currentLessonProgress && lessonIndex < 5) {
                        p.edit()
                                .putInt("lesson_progress_" + lessonLang.toLowerCase(), currentLessonProgress + 1)
                                .remove("activity_progress_" + lessonLang.toLowerCase() + "_" + lessonIndex) // Reset for next time
                                .putBoolean("lesson_completed_" + lessonLang.toLowerCase() + "_" + lessonIndex, true)
                                .apply();
                    }

                    setResult(RESULT_OK, new Intent().putExtra("force_refresh", true));
                    Toast.makeText(this, "🎉 Lesson complete!", Toast.LENGTH_LONG).show();
                    saveProgressToBackendAndFinish();
                    return;
                }
            }

            new Handler().postDelayed(() -> {
                if (currentQ < questions.size()) {
                    showQuestion();
                }
            }, 800);
        } else {
            // Handle incorrect answer
            lives--;
            p.edit().putInt(KEY_LIVES, Math.max(0, lives)).apply();
            updateLivesDisplay();
            saveProgressToBackend();

            if (lives <= 0) {
                submitBtn.setEnabled(false);
                codeInput.setEnabled(false);
                answerStatus.setText("⏳ Come back in 30min for more lives!");
                p.edit().putLong(KEY_LIVES_TS, System.currentTimeMillis()).apply();
            } else {
                answerStatus.setText("❌ Incorrect. Try again!");
            }
        }
    }

    private void updateGlobalXp() {
        SharedPreferences p = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        int total =
                p.getInt("xp_java",0) +
                        p.getInt("xp_python",0) +
                        p.getInt("xp_javascript",0);
        globalXpText.setText(total + "XP");
    }

    private void updateStreakDisplay() {
        int streak = getSharedPreferences(PREF_NAME,MODE_PRIVATE)
                .getInt(KEY_STREAK,0);
        streakText.setText(streak + "🔥");
    }

    private void updateLivesDisplay() {
        int lives = getSharedPreferences(PREF_NAME,MODE_PRIVATE)
                .getInt(KEY_LIVES,5);
        livesText.setText(lives + "❤");
    }

    private void restoreLives() {
        SharedPreferences p = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int stored = p.getInt(KEY_LIVES, 5);
        long lastTs = p.getLong(KEY_LIVES_TS, System.currentTimeMillis());
        long now = System.currentTimeMillis();
        long elapsed = now - lastTs;

        // Calculate gained lives (1 per 30 minutes)
        int gained = (int) (elapsed / INTERVAL_MS);
        int newLives = Math.min(5, stored + gained);

        // Calculate new timestamp
        long newTs = lastTs + (gained * INTERVAL_MS);

        p.edit()
                .putInt(KEY_LIVES, newLives)
                .putLong(KEY_LIVES_TS, newTs)
                .apply();
        lives = newLives;

        updateLivesDisplay();
    }

    // List of five questions per lesson prototype and default
    // Shared questions for all languages
    private List<LessonQuestion> getQuestionsForLesson(String title, String lang) {
        List<LessonQuestion> list = new ArrayList<>();
        switch (title) {
            case "Basics":
                list.add(new LessonQuestion(
                        "1) What keyword declares an int x?\n" +
                                "A) int x;\nB) var x;\nC) const x;",
                        "int x;"
                ));
                list.add(new LessonQuestion(
                        "2) Print Hello in "+lang+"?\n" +
                                "A) System.out.println(\"Hello\");\n" +
                                "B) print(\"Hello\");\n" +
                                "C) echo \"Hello\";",
                        lang.equals("Java")
                                ? "System.out.println(\"Hello\");"
                                : "print(\"Hello\");"
                ));
                list.add(new LessonQuestion(
                        "3) Single‐line comment?\n" +
                                "A) // comment\n" +
                                "B) # comment\n" +
                                "C) /* comment */",
                        "// comment"
                ));
                list.add(new LessonQuestion(
                        "4) What ends a statement in Java?\n" +
                                "A) ;\nB) .\nC) ,",
                        ";"
                ));
                list.add(new LessonQuestion(
                        "5) Declare foo method:\n" +
                                "A) void foo() {}\n" +
                                "B) def foo():\n" +
                                "C) func foo() {}",
                        "void foo() {}"
                ));
                break;

            case "Variables":
                list.add(new LessonQuestion(
                        "1) Declare x=5:\n" +
                                "A) int x = 5;\nB) var x = 5;\nC) let x = 5;",
                        "int x = 5;"
                ));
                list.add(new LessonQuestion(
                        "2) String s = \"Bob\":\n" +
                                "A) String s = \"Bob\";\nB) str s = \"Bob\";\nC) var s = \"Bob\";",
                        "String s = \"Bob\";"
                ));
                list.add(new LessonQuestion(
                        "3) Reassign x to 10:\n" +
                                "A) x == 10;\nB) x = 10;\nC) let x = 10;",
                        "x = 10;"
                ));
                list.add(new LessonQuestion(
                        "4) Declare float f:\n" +
                                "A) float f = 3.14f;\nB) float f = 3.14;\nC) var f:float = 3.14;",
                        "float f = 3.14f;"
                ));
                list.add(new LessonQuestion(
                        "5) Keyword for constant?\n" +
                                "A) final\nB) const\nC) static",
                        "final"
                ));
                break;

        }
        return list;
    }

    private void saveProgressToBackend() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        if (userId < 0) return;

        ProgressService progressService = ApiClient.getClient().create(ProgressService.class);
        ProgressResponse pr = new ProgressResponse();
        pr.setUserId(userId);
        pr.setLanguage(lessonLang.toLowerCase());
        pr.setXp(prefs.getInt("xp_"  + lessonLang.toLowerCase(), 0));
        pr.setStreak(prefs.getInt(KEY_STREAK, 0));
        pr.setLives(prefs.getInt(KEY_LIVES,    5));
        pr.setLivesTimestamp(prefs.getLong(KEY_LIVES_TS, System.currentTimeMillis()));
        pr.setLastActivityDate(prefs.getLong("lastActivityDate_" + lessonLang.toLowerCase(), 0L));

        progressService.saveProgress(pr).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
            }
        });
    }

    private void saveProgressToBackendAndFinish() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        if (userId < 0) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        ProgressService progressService = ApiClient.getClient().create(ProgressService.class);
        ProgressResponse pr = new ProgressResponse();
        pr.setUserId(userId);
        pr.setLanguage(lessonLang.toLowerCase());
        pr.setXp(prefs.getInt("xp_"  + lessonLang.toLowerCase(), 0));
        pr.setStreak(prefs.getInt(KEY_STREAK, 0));
        pr.setLives(prefs.getInt(KEY_LIVES,    5));
        pr.setLivesTimestamp(prefs.getLong(KEY_LIVES_TS, System.currentTimeMillis()));
        pr.setLastActivityDate(prefs.getLong("lastActivityDate_" + lessonLang.toLowerCase(), 0L));

        progressService.saveProgress(pr).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("xp_" + lessonLang.toLowerCase(), pr.getXp());
                editor.putInt(KEY_STREAK, pr.getStreak());
                editor.putInt(KEY_LIVES, pr.getLives());
                editor.apply();

                setResult(RESULT_OK, new Intent().putExtra("force_refresh", true));
                Toast.makeText(LessonActivity.this, "🎉 Lesson complete!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("xp_" + lessonLang.toLowerCase(), pr.getXp());
                editor.putInt(KEY_STREAK, pr.getStreak());
                editor.putInt(KEY_LIVES, pr.getLives());
                editor.apply();

                setResult(RESULT_OK, new Intent().putExtra("force_refresh", true));
                Toast.makeText(LessonActivity.this, "Lesson complete, but failed to sync progress.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}

