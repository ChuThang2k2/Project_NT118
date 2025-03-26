package com.example.projectnt118;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.ResetPwRequest;
import com.example.projectnt118.modle.ResetPwResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ResetPwsActivity extends AppCompatActivity {
    private Button btn_reset_password;
    private TextInputLayout et_confirm_password_layout, et_password_layout;
    private TextInputEditText et_confirm_password;
    private TextInputEditText et_password;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_pws);

        btn_reset_password = findViewById(R.id.btn_reset_password);
        et_password_layout = findViewById(R.id.et_password_layout);
        et_confirm_password_layout = findViewById(R.id.et_confirm_password_layout);
        et_password = findViewById(R.id.et_password);
        et_confirm_password = findViewById(R.id.et_confirm_password);

        Retrofit retrofit = RetrofitClient.getClient("https://linhlinh.onrender.com/api/v1/auth/");
        apiService = retrofit.create(ApiService.class);

        email = getIntent().getStringExtra("email");

        btn_reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(ResetPwsActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                resetPassword();

            }
        });
    }
    private void resetPassword() {
        String password = et_password.getText().toString().trim();
        String confirmPassword = et_confirm_password.getText().toString().trim();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("ResetPassword", "Email: " + email);
        ResetPwRequest request = new ResetPwRequest(email, password);
        apiService.resetPassword(request).enqueue(new Callback<ResetPwResponse>() {
            @Override
            public void onResponse(Call<ResetPwResponse> call, Response<ResetPwResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ResetPwsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPwsActivity.this, NotiPasswordActivity.class);
                    startActivity(intent);
                } else {
                    try {
                        String errorMessage = response.errorBody().string();
                        Log.e("ResetPasswordError", "Response code: " + response.code() + ", message: " + errorMessage);
                        Toast.makeText(ResetPwsActivity.this, "Reset password failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ResetPwsActivity.this, "Reset password failed", Toast.LENGTH_SHORT).show();
                    }    }
            }

            @Override
            public void onFailure(Call<ResetPwResponse> call, Throwable t) {
                Toast.makeText(ResetPwsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}