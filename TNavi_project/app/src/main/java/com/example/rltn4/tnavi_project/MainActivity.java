package com.example.rltn4.tnavi_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TMapView tMapView;
    private TMapData tMapData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout frameLayoutTmap = (FrameLayout)findViewById(R.id.frameLayoutTmap);
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("f51c315c-f7e2-42f9-b290-0d2f818c07d7");
        frameLayoutTmap.addView(tMapView);

        tMapData = new TMapData();

        new Thread() {
            public void run() {
                try {
                    ArrayList<TMapPOIItem> tMapPOIItemArrayList1 = tMapData.findAllPOI("중앙대학교");
                    ArrayList<TMapPOIItem> tMapPOIItemArrayList2 = tMapData.findAllPOI("흑석역");

                    TMapPOIItem start = (TMapPOIItem) tMapPOIItemArrayList1.get(0);
                    TMapPOIItem end = (TMapPOIItem) tMapPOIItemArrayList2.get(0);

                    TMapMarkerItem markerItem1 = new TMapMarkerItem();
                    TMapMarkerItem markerItem2 = new TMapMarkerItem();

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false); // 마커 아이콘 사이즈 조정

                    markerItem1.setIcon(bitmap); // 마커 아이콘 지정
                    markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem1.setTMapPoint(start.getPOIPoint()); // 마커의 좌표 지정
                    tMapView.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가

                    markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                    markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem2.setTMapPoint(end.getPOIPoint()); // 마커의 좌표 지정
                    tMapView.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

                    Bundle bun = new Bundle();

                    TMapPolyLine tMapPolyLine = tMapData.findPathData(start.getPOIPoint(), end.getPOIPoint());
                    tMapPolyLine.setLineColor(Color.BLUE);
                    tMapPolyLine.setLineWidth(2);
                    tMapView.setCenterPoint(start.getPOIPoint().getLongitude(), start.getPOIPoint().getLatitude());
                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);

                } catch(java.io.IOException e) {
                    Log.e("java.io.IOException e: ", "java.io.IOException e");
                } catch(javax.xml.parsers.ParserConfigurationException e) {
                    Log.e("PCException: ", "javax.xml.parsers.ParserConfigurationException");
                } catch(org.xml.sax.SAXException e) {
                    Log.e("SAXException: ", "org.xml.sax.SAXException");
                }
            }
        }.start();

//        tMapData.findAllPOI("중앙대학교", new TMapData.FindAllPOIListenerCallback() {
//            @Override
//            public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
//                for(int i =0; i < arrayList.size(); i++) {
//                    TMapPOIItem item = (TMapPOIItem) arrayList.get(i);
//                    Log.d("POI Name: ", item.getPOIName().toString() + ", " +
//                            "Address: " + item.getPOIAddress().replace("null","") + ", " +
//                            "Point: " + item.getPOIPoint().toString() + " , " +
//                            Double.toString(item.getPOIPoint().getLatitude()) + " " + Double.toString(item.getPOIPoint().getLongitude())
//                            );
//                }
//            }
//        });
    }
}
