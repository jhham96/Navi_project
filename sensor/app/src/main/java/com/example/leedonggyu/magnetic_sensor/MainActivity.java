package com.example.leedonggyu.magnetic_sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

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

    private TextView x_text;
    private TextView gyro_text;
    private TextView correct;

    private double roll;  // x
    private double pitch; // y
    private double yaw;   // z

    /* 단위 시간을 구하기 위한 변수 */
    private double timestamp = 0.0;
    private double dt;

    /* 회전각을 구하기 위한 변수 */
    private double rad_to_dgr = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    private int isFirst;
    private double firstMagn;
    private double handling_x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x_text = (TextView)findViewById(R.id.m_x);
        gyro_text = (TextView)findViewById(R.id.gyro_x);
        correct = (TextView)findViewById(R.id.correct);

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
                        x_text.setText("x" + String.format("%f", firstMagn));
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
                            handling_x = (text+firstMagn)%360;
                            gyro_text.setText("[pitch]" + String.format("%.1f", handling_x));
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(magnetic_Listener, myMagnetic,SensorManager.SENSOR_DELAY_UI);
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

}
