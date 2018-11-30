package com.example.rltn4.tnavi_project;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private Button change_btn;
    private ProgressBar percent_proBar;
    private TextView textView;

    private ArrayList<String> messageList;

    private TService tService; // 서비스 변수이다.
    private boolean isService = false; // 서비스 중인지 확인하는 변수이다.

    private boolean isCreate = false; // 해당 액티비티가 생성되었는지 확인하는 변수이다.

    private boolean compassMode = false; // compassMode를 위한 변수이다.

    public static Activity _Maps_Activity;

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            TService.LocalBinder mb = (TService.LocalBinder) service;
            tService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;

        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 해제",
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FrameLayout frameLayoutTmap = (FrameLayout)findViewById(R.id.frameLayoutTmap);

        _Maps_Activity = MapsActivity.this;

        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("f51c315c-f7e2-42f9-b290-0d2f818c07d7");
        tMapView.setCompassMode(false); // 단말의 방향에 따라 지도를 움직인다.
//        tMapView.setTrackingMode(true); // 화면 중심을 단말의 현재 위치로 이동시킨다.
        tMapView.setMarkerRotate(true); // 나침반 회전 시 Marker 이미지를 같이 회전시킨다.
        tMapView.setZoomLevel(18);
        frameLayoutTmap.addView(tMapView);

        tMapData = new TMapData();

        percent_proBar = (ProgressBar)findViewById(R.id.percent);
        percent_proBar.setIndeterminate(false);

        change_btn = (Button)findViewById(R.id.button1);
        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                tService.setFlag(false);
                startActivity(intent);
//                finish();
            }
        });

        final Button compassMode_btn = (Button) findViewById(R.id.compassMode_btn);
        compassMode_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!compassMode) {
                    compassMode = true;
                    tMapView.setCompassMode(compassMode);
                    compassMode_btn.setText("나침반" + "\n" +"ON");
                    // 화면 중심을 GPS 로 한다.
//                    tMapView.setCenterPoint(tService.getLongitude(), tService.getLatitude());
                } else {
                    compassMode = false;
                    tMapView.setCompassMode(compassMode);
                    compassMode_btn.setText("나침반"+"\n"+"OFF");
                }
            }
        });

        textView = (TextView) findViewById(R.id.textView);

//        gps = new GpsInfo(this);

        // ----------------------
//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//
//        alertDialog.setTitle("GPS Setting");
//        alertDialog.setMessage("GPS 설정이 안 되어 있는 것 같습니다. \n 설정을 확인하시겠습니까?");
//
//        // Setting을 누르게 되면 설정창으로 이동합니다.
//        alertDialog.setPositiveButton("Settings",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog,int which) {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        (MapsActivity.this).startActivity(intent);
//                    }
//                });
//        // Cancel 하면 종료 합니다.
//        alertDialog.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });

        // ----------------------

        // 서비스와 연결한다.
        Intent intent = new Intent(MapsActivity.this, TService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

//        Handler handler = new Handler(){
//            public void handleMessage(Message msg){
//                super.handleMessage(msg);

//            }
//        };
//        handler.sendEmptyMessageDelayed(0,2000); // 3초 딜레이

        if (!isPermission) {
            callPermission();
        }

//        showSettingsAlert();

        // 생성 시, Dialog 나타나게 설정한다.
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("길안내를 시작하겠습니다.");
        builder.setMessage("시작하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MapsActivity.this, "길안내를 시작합니다.", Toast.LENGTH_SHORT).show();

                        isCreate = true;

                        if (!isPermission) {
                            callPermission();
                        }

                        new Thread() {
                            public void run() {
                                try {
                                    // 인텐트로 전달 받은 것을 받아 선언한다.
                                    final ListViewItem listViewItem = (ListViewItem) getIntent().getSerializableExtra("Location");

                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false); // 마커 아이콘 사이즈 조정

//        if (gps.isGetLocation()) {
//
//            TMapPoint currentPoint = new TMapPoint(gps.getLatitude(), gps.getLongitude());
//            TMapMarkerItem currentMarker = new TMapMarkerItem();
//
//            currentMarker.setIcon(bitmap); // 마커 아이콘 지정
//            currentMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
//            currentMarker.setTMapPoint(currentPoint); // 마커의 좌표 지정
//            tMapView.addMarkerItem("currentPoint", currentMarker); // 지도에 마커 추가
//
//            gps.setMarkerItem(tMapView, bitmap); // 마크를 갱신할 수 있도록 설정한다.
//            gps.setText(textView); // TextView를 갱신할 수 있도록 설정한다.
//
//            Toast.makeText(getApplicationContext(), "GPS를 시작합니다.", Toast.LENGTH_SHORT).show();
//
//        } else {
//            // GPS 설정을 확인한다.
//            gps.showSettingsAlert();
//        }
//
//        // 화면 중심을 GPS 로 한다.
//        tMapView.setCenterPoint(gps.getLongitude(), gps.getLatitude());

                                    if (tService.isGetLocation()) {

                                        TMapPoint currentPoint = new TMapPoint(tService.getLatitude(), tService.getLongitude());
                                        TMapMarkerItem currentMarker = new TMapMarkerItem();

                                        currentMarker.setIcon(bitmap); // 마커 아이콘 지정
                                        currentMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                                        currentMarker.setTMapPoint(currentPoint); // 마커의 좌표 지정
                                        tMapView.addMarkerItem("currentPoint", currentMarker); // 지도에 마커 추가

                                        tService.setMarkerItem(tMapView, bitmap); // 마크를 갱신할 수 있도록 설정한다.
                                        tService.setText(textView); // TextView를 갱신할 수 있도록 설정한다.

                                        (MapsActivity.this).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MapsActivity.this, "GPS를 시작합니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        // GPS 설정을 확인한다.
//                        tService.showSettingsAlert();
                                        (MapsActivity.this).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                alertDialog.show();
                                                showSettingsAlert();
                                            }
                                        });
                                    }

                                    // 화면 중심을 GPS 로 한다.
                                    tMapView.setCenterPoint(tService.getLongitude(), tService.getLatitude());

//        Log.d("currentpoint: ", Double.toString(gps.getLatitude()) + ", " + Double.toString(gps.getLongitude()));

//                    ArrayList<TMapPOIItem> tMapPOIItemStartList = tMapData.findAllPOI("중앙대학교 병원"); // 출발지 검색을 위한 변수이다.
//                    ArrayList<TMapPOIItem> tMapPOIItemEndList = tMapData.findAllPOI("흑석역"); // 도착지 검색을 위한 변수이다.
//
//                    TMapPOIItem start = (TMapPOIItem) tMapPOIItemStartList.get(0); // 출발지를 나타내는 변수이다.
//                    TMapPOIItem end = (TMapPOIItem) tMapPOIItemEndList.get(0); // 도착지를 나타내는 변수이다.

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

                                    Log.d("name", listViewItem.gettMapBoxFinish().getName().toString());

                                    TMapPoint start = new TMapPoint(listViewItem.gettMapBoxStart().getLat(), listViewItem.gettMapBoxStart().getLon());
                                    TMapPoint end = new TMapPoint(listViewItem.gettMapBoxFinish().getLat(), listViewItem.gettMapBoxFinish().getLon());

                                    if(listViewItem.gettMapBoxFinish().getName().toString().equals("중앙대학교 약학대학R&D센터")) {
                                        end = new TMapPoint(37.506054, 126.958447);
                                    }

                                    // 지도 위에 선을 나타내는 구현이다.
//                    TMapPolyLine tMapPolyLine = tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start.getPOIPoint(), end.getPOIPoint());
                                    TMapPolyLine tMapPolyLine = tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end);
                                    tMapPolyLine.setLineColor(Color.BLUE);
                                    tMapPolyLine.setLineWidth(2);
//                    tMapView.setCenterPoint(start.getPOIPoint().getLongitude(), start.getPOIPoint().getLatitude());
                                    tMapView.setCenterPoint(start.getLongitude(), start.getLatitude());
                                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);

//                    ArrayList<TMapMarkerItem> pathMarkerList = new ArrayList<>(); // 모든 경로를 마커로 표시하고자 사용하는 변수이다.
                                    ArrayList<TMapMarkerItem> turningMarkerList = new ArrayList<>(); // 중간 지점을 마커로 표시하고자 사용하는 변수이다.
                                    int index = 0; // 중간 지점을 마커로 표시하고자 사용하는 인덱스 변수이다.

                                    ArrayList<TMapPoint> TMapPointList = tMapPolyLine.getLinePoint();
                                    ArrayList<TMapPoint> pathList = new ArrayList<>(); // 경로에서 중복을 제외한 위도, 경도를 나타내는 변수이다.
                                    ArrayList<TMapPoint> pointList = new ArrayList<>(); // 출발지, 도착지, 중간 지점의 위도, 경도를 나타내는 변수이다.
                                    messageList = new ArrayList<>(); // 길안내 메시지를 나타내는 변수이다.

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
                                            String distance = new String();
                                            distance = "약 " + Integer.toString((int) Math.round(distance(pointList.get(pointList.size() - 2).getLatitude(), pointList.get(pointList.size() - 2).getLongitude(), pointList.get(pointList.size() - 1).getLatitude(), pointList.get(pointList.size() - 1).getLongitude(), "meter"))) + "m 이동";
                                            messageList.add(distance);
//                            Log.d("distance", "약 " + Integer.toString((int) Math.round(distance(pointList.get(pointList.size() - 2).getLatitude(), pointList.get(pointList.size() - 2).getLongitude(), pointList.get(pointList.size() - 1).getLatitude(), pointList.get(pointList.size() - 1).getLongitude(), "meter"))) + "m 이동");

                                            index++;

                                            // 시 방향 메시지를 생성한다.
                                            String direction = new String();

                                            if((int) angle > 120) {
                                                direction = "7시";
                                            } else if((int) angle > 60) {
                                                direction = "9시";
                                            } else if((int) angle > 30) {
                                                direction = "11시";
                                            } else if((int) angle > -60) {
                                                direction = "1시";
                                            } else if((int) angle > -120) {
                                                direction = "3시";
                                            } else if((int) angle > -180) {
                                                direction = "5시";
                                            }

                                            // 방향 메시지를 생성한다.
                                            if((int) angle > 0)
                                            {
                                                direction = direction + " 방향 왼쪽으로";
//                                Log.d("turning", direction + " 방향 왼쪽으로");
                                            }
                                            else
                                            {
                                                direction = direction + " 방향 오른쪽으로";
//                                Log.d("turning", direction + " 방향 오른쪽으로");
                                            }

                                            messageList.add(direction);
                                        }
                                    }

                                    // 도착지를 기록한다.
                                    pointList.add(pathList.get(pathList.size() - 1));

                                    // 거리 메시지를 생성한다.
                                    String distance = new String();
                                    distance = "약 " + Integer.toString((int) Math.round(distance(pointList.get(pointList.size() - 2).getLatitude(), pointList.get(pointList.size() - 2).getLongitude(), pointList.get(pointList.size() - 1).getLatitude(), pointList.get(pointList.size() - 1).getLongitude(), "meter"))) + "m 이동";
                                    messageList.add(distance);

                                    // 길안내 메시지를 출력한다.
                                    for ( int i = 0; i < messageList.size(); i++) {
                                        Log.d("message", messageList.get(i));
                                    }

//                    // GPS 관련 정보를 설정한다.
//                    gps.setMessageList(messageList);
//                    gps.setPointList(pointList);
//                    gps.setProgressbar(percent_proBar);
//                    (MapsActivity.this).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            textView.setText(messageList.get(0));
//                            TTS tts = new TTS(MapsActivity.this, messageList.get(0));
//                        }
//                    });

                                    // GPS 관련 정보를 설정한다.
                                    tService.setMessageList(messageList);
                                    tService.setPointList(pointList);
                                    tService.setProgressbar(percent_proBar);
                                    (MapsActivity.this).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText(messageList.get(0));
                                            TTS tts = new TTS(MapsActivity.this, messageList.get(0));
                                        }
                                    });
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
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) MapsActivity.this).finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isCreate) {
            tService.setText(textView);
            tService.setProgressbar(percent_proBar);
            tService.setProgress();
            textView.setText(tService.getMessage());
        }
    }

    protected void onResume() {
        super.onResume();
        if(isCreate){
            tService.setFlag(true);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isService){
            unbindService(conn); // 서비스 종료
            isService = false;
        }
    }

     // SettingAlert를 보여 준다.
     private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("GPS Setting");
        alertDialog.setMessage("GPS 설정이 안 되어 있는 것 같습니다. \n 설정을 확인하시겠습니까?");

        // Setting을 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        (MapsActivity.this).startActivity(intent);
                    }
                });
        // Cancel 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
}
