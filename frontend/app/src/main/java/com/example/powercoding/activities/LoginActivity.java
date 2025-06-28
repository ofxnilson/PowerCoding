package com.example.powercoding.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.powercoding.LessonListActivity;
import com.example.powercoding.R;
import com.example.powercoding.api.AuthService;
import com.example.powercoding.models.User;
import com.example.powercoding.utils.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn, goToRegisterBtn;
    CheckBox rememberMeCheckBox;

    private static final String PREF_NAME = "powerCodingPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_PowerCoding_Splash); // switch from splash to main theme
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // Auto login if "Remember Me" is checked
        if (isLoggedIn) {
            startActivity(new Intent(this, LessonListActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Bind views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        goToRegisterBtn = findViewById(R.id.registerRedirect);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Go to RegisterActivity
        goToRegisterBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Handle login
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            AuthService api = ApiClient.getClient().create(AuthService.class);
            api.login(credentials).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();

                        // Save login state if "Remember Me" is checked
                        if (rememberMeCheckBox.isChecked()) {
                            SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putLong("userId", user.getUserId());
                            editor.putString("username", user.getUsername());
                            editor.apply();
                        }

                        Toast.makeText(LoginActivity.this, "Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, LessonListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}


