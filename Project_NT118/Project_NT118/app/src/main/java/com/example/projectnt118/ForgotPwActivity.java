package com.example.projectnt118;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.ForgotPwRequest;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForgotPwActivity extends AppCompatActivity {
    private Button btn_sendCode;
    private ApiService apiService;
    private EditText et_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pw);
        btn_sendCode = findViewById(R.id.btn_sendCode);
        et_email = findViewById(R.id.et_email);
        Retrofit retrofit = RetrofitClient.getClient("https://linhlinh.onrender.com/api/v1/auth/");
        apiService = retrofit.create(ApiService.class);
        btn_sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(ForgotPwActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                sendForgotPasswordRequest();
            }
        });

    }

    private void sendForgotPasswordRequest() {
        String email = et_email.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        ForgotPwRequest request = new ForgotPwRequest(email);
        apiService.forgotPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Toast.makeText(ForgotPwActivity.this, responseBody, Toast.LENGTH_SHORT).show();
                        // Navigate to CodeVerificationActivity
                        Intent intent = new Intent(ForgotPwActivity.this, CodeVerificationActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ForgotPwActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPwActivity.this, "Failed to send verification code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ForgotPwActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}