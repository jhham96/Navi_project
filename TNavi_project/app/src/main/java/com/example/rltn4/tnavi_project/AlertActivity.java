package com.example.rltn4.tnavi_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class AlertActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("도착하였습니다.");
        builder.setMessage("화면 종료하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!MapsActivity._Maps_Activity.isFinishing()) {
                            (MapsActivity._Maps_Activity).finish();
                        }

                        if(CameraActivity._Camera_Activity != null) {
                            if(!CameraActivity._Camera_Activity.isFinishing()) {
                                (CameraActivity._Camera_Activity).finish();
                            }
                        }

                        finish();
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
