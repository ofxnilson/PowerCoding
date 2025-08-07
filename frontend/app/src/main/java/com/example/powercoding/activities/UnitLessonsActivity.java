package com.example.powercoding.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powercoding.R;
import com.example.powercoding.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class UnitLessonsActivity extends AppCompatActivity {

    private TextView unitTitle;
    private RecyclerView lessonsRecycler;
    private LessonAdapter lessonAdapter;
    private List<Lesson> lessonList = new ArrayList<>();

    private String language;  // gets from intent
    private static final String PREF_NAME = "powerCodingPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unit_lessons_activity);

        unitTitle = findViewById(R.id.unitTitle);
        lessonsRecycler = findViewById(R.id.lessonsRecycler);

        language = getIntent().getStringExtra("lesson_language");
        unitTitle.setText("Basics (" + language + ")");

        // Progress for this language's unit:
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int progress = prefs.getInt(getUnitProgressKey(language), 0);

        lessonList.clear();
        for (int i = 0; i < 5; i++) {
            lessonList.add(new Lesson(
                    (long) (i + 1), language, "Lesson " + (i + 1), "Content for lesson " + (i + 1), R.drawable.ic_lesson,
                    i < progress ? Lesson.LessonState.COMPLETED :
                            (i == progress ? Lesson.LessonState.ACTIVE : Lesson.LessonState.LOCKED), 0
            ));
        }

        lessonAdapter = new LessonAdapter(this, lessonList, (position, lesson) -> {
            if (lesson.getState() == Lesson.LessonState.ACTIVE || lesson.getState() == Lesson.LessonState.COMPLETED) {
                Intent intent = new Intent(this, LessonActivity.class);
                intent.putExtra("lesson_id", lesson.getLessonId());
                intent.putExtra("lesson_title", lesson.getTitle());
                intent.putExtra("lesson_language", lesson.getLanguage());
                intent.putExtra("lesson_index", position);
                startActivityForResult(intent, 101);
            }
        });
        lessonsRecycler.setLayoutManager(new LinearLayoutManager(this));
        lessonsRecycler.setAdapter(lessonAdapter);
    }

    private String getUnitProgressKey(String lang) {
        return "unit_progress_basics_" + lang.toLowerCase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) recreate();
    }
}

