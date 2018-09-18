package com.example.jh.image;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private ListView listView;
    private ListViewAdapter adapter;
    private ImageButton searchButton;
    private EditText start, finish;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        listView = (ListView) findViewById(R.id.ListView);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        start = (EditText) findViewById(R.id.startLocation);
        finish = (EditText) findViewById(R.id.finishLocation);

        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        initHistory();  // 히스토리 초기화(DB와 동기화)

        // 검색버튼을 누르는 순간
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 빈칸 예외처리(출발지, 도착지 모두 필요 (구현완료)
                if (start.getText().toString().equals("") || finish.getText().toString().equals("")) {
                    Toast.makeText(Main2Activity.this, "출발지, 도착지를 지정해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 중복체크 (구현완료)
                /* 1. listview에서 데이터를 가져옴.
                 *  2. 하나씩 비교
                 *  3. 있으면 push 전에 return(종료), 없으면 for문 다 돌고 일반순서대로 진행 */
                for(int i = 0; i < adapter.getCount(); i++) {
                    ListViewItem listViewItem = (ListViewItem) adapter.getItem(i);
                    String s = listViewItem.getTitle();
                    String f = listViewItem.getDesc();

                    if(s.equals((start.getText().toString())) && f.equals(finish.getText().toString())) {
                        Toast.makeText(Main2Activity.this, "중복되는 내용이 있어, 바로 연결합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 20개초과시 옛데이터 삭제후 최신데이터 삽입(*******구현필요)
                /* 1. 20개 넘어가는지 확인
                 *  2. 넘어갈시 인덱스 20번째 해당하는 데이터 삭제
                 *  3. 새로운 데이터 추가
                 *   * 이로써 데이터는 20개가 계속 유지되게 된다.(인덱스 0 ~ 19 고정으로 유지된다.) */
                /*if(adapter.getCount() > 20) {
                    adapter.deleteItem(19);     // 어뎁터에서도 지우고
                    // DB에서도 지워야 한다.
//                    databaseReference.child("Location").removeValue()
                }*/


                DataSave data = new DataSave(start.getText().toString(), finish.getText().toString());
                databaseReference.child("Location").push().setValue(data);          // DB에 내용추가
            }
        });

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

                // 기록부분 삭제 및 전체삭제 기능(고려중, 버튼을 추가로 만들어야 함)
            }
        }) ;
    }

    private void addHistory(DataSnapshot dataSnapshot, ListViewAdapter adapter) {
        DataSave dataSave = dataSnapshot.getValue(DataSave.class);
        Log.e("LOG", dataSave.getStart() + " and " + dataSave.getFinish());
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.walking), dataSave.getStart(), dataSave.getFinish());
    }

    // 데이터베이스에서 데이터를 받아 아이템리스트를 최신화 하는 함수 필요
    // 1. DB에서 데이터 받아옴(출발지, 도착지)
    // 2. 받아온 데이터 각각 addItem 함수 이용해서 add해줌
    //    * 이후 검색한 장소기록들은 검색버튼 누르는 순간 바로 item에 추가시키도록 한다.
    //      -> 여기서 가장 헷갈리는건 이전 화면으로 다시 돌아올때, 기존에 추가시켰던 item들을 처음부터 다시 추가시켜야 하는건지..(내 생각엔 이 엑티비티를 종료시키지 않았을 경우 사라지지 않을걸로 추측)
    // * 해야 하는것 ==> DB에 data 전달 및 받아오는 과정?

    private void initHistory() {
        databaseReference.child("Location").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addHistory(dataSnapshot, adapter);
                adapter.notifyDataSetChanged();     // adapter 최신화
                Log.e("LOG", "s:"+s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onSwitchButtonClicked(View v) {
        // Get editText Objects
        EditText editText1 = (EditText)findViewById(R.id.startLocation);
        EditText editText2 = (EditText)findViewById(R.id.finishLocation);

        // Get editText
        String string1 = editText1.getText().toString();
        String string2 = editText2.getText().toString();

        // swap(switch)
        editText1.setText(string2);
        editText2.setText(string1);
    }
}
