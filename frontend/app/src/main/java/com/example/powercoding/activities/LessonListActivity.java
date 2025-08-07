package com.example.powercoding.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powercoding.R;
import com.example.powercoding.api.LessonService;
import com.example.powercoding.api.ProgressService;
import com.example.powercoding.models.Lesson;
import com.example.powercoding.models.ProgressResponse;
import com.example.powercoding.utils.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonListActivity extends AppCompatActivity {
    private static final String PREF_NAME    = "powerCodingPrefs";
    private static final String KEY_LIVES    = "global_lives";
    private static final String KEY_LIVES_TS = "global_lives_ts";
    private static final String KEY_STREAK   = "streak";
    private static final int REQUEST_CODE_LESSON = 1001;
    private String currentLanguage = "Java";

    // Top-bar
    private ImageButton backButton, logoutButton;
    private ImageView   languageLogo;
    private TextView    globalXpText, streakText, livesText;

    // Progress API
    private ProgressService progressService;
    private LessonService   lessonService;

    // Lesson list
    private RecyclerView  lessonPathRecycler;
    private LessonAdapter lessonAdapter;
    private List<Lesson>  lessonList      = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        // set up Retrofit service
        progressService = ApiClient.getClient()
                .create(ProgressService.class);
        lessonService   = ApiClient.getClient()
                .create(LessonService.class);

        // bind top-bar views
        backButton   = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);
        languageLogo = findViewById(R.id.languageLogo);
        globalXpText = findViewById(R.id.globalXpText);
        streakText   = findViewById(R.id.streakText);
        livesText    = findViewById(R.id.livesText);
        backButton.setVisibility(View.GONE);

        // immediately load stats from server
        long userId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getLong("userId", -1);
        if (userId >= 0) {
            loadRemoteProgress(userId);
        }

        // set up lessons RecyclerView
        lessonPathRecycler = findViewById(R.id.lessonPathRecycler);
        lessonAdapter = new LessonAdapter(
                this,
                lessonList,
                (pos, lesson) -> {
                    if (lesson.getState() != Lesson.LessonState.LOCKED) {
                        Intent i = new Intent(this, LessonActivity.class);
                        i.putExtra("lesson_language", lesson.getLanguage());
                        i.putExtra("lesson_title",    lesson.getTitle());
                        i.putExtra("lesson_index",    pos);
                        startActivity(i);
                    }
                }
        );
        lessonPathRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );
        lessonPathRecycler.setAdapter(lessonAdapter);

        // logout
        logoutButton.setOnClickListener(v -> {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // language selector popup
        languageLogo.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, languageLogo);
            MenuInflater inf = popup.getMenuInflater();
            inf.inflate(R.menu.language_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.lang_java) {
                    setLanguage("Java", R.drawable.java);
                    return true;
                }
                if (id == R.id.lang_python) {
                    setLanguage("Python", R.drawable.python);
                    return true;
                }
                if (id == R.id.lang_js) {
                    setLanguage("JavaScript", R.drawable.js);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // initial lesson load
        setLanguage("Java", R.drawable.java);
    }

    // Pull XP / lives / streak from the server
    private void loadRemoteProgress(long userId) {
        progressService.getProgress(userId, currentLanguage)
                .enqueue(new Callback<ProgressResponse>() {
                    @Override
                    public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> resp) {
                        ProgressResponse pr = resp.body();
                        if (pr != null) {
                            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .putInt("xp_" + currentLanguage.toLowerCase(), pr.getXp())
                                    .putInt(KEY_STREAK, pr.getStreak())
                                    .putInt(KEY_LIVES, pr.getLives())
                                    .apply();

                            // Update the top bar
                            updateTopBarStats();
                        }
                    }
                    @Override
                    public void onFailure(Call<ProgressResponse> call, Throwable t) {}
                });
    }

    /** rebuilds the lesson bubbles & reapplies progress */
    private void setLanguage(String language, int iconRes) {
        currentLanguage = language;
        languageLogo.setImageResource(iconRes);
        fetchLessonsForLanguage(language);

        int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt("lesson_progress_" + language.toLowerCase(), 0);
        updateLessonStates(prog);
    }

    private void fetchLessonsForLanguage(String language) {
        lessonService.getLessonsByLanguage(language)
                .enqueue(new Callback<List<Lesson>>() {
                    @Override
                    public void onResponse(Call<List<Lesson>> call,
                                           Response<List<Lesson>> resp) {
                        if (resp.isSuccessful()
                                && resp.body()      != null
                                && !resp.body().isEmpty()) {

                            lessonList.clear();
                            lessonList.addAll(resp.body());

                            for (Lesson lesson : lessonList) {
                                switch (lesson.getTitle()) {
                                    case "Basics":
                                        lesson.setIconResId(R.drawable.ic_lesson); // Use your code icon resource
                                        break;
                                    case "Variables":
                                        lesson.setIconResId(R.drawable.ic_lesson);
                                        break;
                                    case "Conditionals":
                                        lesson.setIconResId(R.drawable.ic_lesson);
                                        break;
                                    case "Loops":
                                        lesson.setIconResId(R.drawable.ic_lesson);
                                        break;
                                    case "Functions":
                                        lesson.setIconResId(R.drawable.ic_lesson);
                                        break;
                                    case "Advanced":
                                        lesson.setIconResId(R.drawable.ic_unlocked_triangle); // The triangle icon for the last bubble
                                        break;
                                    default:
                                        lesson.setIconResId(R.drawable.ic_lesson); // Default/fallback icon
                                }
                            }
                            int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                                    .getInt("lesson_progress_" + language.toLowerCase(), 0);
                            updateLessonStates(prog);
                            lessonAdapter.notifyDataSetChanged();

                        } else {
                            // Server error handler: server OK but empty or error code
                            Toast.makeText(
                                    LessonListActivity.this,
                                    "Could not load lessons from server;\nshowing defaults.",
                                    Toast.LENGTH_LONG
                            ).show();
                            loadLocalLessons(language);
                        }

                        int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                                .getInt("lesson_progress_" + language.toLowerCase(), 0);
                        updateLessonStates(prog);
                    }

                    @Override
                    public void onFailure(Call<List<Lesson>> call, Throwable t) {
                        // Network failure handler: no connection or server down
                        Toast.makeText(
                                LessonListActivity.this,
                                "Network error;\nshowing default lessons.",
                                Toast.LENGTH_LONG
                        ).show();
                        loadLocalLessons(language);

                        int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                                .getInt("lesson_progress_" + language.toLowerCase(), 0);
                        updateLessonStates(prog);
                    }
                });
    }

    // Always reload the latest stats
    @Override
    protected void onResume() {
        super.onResume();
        // Update local stats & progress
        updateStatsFromPrefs();
        int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt("lesson_progress_" + currentLanguage.toLowerCase(), 0);
        updateLessonStates(prog);

        // Assync refresh data from server
        long userId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getLong("userId", -1);
        if (userId >= 0) {
            loadRemoteProgress(userId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LESSON) {
            if (resultCode == RESULT_OK) {
                // Update the latest lesson progress from SharedPreferences
                int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .getInt("lesson_progress_" + currentLanguage.toLowerCase(), 0);
                updateLessonStates(prog);
                lessonAdapter.notifyDataSetChanged();

                // Also reload XP/streak/lives stats
                long userId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .getLong("userId", -1);
                if (userId >= 0) {
                    loadRemoteProgress(userId); // to sync the stats in the top bar with server
                }
            }
        }
    }

    // Default local lessons for when the server is down or the user is offline
    private void loadLocalLessons(String language) {
        lessonList.clear();
        lessonList.add(new Lesson(0, language, "Basics",      "", R.drawable.ic_lesson, Lesson.LessonState.LOCKED, 0));
        lessonList.add(new Lesson(1, language, "Variables",   "", R.drawable.ic_lesson, Lesson.LessonState.LOCKED, 0));
        lessonList.add(new Lesson(2, language, "Conditionals","", R.drawable.ic_lesson, Lesson.LessonState.LOCKED, 0));
        lessonList.add(new Lesson(3, language, "Loops",       "", R.drawable.ic_lesson, Lesson.LessonState.LOCKED, 0));
        lessonList.add(new Lesson(4, language, "Functions",   "", R.drawable.ic_lesson, Lesson.LessonState.LOCKED, 0));
        lessonList.add(new Lesson(5, language, "Advanced",    "", R.drawable.ic_unlocked_triangle, Lesson.LessonState.LOCKED, 0));

        int prog = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt("lesson_progress_" + language.toLowerCase(), 0);
        updateLessonStates(prog);
    }

    private void updateGlobalXp() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int total =
                prefs.getInt("xp_java", 0) +
                        prefs.getInt("xp_python", 0) +
                        prefs.getInt("xp_javascript", 0);
        globalXpText.setText(total + " XP");
    }

    private void updateStreakDisplay() {
        int streak = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt(KEY_STREAK, 0);
        streakText.setText(streak + "🔥");
    }

    private void updateLivesDisplay() {
        int lives = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getInt(KEY_LIVES, 5);
        livesText.setText(lives + "❤️");
    }

    private void updateTopBarStats() {
        updateGlobalXp();
        updateStreakDisplay();
        updateLivesDisplay();
    }

    private void updateLessonStates(int progress) {
        for (int i = 0; i < lessonList.size(); i++) {
            Lesson lesson = lessonList.get(i);
            Lesson.LessonState state;
            if (i == 5) {
                state = (progress >= 5) ? Lesson.LessonState.ACTIVE
                        : Lesson.LessonState.LOCKED;
            } else if (i < progress) {
                state = Lesson.LessonState.COMPLETED;
            } else if (i == progress) {
                state = Lesson.LessonState.ACTIVE;
            } else {
                state = Lesson.LessonState.LOCKED;
            }
            lesson.setState(state);
        }
        lessonAdapter.notifyDataSetChanged();
    }
    private void updateStatsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int xp = prefs.getInt("xp_" + currentLanguage.toLowerCase(), 0);
        int streak = prefs.getInt(KEY_STREAK, 0);
        int lives = prefs.getInt(KEY_LIVES, 5);

        globalXpText.setText(xp + " XP");
        streakText.setText(streak + "🔥");
        livesText.setText(lives + "❤️");
    }

}
