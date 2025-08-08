package com.example.powercoding.api;

import com.example.powercoding.models.ProgressResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ProgressService {
    @GET("progress/{userId}/{language}")
    Call<ProgressResponse> getProgress(
            @Path("userId")    long userId,
            @Path("language")  String language
    );

    @POST("progress/save")
    Call<Void> saveProgress(@Body ProgressResponse progress);
}


