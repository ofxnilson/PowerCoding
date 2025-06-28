package com.example.powercoding.api;

import com.example.powercoding.models.Lesson;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LessonApi {
    @GET("lessons/{language}")
    Call<List<Lesson>> getLessonsByLanguage(@Path("language") String language);
}
