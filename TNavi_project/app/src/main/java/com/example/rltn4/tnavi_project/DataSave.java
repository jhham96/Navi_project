package com.example.rltn4.tnavi_project;

import com.skt.Tmap.TMapPOIItem;

import java.util.ArrayList;

public class DataSave {

    private String start;
    private String finish;

    // 데이터 저장할 변수 선언
    private TMapPOIItem tMapPOIItemsStart;
    private TMapPOIItem tMapPOIItemsFinish;

    public DataSave() {}
    public DataSave(String start, String finish) {
        this.start = start;
        this.finish = finish;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

//    public String getStart() {
//        return tMapPOIItemsStart.getPOIName();
//    }
    public String getStart() {
        return start;
    }
    public TMapPOIItem getStartObj() {
        return tMapPOIItemsStart;
    }

//    public String getFinish() {
//        return tMapPOIItemsFinish.getPOIName();
//    }
    public String getFinish() {
        return finish;
    }
    public TMapPOIItem getFinishObj() {
        return tMapPOIItemsFinish;
    }
}
