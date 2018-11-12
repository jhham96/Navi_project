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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission_group.CAMERA;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.min;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private android.hardware.Camera mcamera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private final int MY_PERMISSION_REQUEST_CODE = 100;
    private int APIVersion = Build.VERSION.SDK_INT;
    private Button mode_switch_btn;
    private ProgressBar percent_proBar;

    private boolean isCreate;

    private ImageView arrow_img;
    private ImageView destination_img;

    private SensorManager mySensorManager; // 센서 매니저
    private SensorEventListener magnetic_Listener; // 센서 리스너
    private Sensor myMagnetic; // 센서
    private Sensor myAccele;
    private Sensor myGyroscope;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] mGyroValues = new float[3];
    private float[] Rotation = new float[9];
    private float[] I = new float[9];

    private float azimuth;
    private double pitch; // y

    private TextView building_text;
    TextView first_x;
    TextView pitch_text;
    /* 단위 시간을 구하기 위한 변수 */
    private double timestamp = 0.0;
    private double dt;

    /* 회전각을 구하기 위한 변수 */
    private double rad_to_dgr = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    private int isFirst;
    private double firstMagn;
    private double handling_x;

    int width;

    private boolean gyroRunning;
    private boolean accRunning;
    private double mAccPitch, mAccRoll;
    private double temp;
    private float a = 0.2f;

    private double dest_degree = 0.0;

    private float mLowPassY = 0;
    private float mHighPassY = 0;
    private float mLastY = 0;

    private TService tService; // 서비스 변수이다.
    private boolean isService = false; // 서비스 중인지 확인하는 변수이다.

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("LocationData");
    // 읽어온 데이터 저장할 리스트 변수 선언
    private ArrayList<TMapBox> list = new ArrayList<>();

    public static Activity _Camera_Activity;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_camera);

        _Camera_Activity = CameraActivity.this;

        if (APIVersion >= android.os.Build.VERSION_CODES.M){
            if(checkCAMERAPermission()){
                mcamera = android.hardware.Camera.open();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CODE);
            }
        }

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        surfaceCreated(surfaceHolder);

        arrow_img = (ImageView)findViewById(R.id.arrow);
        destination_img = (ImageView)findViewById(R.id.destination);

        percent_proBar = (ProgressBar)findViewById(R.id.percent);
        percent_proBar.setIndeterminate(false);
        percent_proBar.setMax(100);

        mode_switch_btn = (Button) findViewById(R.id.mode_switch);
        mode_switch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
         //       intent.putExtra("gpsinfo",gps);
//                startActivity(intent);
                finish();
            }
        });

        final TextView textView = findViewById(R.id.text);

        // 데이터 읽어오기
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TMapBox tMapBox = dataSnapshot.getValue(TMapBox.class);
                list.add(tMapBox);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // 서비스와 연결한다.
        Intent intent = new Intent(CameraActivity.this, TService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        building_text = (TextView)findViewById(R.id.building_text);

        // 생성 시, Dialog 나타나게 설정한다.
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("카메라 모드를 시작하겠습니다.");
        builder.setMessage("주변을 잘 살펴보시길 바랍니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isCreate = true;
                        tService.setText(textView); // TextView를 갱신할 수 있도록 설정한다.
                        tService.setProgressbar(percent_proBar);
                        tService.setProgress();
                        textView.setText(tService.getMessage());
                        tService.setArrowImg(arrow_img);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        first_x = (TextView)findViewById(R.id.first_x);
        pitch_text = (TextView)findViewById(R.id.pitch);

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 자이로스코프 센서를 사용하겠다고 등록
        myMagnetic = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        myAccele = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        isFirst =0;
        magnetic_Listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                final float alpha = 0.97f;
                int nearest_building_index = 0;
                double building_degree = 0.0;

                synchronized (this) {
                    if(isFirst<100) {
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            if(!accRunning){
                                accRunning=true;
                            }
                            mGravity[0] = sensorEvent.values[0];
                            mGravity[1] = sensorEvent.values[1];
                            mGravity[2] = sensorEvent.values[2];
                        }
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                            mGeomagnetic[0] = sensorEvent.values[0];
                            mGeomagnetic[1] = sensorEvent.values[1];
                            mGeomagnetic[2] = sensorEvent.values[2];
                        }

                        boolean success = SensorManager.getRotationMatrix(Rotation, null, mGravity, mGeomagnetic);

                        if (success) {
                            float orientaion[] = new float[3];
                            SensorManager.getOrientation(Rotation, orientaion);
                            azimuth = (float) Math.toDegrees(orientaion[0]);
                            azimuth = (azimuth + 360) % 360;
                        }
                        firstMagn = azimuth;
                        isFirst++;
                    }
                    if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        mGyroValues = sensorEvent.values;
                        if(!gyroRunning){
                            gyroRunning=true;
                        }
                        double gyroY = sensorEvent.values[1];
                        double text = 0.0;

                        mLowPassY = lowPass((float)gyroY,mLowPassY);
                        /* 하이패스 필터*/
                        mHighPassY = highPass(mLowPassY,mLastY,mHighPassY);
                        mLastY = mLowPassY;

                        /* 단위시간 계산 */
                        dt = (sensorEvent.timestamp - timestamp) * NS2S;
                        timestamp = sensorEvent.timestamp;

                        /* 시간이 변화했으면 */
                        if (dt - timestamp * NS2S != 0) {
                            pitch = pitch + gyroY * dt;
                            text = -(pitch * rad_to_dgr) % 360; // 자이로는 반시계로 돌릴 때 값이 양수로 증가, 시계는 음수로 증가, 나침반이라 반대라서 부호 바꿔줌

                            first_x.setText(String.format("%f",firstMagn));

                            handling_x = text+firstMagn;
                            if(handling_x < 0){
                                handling_x = 360 + handling_x;
                            }
                            handling_x = handling_x%360; // handling_x = 핸드폰 들고 나침반 각도
                            pitch_text.setText(String.format("%f",handling_x));

                            ArrayList<TMapPoint> pointList = tService.getPointList();
                            if(isCreate) {
                                dest_degree = destiny_angle(tService.getPointList().get(tService.getPointList().size() - 1).getLatitude(), tService.getPointList().get(pointList.size() - 1).getLongitude());
                                nearest_building_index = nearest_building();
                                if(nearest_building_index >= 0) {
                                    building_degree = destiny_angle(list.get(nearest_building_index).getLat(), list.get(nearest_building_index).getLon());

                                /* 건물정보 출력 */
                                    if (building_degree >= 10 && building_degree < 350 && handling_x >= (building_degree - 10.0) && handling_x <= (building_degree + 10.0)) { // 목적지가 10~350
                                        building_text.setText(list.get(nearest_building_index).getName());
                                        building_text.setX((float) (width - width * (handling_x - (building_degree - 10.0)) / 20.0));
                                    } else if (building_degree < 10.0 && (handling_x < (building_degree + 10.0) || handling_x > (360 - building_degree))) { // 0~10
                                        building_text.setText(list.get(nearest_building_index).getName());
                                        if (handling_x < 350) {
                                            building_text.setX((float) (width - (width * (handling_x - (building_degree - 10.0)) / 20.0)));
                                        } else {
                                            building_text.setX((float) (width - (width * (handling_x - (360 - building_degree)) / 20.0)));
                                        }
                                    } else if (building_degree >= 350.0 && ((handling_x >= (building_degree - 10.0) || handling_x < (10.0 + building_degree) % 360))) { // 350~360
                                        building_text.setText(list.get(nearest_building_index).getName());
                                        if (handling_x >= (building_degree - 10.0)) {
                                            building_text.setX((float) (width - width * (handling_x - (building_degree - 10.0)) / 20.0));
                                        } else {
                                            building_text.setX((float) (width * (((10.0 + building_degree) % 360 - handling_x)) / 20.0));
                                        }
                                    } else {
                                        building_text.setText("");
                                    }
                                }

                                /* 목적지 팔로잉 */
                                if (dest_degree >= 10 && dest_degree < 350 && handling_x >= (dest_degree - 10.0) && handling_x <= (dest_degree + 10.0)) { // 목적지가 10~350
                                    destination_img.setImageDrawable(getResources().getDrawable(R.drawable.flag));
                                    destination_img.setX((float) (width - width * (handling_x - (dest_degree - 10.0)) / 20.0));
                                }
                                else if (dest_degree < 10.0 && (handling_x < (dest_degree +10.0) || handling_x > ( 360 - dest_degree))){ // 0~10
                                    destination_img.setImageDrawable(getResources().getDrawable(R.drawable.flag));
                                    if(handling_x < 350){
                                        destination_img.setX((float) (width - (width * (handling_x - (dest_degree - 10.0)) / 20.0)));
                                    }else{
                                        destination_img.setX((float) (width - (width * (handling_x - (360-dest_degree)) / 20.0)));
                                    }
                                }
                                else if(dest_degree >= 350.0 && ((handling_x >= (dest_degree - 10.0) || handling_x < (10.0+ dest_degree)%360))){ // 350~360
                                    destination_img.setImageDrawable(getResources().getDrawable(R.drawable.flag));
                                    if(handling_x >= (dest_degree - 10.0)){
                                        destination_img.setX((float) (width - width * (handling_x - (dest_degree - 10.0)) / 20.0));
                                    }else{
                                        destination_img.setX((float) (width * (((10.0 + dest_degree)%360-handling_x)) / 20.0));
                                    }
                                }
                                else {
                                    destination_img.setImageDrawable(getResources().getDrawable(R.drawable.blank));
                                }
                            }
                        }
                    }
                }
                if(gyroRunning&&accRunning){
                    complementary(sensorEvent.timestamp);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    public int nearest_building(){
        int index = 0;
        double min_dist = 100000;
        double currnet_dist = 0;

        for(int i=0; i<list.size(); i++) {
            currnet_dist = distance(list.get(i).getLat(),list.get(i).getLon(),tService.getLatitude(),tService.getLongitude(),"meter");
            Log.d("degree","min_dist : "+String.format("%s %f",list.get(index).getName(),min_dist));
            Log.d("degree","current_dist : "+String.format("%s %f",list.get(i).getName(),currnet_dist));
            if( min_dist > currnet_dist ){
                index = i;
                min_dist = currnet_dist;
            }
        }
//        if(min_dist > 7){
//            index = -1;
//        }

        return index;
    }

    public double destiny_angle(double dest_latitude, double dest_longitude){
        double my_latitude = tService.getLatitude();
        double my_longitude = tService.getLongitude(); // gps info 에서 가져옴
        double standard_latitude, standard_longitude; // 가로선, 세로선

        standard_latitude = my_latitude;
        standard_longitude = dest_longitude;

        double vector_Latitude = dest_latitude - my_latitude;
        double vector_Longitude = dest_longitude - my_longitude;

        double vector_standard_latitude = standard_latitude - my_latitude;
        double vector_standard_longitude = standard_longitude - my_longitude;

        double angle = (Math.asin((vector_Longitude * vector_standard_latitude - vector_Latitude * vector_standard_longitude)
                /(Math.sqrt(Math.pow(vector_Latitude, 2) + Math.pow(vector_Longitude, 2)) * Math.sqrt(Math.pow(vector_standard_latitude, 2) + Math.pow(vector_standard_longitude, 2)))) * 57.2958);

        if( my_latitude < dest_latitude && my_longitude < dest_longitude ){ // 1사분면

        }
        else if( my_latitude < dest_latitude && my_longitude > dest_longitude){ // 2사분면
            angle = 270 + angle;
        }
        else if( my_latitude > dest_latitude && my_longitude > dest_longitude){ // 3사분면
            angle = 270 - angle;
        }
        else{ // 4사분면
            angle = 90 + angle;
        }
        // 특정 각도가 넘는지를 확인한다.
        return Math.abs(angle);
    }

    float lowPass(float current, float last){
        return (float)(last*(1.0f-0.1)+current*0.1);
    }

    float highPass(float current, float last, float filtered){
        return (float)(0.1*(filtered+current-last));
    }
    private void complementary(double new_ts){
        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mGravity[0], mGravity[2]) * 180.0 / Math.PI; // Y 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);
    }

    protected void onResume() {
        super.onResume();
        if(isCreate)
            tService.setFlag(false);
        mySensorManager.registerListener(magnetic_Listener, myMagnetic, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(magnetic_Listener, myAccele,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(magnetic_Listener, myGyroscope,SensorManager.SENSOR_DELAY_UI);
    }
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(magnetic_Listener);
    }

    protected void onDestroy(){
        super.onDestroy();
        tService.setFlag(true);
    }
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mcamera.setDisplayOrientation(90);
            }
            else{
                mcamera.setDisplayOrientation(0);
            }

            mcamera.setPreviewDisplay(surfaceHolder);
            mcamera.startPreview();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private boolean checkCAMERAPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    if (cameraAccepted) {
                        mcamera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessagePermission("권한허가를 요청합니다!",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA}, MY_PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessagePermission(String message, DialogInterface.OnClickListener okListener){
        new android.support.v7.app.AlertDialog.Builder(this).setMessage(message).setPositiveButton("허용",okListener).setNegativeButton("거부",null).create().show();
    }

    public static String DecodeString(String string) {
        string = string.replace("{", "[");
        string = string.replace("}", "]");
        return string;
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
