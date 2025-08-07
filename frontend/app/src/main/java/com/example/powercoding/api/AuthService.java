package com.example.powercoding.api;

import com.example.powercoding.models.User;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/users/register")
    Call<User> register(@Body User user);

    @POST("/api/users/login")
    Call<User> login(@Body Map<String,String> credentials);
}
