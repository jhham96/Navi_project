package com.example.rltn4.tnavi_project;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class TService extends Service implements LocationListener{
    private final IBinder mBinder = new LocalBinder();

    private static Context mContext;
    private static TMapView tMapView;
    private static ProgressBar proBar;
    private static Bitmap bitmap;
    private static TextView textView;
    private static ImageView arrow_img;
    private static boolean flag; // 거리가 점점 가까워지거나 멀어질 경우에 대해 true, false 를 갖는 변수이다.
    //    private int count;
    private static int pIndex;
    private static int mIndex;
    private static ArrayList<TMapPoint> pointList;
    private static ArrayList<String> messageList;
    private static int checkpoint_num; // 총 체크포인트 개수

    // 현재 액티비티 체크 maps = true, camera = false
    private static boolean activity_flag;

    // 현재 GPS 사용 유무
    private static boolean isGPSEnabled = false;

    // 네트워크 사용 유무
    private static boolean isNetworkEnabled = false;

    // Location 변수를 할당할 수 있는지 유무
    private static boolean isGetLocation = false;

    // 위도, 경도 정보를 담고 있는 변수이다.
    private static Location tlocation;
    private static double lat; // 위도
    private static double lon; // 경도

    // 최소 GPS 정보 업데이트 거리 4미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 4;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 5초
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;

    protected static LocationManager locationManager;

    class LocalBinder extends Binder {
        TService getService() {
            return TService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_SHORT).show();
        activity_flag = true;
        pIndex = 1;
        mIndex = 0;
        flag = true;
        mContext = getApplicationContext();
        getLocation();
    }

    @TargetApi(23)
    public Location getLocation() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_FINE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        try {
            // LocationManager를 할당한다.
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // GPS 상태 값을 할당한다.
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // 네트워크 상태 값을 할당한다.
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS 와 네트워크 사용이 가능하지 않을 때를 의미한다.

                Toast.makeText(mContext, "GPS 이용 불가", Toast.LENGTH_SHORT).show();
            } else {
                // tLocation 변수를 할당할 수 있음을 의미한다.
                this.isGetLocation = true;

                // 네트워크를 비활성화 한다.
                isNetworkEnabled = false;

                // 먼저 네트워크로부터 위도, 경도 정보를 가져온다.
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        tlocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (tlocation != null) {
                            // 위도 경도 저장
                            lat = tlocation.getLatitude();
                            lon = tlocation.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    // 네트워크로부터 위도, 경도 정보를 가져오지 못 했을 경우
                    // GPS로부터 위도, 경도 정보를 가져온다.
                    if (tlocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            tlocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (tlocation != null) {
                                lat = tlocation.getLatitude();
                                lon = tlocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tlocation;
    }

    // GPS를 종료한다.
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(TService.this);
        }
    }

    // 가장 최근의 위도 값을 반환한다.
    public double getLatitude(){
        if(tlocation != null){
            lat = tlocation.getLatitude();
        }
        return lat;
    }

    // 가장 최근의 경도 값을 반환한다.
    public double getLongitude(){
        if(tlocation != null){
            lon = tlocation.getLongitude();
        }
        return lon;
    }

    // isGetLocation 변수를 반환한다.
    public boolean isGetLocation() {
        return this.isGetLocation;
    }

//    // SettingAlert를 보여 준다.
//    public void showSettingsAlert(){
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//
//        alertDialog.setTitle("GPS Setting");
//        alertDialog.setMessage("GPS 설정이 안 되어 있는 것 같습니다. \n 설정을 확인하시겠습니까?");
//
//        // Setting을 누르게 되면 설정창으로 이동합니다.
//        alertDialog.setPositiveButton("Settings",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog,int which) {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        mContext.startActivity(intent);
//                    }
//                });
//        // Cancel 하면 종료 합니다.
//        alertDialog.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//        alertDialog.show();
//    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        tlocation = location;
        lat = tlocation.getLatitude();
        lon = tlocation.getLongitude();

        try {
            tMapView.removeMarkerItem("currentPoint"); // 기존 마커를 제거한다.
            TMapPoint currentPoint = new TMapPoint(location.getLatitude(), location.getLongitude());
            TMapMarkerItem currentMarker = new TMapMarkerItem();

            currentMarker.setIcon(bitmap); // 마커 아이콘 지정
            currentMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            currentMarker.setTMapPoint(currentPoint); // 마커의 좌표 지정
            tMapView.addMarkerItem("currentPoint", currentMarker); // 지도에 마커 추가

            if (pointList != null) {
                Log.d("pointList", "pointList is live");
                Log.d("messageList", "messageList is live");

                // 현재 위치와 터닝 포인트가 가까워질 때, message 갱신한다.
                if (distance(location.getLatitude(), location.getLongitude(), pointList.get(pIndex).getLatitude(), pointList.get(pIndex).getLongitude(), "meter") < 7 && flag) {
                    mIndex++;
                    flag = false;
                    proBar.setProgress(mIndex);

                    if (mIndex < messageList.size() && pIndex < pointList.size()) {
//                    mIndex++;
                        textView.setText(messageList.get(mIndex));
                        if(activity_flag == false)
                            changeArrow(arrow_img, textView);
                        TTS tts = new TTS(mContext, messageList.get(mIndex));
                    } else {
                        textView.setText("도착하였습니다.");
                        TTS tts = new TTS(mContext, "도착하였습니다");

                        // 도착 시, Dialog 나타나게 설정한다.
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("도착하였습니다.");
                        builder.setMessage("화면 종료하시겠습니까?");
                        builder.setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity) mContext).finish();
                                    }
                                });
                        builder.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        builder.show();
                    }
                }

                // 현재 위치와 터닝 포인트가 멀어질 때, message 갱신한다.
                if (distance(location.getLatitude(), location.getLongitude(), pointList.get(pIndex).getLatitude(), pointList.get(pIndex).getLongitude(), "meter") > 7 && !flag) {
//                mIndex++;
//                pIndex++;
                    flag = true;

                    if (mIndex < messageList.size() && pIndex < pointList.size()) {
                        mIndex++;
                        pIndex++;
                        textView.setText(messageList.get(mIndex));
                        if(activity_flag == false)
                            changeArrow(arrow_img,textView);
                    } else {
                        textView.setText("도착하였습니다.");
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("도착하였습니다.");
                        builder.setMessage("화면 종료하시겠습니까?");
                        builder.setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity) mContext).finish();
                                    }
                                });
                        builder.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        builder.show();
                    }
                }
            }
//        textView.setText("Count: " + Integer.toString(count));
//        count++;
//
//        // 길안내 메시지를 출력한다.
//        Log.d("message", messageList.get(0));
//
//        // pointList 크기를 출력한다.
//        Log.d("pointList.size()", Integer.toString(pointList.size()));

//        Toast.makeText(mContext, "currentPoint: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()) + "\n" + "provider: " + location.getProvider(), Toast.LENGTH_SHORT).show();
//        Log.d("currentpoint: ", Double.toString(getLatitude()) + ", " + Double.toString(getLongitude()));
        } catch (NullPointerException e) {
//            Toast.makeText(mContext, "GPS를 인식할 수 없습니다. 건물 밑에서 나와 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    // 마크를 갱신할 수 있도록 설정한다.
    public void setMarkerItem(TMapView t, Bitmap b) {
        tMapView = t;
        bitmap = b;
    }

    // TextView를 갱신할 수 있도록 설정한다.
    public void setText(TextView t) {
        textView = t;
    }

    public void setMessageList(ArrayList<String> a) {
        messageList = a;
    }

    public String getMessage() { return messageList.get(mIndex); }

    public void setPointList(ArrayList<TMapPoint> a) {
        pointList = a;
    }

    public void setProgressbar(ProgressBar percent_proBar){
        proBar = percent_proBar;
        checkpoint_num = messageList.size();
        proBar.setMax(checkpoint_num);}

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
    // 여기부터 카메라 액티비티에서 쓰는 거
    public static void setFlag(boolean flag){
        activity_flag = flag;
    }

    public static void setArrowImg(ImageView img){
        arrow_img = img;
    }

    void changeArrow(ImageView arrowView, TextView text_msg){
        String msg = (String) text_msg.getText();

        if(msg.indexOf("왼쪽")>=0){
            arrowView.setImageResource(R.drawable.back);
        }
        else if(msg.indexOf("오른쪽")>=0){
            arrowView.setImageResource(R.drawable.next);
        }
        else{
            arrowView.setImageResource(R.drawable.uparrow);
        }
    }

    public static ArrayList<TMapPoint> getPointList(){
        return pointList;
    }
}
