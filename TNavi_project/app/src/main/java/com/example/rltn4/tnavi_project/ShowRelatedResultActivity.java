package com.example.rltn4.tnavi_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowRelatedResultActivity extends AppCompatActivity {

    private TextView textView;
    private ImageButton imageButton;
    private ListView listView;
    private ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_related_result);

        // 이전 엑티비티에서 건내받은 데이터를 가지고 textview 내용을 초기화시켜준다.
        // textView 역시 누를 경우, 수정하는 의미로 인식해 이전 엑티비티로 되돌아가 수정할 수 있게 한다.
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(getIntent().getStringExtra("location"));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // back button을 누르면 이전 엑티비티로 되돌아간다. 즉 현재 엑티비티를 종료시키면 된다.
        imageButton = (ImageButton)findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // listview 초기화
        listView = (ListView)findViewById(R.id.listview);
        final ArrayList<String> items = new ArrayList<String>() ;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        adapter.add("중앙대학교 서울캠퍼스");
        adapter.add("중앙대학교 안성캠퍼스");
        adapter.add("중앙대학교병원");
        adapter.add("중앙대학교병원 장례식장");
        adapter.add("중앙대학교 서울캠퍼스 후문");
        adapter.add("중앙대학교 서울캠퍼스 중문");
        adapter.add("중앙대학교 서울캠퍼스 정문");


        adapter.notifyDataSetChanged();

    }
}
