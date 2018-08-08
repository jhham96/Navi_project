package com.example.jh.image;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

public class Main2Activity extends AppCompatActivity {

    static final String[] LIST_MENU = {"List1", "List2", "List3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ListView listView;
        ListViewAdapter adapter;

        adapter = new ListViewAdapter();

        listView = (ListView) findViewById(R.id.ListView);
        listView.setAdapter(adapter);

        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.walking), "중앙대학교", "전남 화순군 화순읍 내평길 27이상으로 길어질 경우");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.walking), "Foxdife", "Account2");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.walking), "NewYourk", "Account3");

        // 위에서 생성한 listview에 클릭 이벤트 핸들러 정의.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String descStr = item.getDesc() ;
                Drawable iconDrawable = item.getIcon() ;

                // TODO : use item data.
            }
        }) ;
    }

    public void onSwitchButtonClicked(View v) {
        // Get editText Objects
        EditText editText1 = (EditText)findViewById(R.id.editText1);
        EditText editText2 = (EditText)findViewById(R.id.editText2);

        // Get editText
        String string1 = editText1.getText().toString();
        String string2 = editText2.getText().toString();

        // swap(switch)
        editText1.setText(string2);
        editText2.setText(string1);
    }
}
