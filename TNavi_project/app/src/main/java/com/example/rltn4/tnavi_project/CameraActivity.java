package com.example.rltn4.tnavi_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static android.Manifest.permission_group.CAMERA;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private android.hardware.Camera mcamera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private final int MY_PERMISSION_REQUEST_CODE = 100;
    private int APIVersion = Build.VERSION.SDK_INT;
    private Button mode_switch_btn;
    private ProgressBar percent_proBar;
    private ImageView arrow_img;

    private SensorManager mySensorManager; // 센서 매니저
    private SensorEventListener magnetic_Listener; // 센서 리스너
    private Sensor myMagnetic; // 센서
    private Sensor myAccele;
    private Sensor myGyroscope;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);

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

        percent_proBar = (ProgressBar)findViewById(R.id.percent);
        percent_proBar.setIndeterminate(false);
        percent_proBar.setMax(100);
        percent_proBar.setProgress(80);

        mode_switch_btn = (Button) findViewById(R.id.mode_switch);
        mode_switch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView textview = findViewById(R.id.text);
        textview.setText("우회전");
        textview.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
                builder.setTitle("AlertDialog Title");
                builder.setMessage("AlertDialog Content");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();
            }
        });

        // textview 메세지에 따라 화살표 변경 추후에 메세지 변경하는 함수에 넣어서 호출
        changeArrow(arrow_img,textview);

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
                                building_text.setX((float)(width*(handling_x-20)/20));
                            }
                            else{
                                building_text.setText("");
                            }
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    void changeArrow(ImageView arrowView, TextView text_msg){
        String msg = (String) text_msg.getText();

        if(msg.indexOf("좌회전")>=0){
            arrowView.setImageResource(R.drawable.back);
        }
        else if(msg.indexOf("우회전")>=0){
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
