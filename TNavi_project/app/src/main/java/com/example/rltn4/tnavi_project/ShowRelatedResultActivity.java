package com.example.rltn4.tnavi_project;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

public class ShowRelatedResultActivity extends AppCompatActivity {

    // 출발지검색 / 도착지검색 구분변수
    private String is_start_or_finish;

    // 리스트뷰 관련 변수들
    private ListView listView;
    private ListViewAdapter2 adapter;
    private ArrayList<TMapPOIItem> tMapPOIItemsList;
    private TMapData tMapData;
    private TMapView tMapView;


    // 레이아웃 구성변수
    private TextView textView;
    private ImageButton imageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_related_result);

        // tMapData 생성
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("f51c315c-f7e2-42f9-b290-0d2f818c07d7");
        tMapData = new TMapData();

        // 인텐트 값 전달받기
        is_start_or_finish = getIntent().getStringExtra("value");

        // 이전 엑티비티에서 건내받은 데이터를 가지고 textview 내용을 초기화시켜준다.
        // textView 역시 누를 경우, 수정하는 의미로 인식해 이전 엑티비티로 되돌아가 수정할 수 있게 한다.
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(getIntent().getStringExtra("location"));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(300,intent);  // 이전으로 이동
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
        listView = (ListView)findViewById(R.id.ListView);
        adapter = new ListViewAdapter2(this);
        listView.setAdapter(adapter);


        // 검색어에 연관된 데이터들을 tMapPOIItemsList (ArrayList형식)로 생성
        new Thread() {
            public void run() {
                try {
                    tMapPOIItemsList = tMapData.findAllPOI(textView.getText().toString());

                    // listview에 내용 삽입
                    for(int i = 0; i < tMapPOIItemsList.size(); i++) {
                        adapter.addItem(tMapPOIItemsList.get(i));
                    }

                    (ShowRelatedResultActivity.this).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();     // 최신화 후 내용 출력
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        // listview item 클릭 이벤트 정의
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 클릭한 데이터 값 가져오기
                TMapPOIItem item_tmp = (TMapPOIItem) parent.getItemAtPosition(position);
                TMapBox tMapBox = new TMapBox();

                tMapBox.setLat(item_tmp.getPOIPoint().getLatitude());
                tMapBox.setLon(item_tmp.getPOIPoint().getLongitude());
                tMapBox.setName(item_tmp.getPOIName());

//                TMapPOIItem_child item = new TMapPOIItem_child();
//                item.settMapPOIItem(item_tmp);

                // 데이터를 MainActivity에 넘겨주기 위해 Intent 생성 후 MainActivity시작
                Intent intent = new Intent();
                intent.putExtra("value", is_start_or_finish);
                intent.putExtra("data", tMapBox);

                // 메인 엑티비티로 이동하기 전, 이전 엑티비티인 검색 엑티비티, 검색결과 엑티비티를 종료시킨다.
//                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

                setResult(100,intent);
                finish();

            }
        });

    }
}

class TMapBox implements Serializable {
    private String name = null;
    private double lat;
    private double lon;

    public TMapBox () {

    }
    public TMapBox (TMapBox t) {
        this.name = t.getName();
        this.lat = t.getLat();
        this.lon = t.getLon();
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

//class TMapPOIItem_child extends TMapPOIItem implements Serializable {
//    private static final long serialVersionUID = 2220255028278687727L;
//    private TMapPOIItem tMapPOIItem;
//
//    public TMapPOIItem_child() {
//
//    }
//
//    public TMapPOIItem gettMapPOIItem() {
//        return tMapPOIItem;
//    }
//
//    public void settMapPOIItem(TMapPOIItem tMapPOIItem) {
//        this.tMapPOIItem = tMapPOIItem;
//    }
//}

//class TMapPOIItem_child extends TMapPOIItem implements Serializable, Parcelable {
//
//    private static final long serialVersionUID = 2220255028278687727L;
//    private TMapPOIItem tMapPOIItem;
//
//    public TMapPOIItem_child(TMapPOIItem t){
//        this.tMapPOIItem = t;
//    }
//
//    protected TMapPOIItem_child(Parcel in) {
//    }
//
//    public static final Creator<TMapPOIItem_child> CREATOR = new Creator<TMapPOIItem_child>() {
//        @Override
//        public TMapPOIItem_child createFromParcel(Parcel in) {
//            return new TMapPOIItem_child(in);
//        }
//
//        @Override
//        public TMapPOIItem_child[] newArray(int size) {
//            return new TMapPOIItem_child[size];
//        }
//    };
//
//    public TMapPOIItem gettMapPOIItem() {
//        return tMapPOIItem;
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        //   dest.writeSerializable(this);
//    }
//}