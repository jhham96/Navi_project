package com.example.rltn4.tnavi_project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    final int MY_PERMISSION_REQUEST_CODE = 100;
    // 검색결과 저장할 변수 선언
    private ListViewItem listViewItem;

    // 엑티비티에서 사용할 리스트뷰 및 검색창과 관련된 변수 선언
    private ListView listView;
    private ListViewAdapter adapter;
    private ImageButton searchButton;
    private TextView start, finish;

    // 최근 히스토리를 저장할 DB와 관련된 변수 선언
    private Gson gson;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("intent333333",String.format("%s",((TMapBox)data.getSerializableExtra("data")).getName()));
        if(resultCode == 400) {

        } else {
            if(data.getStringExtra("value").equals("1")) {
                listViewItem.settMapBoxStart(((TMapBox)data.getSerializableExtra("data")));
//            TMapPOIItem_child tMapPOIItem_child = (TMapPOIItem_child) getIntent().getSerializableExtra("data");
//            listViewItem.settMapPOIItemsStart(tMapPOIItem_child.gettMapPOIItem());
                // listViewItem.settMapPOIItemsStart(((TMapPOIItem_child) getIntent().getParcelableExtra("data")).gettMapPOIItem());
                start.setText(listViewItem.gettMapBoxStart().getName());
            }
            else {
                listViewItem.settMapBoxFinish(((TMapBox)data.getSerializableExtra("data")));
                //            listViewItem.settMapPOIItemsFinish(((TMapPOIItem_child) getIntent().getParcelableExtra("data")).gettMapPOIItem());
                //            finish.setText(listViewItem.gettMapPOIItemsFinish().getPOIName());
                finish.setText(listViewItem.gettMapBoxFinish().getName());
            }
        }
    }

    // 시작
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CODE);

        listViewItem = new ListViewItem();

        listView = (ListView) findViewById(R.id.ListView);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        start = (TextView) findViewById(R.id.startLocation);
        finish = (TextView) findViewById(R.id.finishLocation);

        // 어뎁터를 생성해 리스트뷰와 연결한다.
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        // 히스토리 초기화(DB와 동기화)
        initHistory();

        // 저장 번들 확인
        if (savedInstanceState != null) {
            String data = savedInstanceState.getString("key1");
            start.setText(data);
            data = savedInstanceState.getString("key2");
            finish.setText(data);
        }

        // 검색버튼 클릭리스너
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 빈칸 예외처리(출발지, 도착지 모두 필요 (구현완료)
                if (start.getText().toString().equals("") || finish.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "출발지, 도착지를 지정해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 중복체크 (구현완료)
                /* 1. listview에서 데이터를 가져옴.
                 *  2. 하나씩 비교
                 *  3. 있으면 push 전에 return(종료), 없으면 for문 다 돌고 일반순서대로 진행 */
                for(int i = 0; i < adapter.getCount(); i++) {
                    ListViewItem listViewItem = (ListViewItem) adapter.getItem(i);
                    String s = listViewItem.gettMapBoxStart().getName();
                    String f = listViewItem.gettMapBoxFinish().getName();

//                    if(s.equals((start.getText().toString())) && f.equals(finish.getText().toString())) {
//                        Toast.makeText(getApplicationContext(), "중복되는 내용이 있어, 바로 연결합니다.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                }

                // 20개 초과시 가장 오래된 데이터 삭제후 최신데이터 삽입함으로써 20개 유지(*******구현필요)
                /* 1. 20개 넘어가는지 확인
                 *  2. 넘어갈시 인덱스 20번째 해당하는 데이터 삭제
                 *  3. 새로운 데이터 추가
                 *   * 이로써 데이터는 20개가 계속 유지되게 된다.(인덱스 0 ~ 19 고정으로 유지된다.) */
                if(adapter.getCount() > 19) {
                    adapter.deleteItem(19);    // 옛날 데이터 삭제
                }
                // 최신 데이터 삽입
                adapter.addItem(listViewItem);
                adapter.notifyDataSetChanged();

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Location", (Serializable) listViewItem);
                startActivity(intent);
            }
        });

        // 위에서 생성한 listview에 클릭 이벤트 핸들러 정의.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // 클릭한 item을 get
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                // 검색창에 선택한 데이터 내용 삽입
                listViewItem = item;
                start.setText(listViewItem.gettMapBoxStart().getName());
                finish.setText(listViewItem.gettMapBoxFinish().getName());

                // 기록부분 삭제 및 전체삭제 기능(고려중, 버튼을 추가로 만들어야 함)
            }
        });

        // TextView창을 클릭하면 다음 입력엑티비티로 넘어간다.
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 생성 및 이동
                Intent intent = new Intent(getApplicationContext(), SetLocationActivity.class);
                intent.putExtra("value", "1");
                startActivityForResult(intent,1);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 생성 및 이동
                Intent intent = new Intent(getApplicationContext(), SetLocationActivity.class);
                intent.putExtra("value", "2");
                startActivityForResult(intent,2);
            }
        });

    }
    private void initHistory() {
        SharedPreferences sp = getSharedPreferences("main_activity_his", MODE_PRIVATE); // main_activity_his라는 preference를 참조
        ArrayList<String> preference_keys = new ArrayList<>();

        // ArrayList에 키값 모두 저장
        Collection<?> col = sp.getAll().values();
        Iterator<?> it = col.iterator();

        while(it.hasNext()) {
            preference_keys.add((String)it.next());
        }

        gson = new GsonBuilder().create();

        // 키값 하나하나 다시 gson(객체)형태로 변환해 객체형 ArrayList에 추가
        for(int i = 0; i < preference_keys.size(); i++) {
            Log.i("get ID", preference_keys.get(i));
            gson.fromJson(preference_keys.get(i), ListViewItem.class);
            ListViewItem listViewItem = gson.fromJson(preference_keys.get(i), ListViewItem.class);
            adapter.addItem(listViewItem);
        }
        adapter.notifyDataSetChanged();     // adapter 최신화

    }
    public void onSwitchButtonClicked(View v) {

        // Get TextView Objects
        TextView TextView1 = (TextView)findViewById(R.id.startLocation);
        TextView TextView2 = (TextView)findViewById(R.id.finishLocation);

        // Get TextView
        String string1 = TextView1.getText().toString();
        String string2 = TextView2.getText().toString();

        // swap(switch)
        if(TextView1.getText().toString().equals("") && TextView2.getText().toString().equals("")) {
            return;
        } else if(TextView1.getText().toString().equals("")) {
            TMapBox tMapBox_tmp = new TMapBox(listViewItem.gettMapBoxFinish());
            listViewItem.settMapBoxFinish(listViewItem.gettMapBoxStart());
            listViewItem.settMapBoxStart(tMapBox_tmp);
        } else /*if(TextView2.getText().toString().equals(""))*/ {
            TMapBox tMapBox_tmp = new TMapBox(listViewItem.gettMapBoxStart());
            listViewItem.settMapBoxStart(listViewItem.gettMapBoxFinish());
            listViewItem.settMapBoxFinish(tMapBox_tmp);
        }
        TextView1.setText(string2);
        TextView2.setText(string1);

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sp = getSharedPreferences("main_activity_his", MODE_PRIVATE); // main_activity_his라는 preference를 참조
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();         // DB data 초기화

        // DB에 새로운 값추가
//        gson = new GsonBuilder().create();

        for(Integer i = adapter.getCount() - 1 ; i >= 0  ; i--) {
            ListViewItem data = new ListViewItem(adapter.getItem(i));
            editor.putString(i.toString(), gson.toJson(data, ListViewItem.class));  // 객체->Json으로 변환한 값 추가
        }
        editor.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // 검색결과 엑티비티를 종료하면 다시 MainActivity로 되돌아 올때 이 메소드를 거친다.
        // 검색결과 Item을 받고 데이터로 초기화 해준다.
        String is_start_or_finish = getIntent().getStringExtra("value");

        if(is_start_or_finish == null)
            return;

        if(is_start_or_finish.equals("1")) {
            TMapBox tMapBox = (TMapBox) getIntent().getSerializableExtra("data");
            listViewItem.settMapBoxStart(tMapBox);
//            TMapPOIItem_child tMapPOIItem_child = (TMapPOIItem_child) getIntent().getSerializableExtra("data");
//            listViewItem.settMapPOIItemsStart(tMapPOIItem_child.gettMapPOIItem());
            // listViewItem.settMapPOIItemsStart(((TMapPOIItem_child) getIntent().getParcelableExtra("data")).gettMapPOIItem());
            start.setText(listViewItem.gettMapBoxStart().getName());
        } else if(is_start_or_finish.equals("2")) {
            TMapBox tMapBox = (TMapBox) getIntent().getSerializableExtra("data");
            listViewItem.settMapBoxFinish(tMapBox);
//            listViewItem.settMapPOIItemsFinish(((TMapPOIItem_child) getIntent().getParcelableExtra("data")).gettMapPOIItem());
//            finish.setText(listViewItem.gettMapPOIItemsFinish().getPOIName());
            finish.setText(listViewItem.gettMapBoxFinish().getName());
        }
    }
}
