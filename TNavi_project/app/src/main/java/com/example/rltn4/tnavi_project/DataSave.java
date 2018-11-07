package com.example.rltn4.tnavi_project;

public class DataSave {

    private String start;
    private String finish;

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

    public String getStart() {
        return start;
    }

    public String getFinish() {
        return finish;
    }
}
