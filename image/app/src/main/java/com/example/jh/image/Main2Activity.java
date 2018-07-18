package com.example.jh.image;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Get editText, ImageButton Objects
        EditText editText1 = (EditText)findViewById(R.id.editText1);
        EditText editText2 = (EditText)findViewById(R.id.editText2);



        //
        String string1 = editText1.getText().toString();
        String string2 = editText2.getText().toString();


    }
}
