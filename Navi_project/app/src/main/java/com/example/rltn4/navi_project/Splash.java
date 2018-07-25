package com.example.rltn4.navi_project;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler(){
          public void handleMessage(Message msg){
              super.handleMessage(msg);
              startActivity(new Intent(getApplicationContext(),MainActivity.class));
              finish();
          }
        };
        handler.sendEmptyMessageDelayed(0,3000);
    }
}
