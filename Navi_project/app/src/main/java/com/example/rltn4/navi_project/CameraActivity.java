package com.example.rltn4.navi_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import static android.Manifest.permission_group.CAMERA;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    final int MY_PERMISSION_REQUEST_CODE = 100;
    int APIVersion = Build.VERSION.SDK_INT;
    android.hardware.Camera mcamera;
    ProgressBar percent_proBar;
    ImageView arrow_img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);

        // 안드로이드 api 버전 높으면 접근권한 받기
        if (APIVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkCAMERAPermission()) {
                mcamera = android.hardware.Camera.open();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CODE);
            }
        }

        //surfaceview 생성 및 가로로 설정
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceCreated(surfaceHolder);

        // 이미지뷰 설정
        arrow_img = (ImageView)findViewById(R.id.arrow);

        //프로그래스바 생성 및 max, 현재 상황 표시
        percent_proBar = (ProgressBar)findViewById(R.id.percent);
        percent_proBar.setIndeterminate(false);
        percent_proBar.setMax(100);
        percent_proBar.setProgress(80);

        //모드 변경 버튼
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        //메시지로 안내
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

    //surface 및 카메라 권한 체크 함수들
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
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}