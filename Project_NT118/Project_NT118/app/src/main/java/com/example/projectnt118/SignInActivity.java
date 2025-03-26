package com.example.projectnt118;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.SignInRequest;
import com.example.projectnt118.modle.SignInResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignInActivity extends AppCompatActivity {
    private EditText et_username, et_password;
    private ImageView back_signin;
    private TextView tv_forget_password;
    private Button btn_signin;
    private ApiService apiService;
    private boolean passwordShowing =false;
    ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        back_signin = findViewById(R.id.back_signin);
        btn_signin = findViewById(R.id.btn_signin);
        ImageView icon_password = findViewById(R.id.icon_password);
        icon_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordShowing) {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordShowing = false;
                    icon_password.setImageResource(R.drawable.show);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    passwordShowing = true;
                    icon_password.setImageResource(R.drawable.hidden);
                }
                et_password.setSelection(et_password.getText().length());
            }

        });
        back_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        tv_forget_password = findViewById(R.id.tv_forget_password);
        tv_forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                Intent intent = new Intent(SignInActivity.this, ForgotPwActivity.class);
                startActivity(intent);
            }
        });
        Retrofit retrofit = RetrofitClient.getClient("https://linhlinh.onrender.com/api/v1/auth/");
        apiService = retrofit.create(ApiService.class);
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.button_scale);
                v.startAnimation(animation);

                signIn();

            }
        });
        

    }

    private void signIn() {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        SignInRequest request = new SignInRequest(username, password);
        apiService.signIn(request).enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful()) {
                    SignInResponse signInResponse = response.body();
                    Toast.makeText(SignInActivity.this, signInResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                Toast.makeText(SignInActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}