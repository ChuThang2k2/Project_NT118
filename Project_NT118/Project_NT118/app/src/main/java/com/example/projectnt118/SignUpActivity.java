package com.example.projectnt118;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.SignUpRequest;
import com.example.projectnt118.modle.SignUpResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {
    ImageView back_signup;
    Button btn_signup;
    EditText et_username, et_email, et_password, et_confirm_password;
    private boolean passwordShowing = false;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        back_signup = findViewById(R.id.back_signup);
        btn_signup = findViewById(R.id.btn_signup);
        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_confirm_password = findViewById(R.id.et_confirm_password);
        ImageView ic_password, ic_confirm_password;
        ic_confirm_password = findViewById(R.id.icon_confirm_password);
        ic_password = findViewById(R.id.icon_password1);
        ic_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    ic_password.setImageResource(R.drawable.hidden);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ic_password.setImageResource(R.drawable.show);
                }
                et_password.setSelection(et_password.getText().length());
            }
        });

        ic_confirm_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_confirm_password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    et_confirm_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    ic_confirm_password.setImageResource(R.drawable.hidden);
                } else {
                    et_confirm_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ic_confirm_password.setImageResource(R.drawable.show);
                }
                et_confirm_password.setSelection(et_confirm_password.getText().length());
            }
        });

        back_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Retrofit retrofit = RetrofitClient.getClient("https://linhlinh.onrender.com/api/v1/auth/");
        apiService = retrofit.create(ApiService.class);

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                String username = et_username.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if (isValidEmail(email)) {
                    signUp(username, email, password);
                } else {
                    Toast.makeText(SignUpActivity.this, "Please provide a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signUp(String username, String email, String password) {
        SignUpRequest signUpRequest = new SignUpRequest(username, email, password);
        apiService.signUp(signUpRequest).enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignUpActivity.this, "Sign up success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, NotiSingupActivity.class);
                    startActivity(intent);
                } else {
                    String errorMessage = null;
                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Log.e("SignUpError", "Response code: " + response.code() + ", message: " + errorMessage);
                    Toast.makeText(SignUpActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}