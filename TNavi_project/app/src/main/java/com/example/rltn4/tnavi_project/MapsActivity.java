package com.example.rltn4.tnavi_project;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity {

    private TMapView tMapView;
    private TMapData tMapData;
    //    private GpsInfo gps;
    private Button change_btn;
    private ProgressBar percent_proBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FrameLayout frameLayoutTmap = (FrameLayout)findViewById(R.id.frameLayoutTmap);
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("f51c315c-f7e2-42f9-b290-0d2f818c07d7");
        tMapView.setCompassMode(true); // 단말의 방향에 따라 지도를 움직인다.
//        tMapView.setTrackingMode(true); // 화면 중심을 단말의 현재 위치로 이동시킨다.
        tMapView.setMarkerRotate(true); // 나침반 회전 시 Marker 이미지를 같이 회전시킨다.
        tMapView.setZoomLevel(18);
        frameLayoutTmap.addView(tMapView);

        tMapData = new TMapData();

        percent_proBar = (ProgressBar)findViewById(R.id.percent);
        percent_proBar.setIndeterminate(false);
        percent_proBar.setMax(100);
        percent_proBar.setProgress(80);

        change_btn = (Button)findViewById(R.id.button1);
        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        gps = new GpsInfo(this);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
//        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false); // 마커 아이콘 사이즈 조정
//
//        if (gps.isGetLocation()) {
//
//            TMapPoint currentPoint = new TMapPoint(gps.getLatitude(), gps.getLongitude());
//            TMapMarkerItem currentMarker = new TMapMarkerItem();
//
//            currentMarker.setIcon(bitmap); // 마커 아이콘 지정
//            currentMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
//            currentMarker.setTMapPoint(currentPoint); // 마커의 좌표 지정
//            tMapView.addMarkerItem("currentpoint", currentMarker); // 지도에 마커 추가
//
//        } else {
//            // GPS 를 사용할수 없으므로
//            gps.showSettingsAlert();
//        }
//
//        tMapView.setCenterPoint(gps.getLongitude(), gps.getLatitude());
//
//        Log.d("currentpoint: ", Double.toString(gps.getLatitude()) + ", " + Double.toString(gps.getLongitude()));

        new Thread() {
            public void run() {
                try {
                    ArrayList<TMapPOIItem> tMapPOIItemStartList = tMapData.findAllPOI("중앙대학교 병원"); // 출발지 검색을 위한 변수이다.
                    ArrayList<TMapPOIItem> tMapPOIItemEndList = tMapData.findAllPOI("흑석역"); // 도착지 검색을 위한 변수이다.

                    TMapPOIItem start = (TMapPOIItem) tMapPOIItemStartList.get(0); // 출발지를 나타내는 변수이다.
                    TMapPOIItem end = (TMapPOIItem) tMapPOIItemEndList.get(0); // 도착지를 나타내는 변수이다.

                    // API에서 제공하는 함수이나, 부정확한 정보를 출력한다.
//                    tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, start.getPOIPoint(), end.getPOIPoint(), new TMapData.FindPathDataAllListenerCallback() {
//                        @Override
//                        public void onFindPathDataAll(Document document) {
//                            Element root = document.getDocumentElement();
//                            NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
//                            for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
//                                NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
//                                for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
//                                    if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
//                                        Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
//                                    }
//                                }
//                            }
//                        }
//                    });

                    // 지도 위에 선을 나타내는 구현이다.
                    TMapPolyLine tMapPolyLine = tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start.getPOIPoint(), end.getPOIPoint());
                    tMapPolyLine.setLineColor(Color.BLUE);
                    tMapPolyLine.setLineWidth(2);
                    tMapView.setCenterPoint(start.getPOIPoint().getLongitude(), start.getPOIPoint().getLatitude());
                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);

//                    ArrayList<TMapMarkerItem> pathMarkerList = new ArrayList<>(); // 모든 경로를 마커로 표시하고자 사용하는 변수이다.
                    ArrayList<TMapMarkerItem> turningMarkerList = new ArrayList<>(); // 중간 지점을 마커로 표시하고자 사용하는 변수이다.
                    int index = 0; // 중간 지점을 마커로 표시하고자 사용하는 인덱스 변수이다.

                    ArrayList<TMapPoint> TMapPointList = tMapPolyLine.getLinePoint();
                    ArrayList<TMapPoint> pathList = new ArrayList<>(); // 경로에서 중복을 제외한 위도, 경도를 나타내는 변수이다.
                    ArrayList<TMapPoint> pointList = new ArrayList<>(); // 출발지, 도착지, 중간 지점의 위도, 경도를 나타내는 변수이다.

                    for (int i = 0; i < TMapPointList.size(); i++) {
                        if (!pathList.contains(TMapPointList.get(i))) {
                            pathList.add(TMapPointList.get(i));
                        }
                    }

                    Bitmap target = BitmapFactory.decodeResource(getResources(), R.drawable.target); // 출발지 및 도착지의 마커 이미지를 나타내는 변수이다.
                    target = Bitmap.createScaledBitmap(target, 50, 50, false); // 마커 아이콘 사이즈 조정

                    Bitmap turning = BitmapFactory.decodeResource(getResources(), R.drawable.turning); // 중간 지점의 마커 이미지를 나타내는 변수이다.
                    turning = Bitmap.createScaledBitmap(turning, 25, 25, false); // 마커 아이콘 사이즈 조정

                    // 출발지 마커를 위한 구현이다.
                    TMapMarkerItem startMark = new TMapMarkerItem();
                    startMark.setIcon(target);
                    startMark.setTMapPoint(pathList.get(0));
                    startMark.setPosition(0.5f, 1.0f);
                    tMapView.addMarkerItem("startMark", startMark);

                    // 도착지 마커를 위한 구현이다.
                    TMapMarkerItem endMark = new TMapMarkerItem();
                    endMark.setIcon(target);
                    endMark.setTMapPoint(pathList.get(pathList.size() - 1));
                    endMark.setPosition(0.5f, 1.0f);
                    tMapView.addMarkerItem("endMark", endMark);

//                    // 모든 경로를 마커로 표시한다.
//                    for ( int i = 0; i < pathList.size(); i++) {
//                        pathMarkerList.add(new TMapMarkerItem());
//                        pathMarkerList.get(i).setIcon(bitmap);
//                        pathMarkerList.get(i).setPosition(0.5f, 1.0f);
//                        pathMarkerList.get(i).setTMapPoint(pathList.get(i));
//                        String id = "pathMarkerList" + Integer.toString(i);
//                        tMapView.addMarkerItem(id, pathMarkerList.get(i));
//                    }

                    // 출발지를 기록한다.
                    pointList.add(0, pathList.get(0));

                    // 특정 각도가 넘는 지점을 마커로 표현한다.
                    for ( int i = 1; i < pathList.size() - 1; i++) {
                        double vector1_Latitude = pathList.get(i).getLatitude() - pathList.get(i-1).getLatitude();
                        double vector1_Longitude = pathList.get(i).getLongitude() - pathList.get(i-1).getLongitude();

                        double vector2_Latitude = pathList.get(i+1).getLatitude() - pathList.get(i).getLatitude();
                        double vector2_Longitude = pathList.get(i+1).getLongitude() - pathList.get(i).getLongitude();

                        // 각도가 얼마나 변했는지를 표현한다.
//                        Log.d("angle", Double.toString(Math.asin((vector1_Longitude * vector2_Latitude - vector1_Latitude * vector2_Longitude)
//                                /(Math.sqrt(Math.pow(vector1_Latitude, 2) + Math.pow(vector1_Longitude, 2)) * Math.sqrt(Math.pow(vector2_Latitude, 2) + Math.pow(vector2_Longitude, 2)))) * 57.2958));

                        double angle = (Math.asin((vector1_Longitude * vector2_Latitude - vector1_Latitude * vector2_Longitude)
                                /(Math.sqrt(Math.pow(vector1_Latitude, 2) + Math.pow(vector1_Longitude, 2)) * Math.sqrt(Math.pow(vector2_Latitude, 2) + Math.pow(vector2_Longitude, 2)))) * 57.2958);

                        // 특정 각도가 넘는지를 확인한다.
                        if((int) Math.abs(angle) > 30) {
                            // 중간 지점 마커 표시를 위한 구현이다.
                            turningMarkerList.add(new TMapMarkerItem());
                            turningMarkerList.get(index).setIcon(turning);
                            turningMarkerList.get(index).setTMapPoint(pathList.get(i));
                            String id = "pathMarker" + Integer.toString(index);
                            tMapView.addMarkerItem(id, turningMarkerList.get(index));

                            // 중간 지점을 기록한다.
                            pointList.add(pathList.get(i));

                            // 거리 메시지를 생성한다.
                            Log.d("distance", "약 " + Integer.toString((int) Math.round(distance(pointList.get(pointList.size() - 2).getLatitude(), pointList.get(pointList.size() - 2).getLongitude(), pointList.get(pointList.size() - 1).getLatitude(), pointList.get(pointList.size() - 1).getLongitude(), "meter"))) + "m 이동");

                            index++;

                            // 방향 메시지를 생성한다.
                            if((int) angle > 0)
                            {
                                Log.d("turning", "왼쪽 방향으로");
                            }
                            else
                            {
                                Log.d("turning", "오른쪽 방향으로");
                            }
                        }
                    }

                    // 도착지를 기록한다.
                    pointList.add(pathList.get(pathList.size() - 1));
//
//                    // 거리 메시지를 생성한다.
//                    Log.d("distance", "약 " + Integer.toString((int) Math.round(distance(pointList.get(pointList.size() - 2).getLatitude(), pointList.get(pointList.size() - 2).getLongitude(), pointList.get(pointList.size() - 1).getLatitude(), pointList.get(pointList.size() - 1).getLongitude(), "meter"))) + "m 이동");
//
////                    // 입력한 두 점 사이의 거리를 표현한다.
////                    Log.d("distance: ", "distance: " + Long.toString(Math.round(distance(pointList.get(5).getLatitude(), pointList.get(5).getLongitude(), pointList.get(6).getLatitude(), pointList.get(6).getLongitude(), "meter"))));
//
////                    // 모든 경로의 위도와 경도를 표현한다.
////                    Log.d("pathList: ", "pathList.size(): " + Double.toString(pathList.size()));
////                    for ( int i = 0; i < pathList.size(); i++) {
////                        Log.d("pathList: ", "pathList: " + Double.toString(pathList.get(i).getLatitude()) + ", " + Double.toString(pathList.get(i).getLongitude()));
////                    }
////
////                    //   출발지, 도착지, 중간 지점의 위도와 경도를 표현한다.
////                    Log.d("pointList: ", "pointList.size(): " + Double.toString(pointList.size()));
////                    for ( int i = 0; i < pointList.size(); i++) {
////                        Log.d("pointList: ", "pointList: " + Double.toString(pointList.get(i).getLatitude()) + ", " + Double.toString(pointList.get(i).getLongitude()));
////                    }

                } catch(java.io.IOException e) {
                    Log.e("java.io.IOException e: ", "java.io.IOException e");
                } catch(javax.xml.parsers.ParserConfigurationException e) {
                    Log.e("PCException: ", "javax.xml.parsers.ParserConfigurationException");
                } catch(org.xml.sax.SAXException e) {
                    Log.e("SAXException: ", "org.xml.sax.SAXException");
                }
            }
        }.start();
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}