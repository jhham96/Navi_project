package com.example.rltn4.tnavi_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SetLocationActivity extends AppCompatActivity {

    private EditText editText;
    private ListView listView;
    private ArrayAdapter adapter;
    private ArrayList<String> listitems = new ArrayList<>();
    private String is_start_or_finish;
    private ImageButton imageButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("intent22222222",String.format("%s",((TMapBox)data.getSerializableExtra("data")).getName()));
        if(resultCode == 100) {
            if(data.getStringExtra("value").equals("1")) { //start
                setResult(100,data);
                finish();
            }
            else if(data.getStringExtra("value").equals("2")){ //finish
                setResult(200,data);
                finish();
            }
        } else if(resultCode == 300) {
            ;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        // MainActivity에서 받은 Intent정보를 받아 계속 전달한다.(메인 엑티비티로 되돌아갈시, 이게 출발데이터인지, 도착데이터인지 구분하기 위해)
        is_start_or_finish = getIntent().getStringExtra("value");

        // listview 초기화 및 어뎁터 설정
        listView = (ListView)findViewById(R.id.ListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listitems);
        listView.setAdapter(adapter);

        initHistory();  // history 초기화

        // editText에 연결된 소프트키보드 검색버튼 액션리스너를 활성화 시켜준다.
        editText = (EditText)findViewById(R.id.edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if(adapter.getCount() > 19) {
                        adapter.remove(adapter.getItem(19));    // 20개가 넘어가면 맨 마지막에 있는 것들을 삭제하도록 한다.
                    }
                    // adapter에 추가한다.
                    adapter.insert(editText.getText().toString(), 0);
                    adapter.notifyDataSetChanged();     // adapter 최신화

                    // sharedPreferenced 최신화
                    // 출발지인지, 도착지 검색인지 판단해 따로 검색기록을 저장해야 한다. 따라서 다른 preferenced를 사용하게 한다.
                    String preferenced_name = null;
                    if(is_start_or_finish.equals("1"))
                        preferenced_name = "set_location_activity_his1";
                    else if(is_start_or_finish.equals("2"))
                        preferenced_name = "set_location_activity_his2";

                    // SharedPreferenced에 등록
                    //      1. DB 초기화
                    //      2. 리스트뷰 내용 삽입
                    //      3. 종료
                    SharedPreferences sp = getSharedPreferences(preferenced_name, MODE_PRIVATE); // set_location_activity_his라는 preference를 참조
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();         // DB data 초기화

                    for(int i = 0 ; i < adapter.getCount()  ; i++) {
                        editor.putString(String.format("%s", i), String.format("%s", adapter.getItem(i)));  // adapter -> sharedPreferenced에 저장
                    }
                    editor.commit();

                    // 다음 ShowRelatedResultActivity로 넘어간다.
                    Intent intent = new Intent(getApplicationContext(), ShowRelatedResultActivity.class);
                    intent.putExtra("location", editText.getText().toString());         // 다음 엑티비티로 넘어가기전, 결과값을 넘겨주기 위해 intent에 edit창의 내용을 저장한다.
                    intent.putExtra("value", is_start_or_finish);                         // MainActivity에서 받은 Intent정보를 받아 계속 전달한다.(메인 엑티비티로 되돌아갈시, 이게 출발데이터인지, 도착데이터인지 구분하기 위해)

                    startActivityForResult(intent,3);
                    return true;
                }
                return false;
            }
        });

//        // -------------------- 수 정 --------------------
//        // adapter에 추가한다.
//        adapter.insert("중앙대학교", 0);
//
//        if(adapter.getCount() > 19) {
//            adapter.remove(adapter.getItem(19));    // 20개가 넘어가면 맨 마지막에 있는 것들을 삭제하도록 한다.
//        }
//
//        // 다음 ShowRelatedResultActivity로 넘어간다.
//        Intent intent = new Intent(getApplicationContext(), ShowRelatedResultActivity.class);
//        intent.putExtra("location", "중앙대학교");         // 다음 엑티비티로 넘어가기전, 결과값을 넘겨주기 위해 intent에 edit창의 내용을 저장한다.
//        intent.putExtra("value", is_start_or_finish);                         // MainActivity에서 받은 Intent정보를 받아 계속 전달한다.(메인 엑티비티로 되돌아갈시, 이게 출발데이터인지, 도착데이터인지 구분하기 위해)
//
//        startActivityForResult(intent,3);
//
//        // ----------------------------------------------


        // back button 초기화
        imageButton = (ImageButton) findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(400,intent);  // 이전으로 이동
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // 클릭한 item을 get
                String str = (String) parent.getItemAtPosition(position) ;

                // 검색창에 선택한 데이터 내용 삽입
                editText.setText(str);

                // adapter에 추가한다.
                adapter.insert(editText.getText().toString(), 0);

                if(adapter.getCount() > 19) {
                    adapter.remove(adapter.getItem(19));    // 20개가 넘어가면 맨 마지막에 있는 것들을 삭제하도록 한다.
                }

                adapter.notifyDataSetChanged();     // adapter 최신화

                // sharedPreferenced 최신화
                // 출발지인지, 도착지 검색인지 판단해 따로 검색기록을 저장해야 한다. 따라서 다른 preferenced를 사용하게 한다.
                String preferenced_name = null;
                if(is_start_or_finish.equals("1"))
                    preferenced_name = "set_location_activity_his1";
                else if(is_start_or_finish.equals("2"))
                    preferenced_name = "set_location_activity_his2";

                // SharedPreferenced에 등록
                //      1. DB 초기화
                //      2. 리스트뷰 내용 삽입
                //      3. 종료
                SharedPreferences sp = getSharedPreferences(preferenced_name, MODE_PRIVATE); // set_location_activity_his라는 preference를 참조
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();         // DB data 초기화

                for(int i = 0 ; i < adapter.getCount()  ; i++) {
                    editor.putString(String.format("%s", i), String.format("%s", adapter.getItem(i)));  // adapter -> sharedPreferenced에 저장
                }
                editor.commit();

                // 다음 엑티비티 실행
                Intent intent = new Intent(getApplicationContext(), ShowRelatedResultActivity.class);
                intent.putExtra("location", editText.getText().toString());         // 다음 엑티비티로 넘어가기전, 결과값을 넘겨주기 위해 intent에 edit창의 내용을 저장한다.
                intent.putExtra("value", is_start_or_finish);                         // MainActivity에서 받은 Intent정보를 받아 계속 전달한다.(메인 엑티비티로 되돌아갈시, 이게 출발데이터인지, 도착데이터인지 구분하기 위해)

                startActivityForResult(intent,3);

                // 기록부분 삭제 및 전체삭제 기능(고려중, 버튼을 추가로 만들어야 함)
            }
        });


    }

//    @Override
    // Back key 눌러서 뒤로 가는 경우 구현.(왜 안되는지 파악 필요)
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent();
//        setResult(400,intent);  // 이전으로 이동
//        finish();
//
//    }

    private void initHistory() {

        // 출발지인지, 도착지 검색인지 판단해 따로 검색기록을 저장해야 한다. 따라서 다른 preferenced를 사용하게 한다.
        String preferenced_name = null;
        if(is_start_or_finish.equals("1"))
            preferenced_name = "set_location_activity_his1";
        else if(is_start_or_finish.equals("2"))
            preferenced_name = "set_location_activity_his2";

        SharedPreferences sp = getSharedPreferences(preferenced_name, MODE_PRIVATE); // preference 설정

        // 키값 전부받아서 추가로 ListView와 연결된 어뎁터 내부의 ArrayList에 저장
        Collection<?> col = sp.getAll().values();
        Iterator<?> it = col.iterator();

        while(it.hasNext()) {
            adapter.add((String)it.next());   // 데이터 추가 (앞쪽으로 밀어넣는게 안되서 그냥 순서대로로 수정.)
        }
        adapter.notifyDataSetChanged();     // adapter 최신화

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // 출발지인지, 도착지 검색인지 판단해 따로 검색기록을 저장해야 한다. 따라서 다른 preferenced를 사용하게 한다.
//        String preferenced_name = null;
//        if(is_start_or_finish.equals("1"))
//            preferenced_name = "set_location_activity_his1";
//        else if(is_start_or_finish.equals("2"))
//            preferenced_name = "set_location_activity_his2";
//
//        // SharedPreferenced에 등록
//        //      1. DB 초기화
//        //      2. 리스트뷰 내용 삽입
//        //      3. 종료
//        SharedPreferences sp = getSharedPreferences(preferenced_name, MODE_PRIVATE); // set_location_activity_his라는 preference를 참조
//        SharedPreferences.Editor editor = sp.edit();
//        editor.clear();         // DB data 초기화
//
//        for(int i = adapter.getCount() - 1 ; i >= 0  ; i--) {
//            editor.putString(String.format("%s", i), String.format("%s", adapter.getItem(i)));  // adapter -> sharedPreferenced에 저장
//        }
//        editor.commit();
//    }


}
