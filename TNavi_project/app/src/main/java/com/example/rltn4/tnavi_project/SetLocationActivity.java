package com.example.rltn4.tnavi_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SetLocationActivity extends AppCompatActivity {

    private EditText editText;
    private ListView listView;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        // editText에 연결된 소프트키보드 검색버튼 액션리스너를 활성화 시켜준다.
        editText = (EditText)findViewById(R.id.edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // do it what?
                    // 다음 ShowRelatedResultActivity로 넘어간다.
                    Intent intent = new Intent(getApplicationContext(), ShowRelatedResultActivity.class);

                    // 다음 엑티비티로 넘어가기전, 결과값을 넘겨주기 위해 intent에 edit창의 내용을 저장한다.
                    intent.putExtra("location", editText.getText().toString());
                    startActivity(intent);

                    // for debug
                    Log.d("test", "검색버튼이 눌렸어요~!");

                    return true;
                }
                return false;
            }
        });

        // listview 초기화
        listView = (ListView)findViewById(R.id.listview);
        final ArrayList<String> items = new ArrayList<String>() ;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        adapter.add("중앙대학교");
        adapter.add("서울대학교");
        adapter.add("흑석역");
        adapter.add("용산역");
        adapter.add("SK핸드볼경기장");

        adapter.notifyDataSetChanged();

    }
}
