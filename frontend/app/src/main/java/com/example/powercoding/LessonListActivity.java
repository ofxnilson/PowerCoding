package com.example.powercoding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powercoding.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonListActivity extends AppCompatActivity {

    private ImageView languageLogo;
    private ImageButton logoutButton;
    private TextView xpText, streakText;
    private RecyclerView lessonPathRecycler;
    private LessonAdapter lessonAdapter;
    private List<Lesson> lessonList = new ArrayList<>();

    private static final String PREF_NAME = "powerCodingPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        // Bind views
        languageLogo = findViewById(R.id.languageLogo);
        logoutButton = findViewById(R.id.logoutButton);
        xpText = findViewById(R.id.xpText);
        streakText = findViewById(R.id.streakText);
        lessonPathRecycler = findViewById(R.id.lessonPathRecycler);

        // Get saved user data
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int xp = prefs.getInt("xp", 0);
        int streak = prefs.getInt("streak", 0);

        // Set stats
        xpText.setText(xp + " XP");
        streakText.setText(streak + "🔥");

        // Setup RecyclerView
        lessonAdapter = new LessonAdapter(this, lessonList, (position, lesson) -> {
            // Handle lesson bubble click if needed
        });
        lessonPathRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        lessonPathRecycler.setAdapter(lessonAdapter);

        // Load default language lessons
        loadLessonsForLanguage("Java");
        languageLogo.setImageResource(R.drawable.java);

        // Logout
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Language selector
        languageLogo.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(LessonListActivity.this, languageLogo);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.language_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.lang_java) {
                    languageLogo.setImageResource(R.drawable.java);
                    loadLessonsForLanguage("Java");
                    return true;
                } else if (id == R.id.lang_python) {
                    languageLogo.setImageResource(R.drawable.python);
                    loadLessonsForLanguage("Python");
                    return true;
                } else if (id == R.id.lang_js) {
                    languageLogo.setImageResource(R.drawable.js);
                    loadLessonsForLanguage("JavaScript");
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void loadLessonsForLanguage(String language) {
        lessonList.clear();
        lessonList.add(new Lesson(
                1L, language, "Basics", "Content for Basics", R.drawable.ic_lesson, Lesson.LessonState.ACTIVE));
        lessonList.add(new Lesson(
                2L, language, "Variables", "Content for Variables", R.drawable.ic_lesson, Lesson.LessonState.LOCKED));
        lessonList.add(new Lesson(
                3L, language, "Conditionals", "Content for Conditionals", R.drawable.ic_lesson, Lesson.LessonState.LOCKED));
        lessonList.add(new Lesson(
                4L, language, "Loops", "Content for Loops", R.drawable.ic_lesson, Lesson.LessonState.LOCKED));
        lessonList.add(new Lesson(
                5L, language, "Functions", "Content for Functions", R.drawable.ic_lesson, Lesson.LessonState.LOCKED));

        lessonAdapter.notifyDataSetChanged();
    }
}
