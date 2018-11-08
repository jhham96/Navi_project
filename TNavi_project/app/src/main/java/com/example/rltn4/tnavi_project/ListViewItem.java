package com.example.rltn4.tnavi_project;

import android.graphics.drawable.Drawable;
import com.skt.Tmap.TMapPOIItem;

import java.io.Serializable;

public class ListViewItem implements Serializable {
    private TMapBox tMapBoxStart;
    private TMapBox tMapBoxFinish;

    public ListViewItem(ListViewItem item) {
        this.tMapBoxStart = new TMapBox(item.gettMapBoxStart());
        this.tMapBoxFinish = new TMapBox(item.gettMapBoxFinish());
    }
    public ListViewItem() {

    }

    public TMapBox gettMapBoxStart() {
        return tMapBoxStart;
    }

    public TMapBox gettMapBoxFinish() {
        return tMapBoxFinish;
    }

    public void settMapBoxStart(TMapBox tMapBox) {
        this.tMapBoxStart = tMapBox;
    }

    public void settMapBoxFinish(TMapBox tMapBox) {
        this.tMapBoxFinish = tMapBox;
    }


}