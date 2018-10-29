package com.example.rltn4.tnavi_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skt.Tmap.TMapPOIItem;

import java.util.ArrayList;

public class ListViewAdapter2 extends BaseAdapter {
    private ArrayList<TMapPOIItem> item_list = new ArrayList<>();
    private final Context mContext;

    public ListViewAdapter2(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return item_list.size();
    }

    @Override
    public Object getItem(int position) {
        return item_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_search_result_item" layout을 inflate하여 convertView 참조 획득
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_search_result_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView textView = (TextView) convertView.findViewById(R.id.textView3);

        // Data Set(item_list)에서 position에 위치한 데이터 참조 획득
        TMapPOIItem item = item_list.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        textView.setText(item.getPOIName());

        return convertView;
    }

    // 아이템 추가
    public void addItem(TMapPOIItem item) {
        item_list.add(item);
    }
}
