package com.example.rltn4.tnavi_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowRelatedResultActivity extends AppCompatActivity {

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_related_result);

        // 이전 엑티비티에서 건내받은 데이터를 가지고 textview 내용을 초기화시켜준다.
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(getIntent().getStringExtra("location"));
    }
}
