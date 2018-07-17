package com.example.rltn4.navi_project;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.IOException;

import static android.Manifest.permission_group.CAMERA;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    android.hardware.Camera mcamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    final int MY_PERMISSION_REQUEST_CODE = 100;
    int APIVersion = Build.VERSION.SDK_INT;

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

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, 101);
            }
        });
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
