package com.example.rltn4.tnavi_project;

import android.graphics.drawable.Drawable;
import com.skt.Tmap.TMapPOIItem;

import java.io.Serializable;

public class ListViewItem implements Serializable {
    private Drawable iconDrawable ;
    private TMapBox tMapBoxStart;
    private TMapBox tMapBoxFinish;

    public ListViewItem(ListViewItem item) {
        this.iconDrawable = item.getIconDrawable();
        this.tMapBoxStart= item.gettMapBoxStart();
        this.tMapBoxFinish = item.gettMapBoxFinish();
    }
    public ListViewItem() {

    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public TMapBox gettMapBoxStart() {
        return tMapBoxStart;
    }

    public TMapBox gettMapBoxFinish() {
        return tMapBoxFinish;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public void settMapBoxStart(TMapBox tMapBox) {
        this.tMapBoxStart = tMapBox;
    }

    public void settMapBoxFinish(TMapBox tMapBox) {
        this.tMapBoxFinish = tMapBox;
    }


}