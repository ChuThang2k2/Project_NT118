package com.example.projectnt118;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.VerifyCodeRequest;
import com.example.projectnt118.modle.VerifyCodeResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CodeVerificationActivity extends AppCompatActivity {
    private Button btn_verify;
    private TextView tv_resend;
    private EditText et_code;
    private int resendTime = 60;
    private boolean resendEnable = true;
    private ApiService apiService;
    private String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_code_verification);
        btn_verify = findViewById(R.id.btn_verify);
        tv_resend = findViewById(R.id.tv_resend_code);
        et_code = findViewById(R.id.et_code);
        Retrofit retrofit = RetrofitClient.getClient("https://linhlinh.onrender.com/api/v1/auth/");
        apiService = retrofit.create(ApiService.class);
        email = getIntent().getStringExtra("email");
        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(CodeVerificationActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                verifyCode();

            }
        });
        startCountDownTimer();
        tv_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resendEnable) {
                    startCountDownTimer();
                }
            }
        });
    }

    private void startCountDownTimer() {
        resendEnable = false;
        tv_resend.setTextColor(Color.GRAY);
        new CountDownTimer(resendTime * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tv_resend.setText("Resend code in (" + millisUntilFinished / 1000 + ") seconds");
            }

            @Override
            public void onFinish() {
                resendEnable = true;
                tv_resend.setText("Resend code");
                tv_resend.setTextColor(Color.BLACK);

            }
        }.start();
    }

    private void verifyCode() {
        String token = et_code.getText().toString().trim();
        if (token.isEmpty()) {
            Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
            return;
        }
        VerifyCodeRequest request = new VerifyCodeRequest(email, token);
        apiService.verifyCode(request).enqueue(new Callback<VerifyCodeResponse>() {
            @Override
            public void onResponse(Call<VerifyCodeResponse> call, Response<VerifyCodeResponse> response) {
                if (response.isSuccessful()) {
                    String message = response.body().getMessage();
                    Toast.makeText(CodeVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                    if ("token chinh xac".equals(message)) {
                        Intent intent = new Intent(CodeVerificationActivity.this, ResetPwsActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);

                    }
                } else {
                    String errorMessage = null;
                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Log.e("VerifyCodeError", "Response code: " + response.code() + ", message: " + errorMessage);
                    Toast.makeText(CodeVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<VerifyCodeResponse> call, Throwable t) {
                Toast.makeText(CodeVerificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log the error for debugging
            }
        });
    }


}