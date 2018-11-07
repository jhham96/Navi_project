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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission_group.CAMERA;
import static java.lang.StrictMath.abs;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private android.hardware.Camera mcamera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private final int MY_PERMISSION_REQUEST_CODE = 100;
    private int APIVersion = Build.VERSION.SDK_INT;
    private Button mode_switch_btn;
    private ProgressBar percent_proBar;

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

    private GpsInfo gps;

    private Location tlocation; // gps를 아직 못가져와서 넣어놈

    private TService tService; // 서비스 변수이다.
    private boolean isService = false; // 서비스 중인지 확인하는 변수이다.

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
        setContentView(R.layout.activity_camera);
        gps = (GpsInfo)getIntent().getSerializableExtra("gpsinfo");
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

        // 서비스와 연결한다.
        Intent intent = new Intent(CameraActivity.this, TService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        // 생성 시, Dialog 나타나게 설정한다.
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("카메라 모드를 시작하겠습니다.");
        builder.setMessage("주변을 잘 살펴보시길 바랍니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tService.setText(textView); // TextView를 갱신할 수 있도록 설정한다.
                        tService.setProgressbar(percent_proBar);
                        textView.setText(tService.getMessage());
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // textview 메세지에 따라 화살표 변경 추후에 메세지 변경하는 함수에 넣어서 호출
        changeArrow(arrow_img,textView);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        width = metrics.widthPixels;
        building_text = (TextView)findViewById(R.id.building_text);
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

                synchronized (this) {
                    if(isFirst<100) {
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            if(!accRunning){
                                accRunning=true;
                            }
                            mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                            mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                            mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
                        }
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                            mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                            mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                            mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
                        }

                        boolean success = SensorManager.getRotationMatrix(Rotation, I, mGravity, mGeomagnetic);

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
                        double gyroX = sensorEvent.values[0];
                        double gyroY = sensorEvent.values[1];
                        double gyroZ = sensorEvent.values[2];
                        double text = 0.0;

                        /* 단위시간 계산 */
                        dt = (sensorEvent.timestamp - timestamp) * NS2S;
                        timestamp = sensorEvent.timestamp;

                        /* 시간이 변화했으면 */
                        if (dt - timestamp * NS2S != 0) {
                            pitch = pitch + gyroY * dt;
                            /*
                            roll = roll + gyroX * dt;
                            yaw = yaw + gyroZ * dt;
                            */
                            if(pitch > 0){
                                text = -(360 + pitch * rad_to_dgr)%360;
                            }
                            else {
                                text = -(pitch * rad_to_dgr) % 360;
                            }
                            first_x.setText(String.format("%f",firstMagn));
                            handling_x = (text+firstMagn)%360; // handling_x = 핸드폰 들고 나침반 각도
                            pitch_text.setText(String.format("%f",handling_x));
                            if(handling_x>=20 && handling_x<=40) {
                                building_text.setText("건물있당!");
                                building_text.setX((float)(width-width*(handling_x-20)/20));
                            }
                            else if(handling_x >= 180 && handling_x <=200){
                                destination_img.setImageDrawable(getResources().getDrawable(R.drawable.flag));
                                destination_img.setX((float)(width-width*(handling_x-180)/20));
                            }
                            else{
                                building_text.setText("");
                            //    destination_img.setImageDrawable(getResources().getDrawable(R.drawable.blank));
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

    //목적지 팔로잉 각도
    public double destiny_angle(double latitude, double longitude){
        double my_latitude = 0.0;
        double my_longitude = 0.0; // gps info 에서 가져옴
        double standard_latitude, standard_longitude; // 가로, 세로

        standard_latitude = my_latitude;
        standard_longitude = longitude;

        double vector_Latitude = latitude - my_latitude;
        double vector_Longitude = longitude - my_longitude;

        double vector_standard_latitude = standard_latitude - my_latitude;
        double vector_standard_longitude = standard_longitude - my_longitude;

        // 각도가 얼마나 변했는지를 표현한다.
//                        Log.d("angle", Double.toString(Math.asin((vector1_Longitude * vector2_Latitude - vector1_Latitude * vector2_Longitude)
//                                /(Math.sqrt(Math.pow(vector1_Latitude, 2) + Math.pow(vector1_Longitude, 2)) * Math.sqrt(Math.pow(vector2_Latitude, 2) + Math.pow(vector2_Longitude, 2)))) * 57.2958));

        double angle = (Math.asin((vector_Longitude * vector_standard_latitude - vector_Latitude * vector_standard_longitude)
                /(Math.sqrt(Math.pow(vector_Latitude, 2) + Math.pow(vector_Longitude, 2)) * Math.sqrt(Math.pow(vector_standard_latitude, 2) + Math.pow(vector_standard_longitude, 2)))) * 57.2958);

        // 특정 각도가 넘는지를 확인한다.
        return Math.abs(angle);
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
        mAccRoll= Math.atan2(mGravity[1], mGravity[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);
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

    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(magnetic_Listener, myMagnetic, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(magnetic_Listener, myAccele,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(magnetic_Listener, myGyroscope,SensorManager.SENSOR_DELAY_UI);
    }
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(magnetic_Listener);
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
}
