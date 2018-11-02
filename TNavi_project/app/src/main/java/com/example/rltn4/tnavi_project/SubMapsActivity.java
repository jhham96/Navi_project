package com.example.rltn4.tnavi_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

public class SubMapsActivity extends AppCompatActivity {
    private TMapView tMapView;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_maps);

        // 전달 받은 Intent를 생성한다.
        intent = new Intent(this.getIntent());
        TMapBox tMapBox = (TMapBox) intent.getSerializableExtra("data");

        // T Map을 생성한다.
        FrameLayout frameLayoutTmap = (FrameLayout)findViewById(R.id.frameLayoutTmap);
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("f51c315c-f7e2-42f9-b290-0d2f818c07d7");
//        tMapView.setCompassMode(true); // 단말의 방향에 따라 지도를 움직인다.
//        tMapView.setTrackingMode(true); // 화면 중심을 단말의 현재 위치로 이동시킨다.
//        tMapView.setMarkerRotate(true); // 나침반 회전 시 Marker 이미지를 같이 회전시킨다.
        tMapView.setZoomLevel(18);
        frameLayoutTmap.addView(tMapView);

        // Marker를 생성한다.
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false); // 마커 아이콘 사이즈 조정

        TMapPoint selectedPoint = new TMapPoint(tMapBox.getLat(), tMapBox.getLon());
        TMapMarkerItem selectedMarker = new TMapMarkerItem();

        selectedMarker.setIcon(bitmap); // 마커 아이콘 지정
        selectedMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        selectedMarker.setTMapPoint(selectedPoint); // 마커의 좌표 지정
        tMapView.addMarkerItem("selectedPoint", selectedMarker); // 지도에 마커 추가

        // 화면 중심을 Marker 로 한다.
        tMapView.setCenterPoint(tMapBox.getLon(), tMapBox.getLat());

        // back_btn 설정이다.
        Button back_btn = (Button)findViewById(R.id.button1);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 뒤로 간다.
                finish();
            }
        });

        // ok_btn 설정이다.
        Button ok_btn = (Button)findViewById(R.id.button2);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 데이터를 MainActivity에 넘겨 준다.
                setResult(100, intent);
                finish();
            }
        });
    }
}
