package com.example.xxfin.recommendationsystemtesina;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        user = FirebaseAuth.getInstance().getCurrentUser();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, PrincipalActivity.class));
                /*if(user != null){
                    startActivity(new Intent(SplashActivity.this, PrincipalActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(SplashActivity.this, ActivityLogin.class));
                    finish();
                }*/
            }
        },3000);
    }
}
