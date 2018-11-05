package com.example.rltn4.tnavi_project;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

public class TTS implements TextToSpeech.OnInitListener {

    private TextToSpeech TTS;
    private String text;

    public TTS(Context mContext, String t) {
        TTS = new TextToSpeech(mContext, this);
        text = t;
    }

    public void onInit(int status) {
        TTS.speak(text, TextToSpeech.QUEUE_ADD, null);
    }
}
