package com.example.jh.image;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0, 3000); //3초후 화면전환

    }
}
