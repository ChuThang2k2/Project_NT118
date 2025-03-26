package com.example.projectnt118;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class NotiPasswordActivity extends AppCompatActivity {
    Button btn_signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_noti_password);
        btn_signin = findViewById(R.id.btn_signin);
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(NotiPasswordActivity.this, R.anim.button_scale);
                v.startAnimation(animation);
                Intent intent = new Intent(NotiPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}