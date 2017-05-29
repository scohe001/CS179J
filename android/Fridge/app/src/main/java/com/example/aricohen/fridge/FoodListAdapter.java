package com.example.aricohen.fridge;

import android.widget.BaseAdapter;

/**
 * Created by aricohen on 5/21/17.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;




public class FoodListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<ArrayList<Food>> mDataSource;
    private static LayoutInflater inflater=null;
    private ListView parentView;

    public FoodListAdapter(Activity a, ArrayList<ArrayList<Food>> d, ListView lv) {
        activity = a;
        mDataSource=d;
        parentView = lv;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mDataSource.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.list_title); // title
        TextView serial = (TextView)vi.findViewById(R.id.list_serial); // artist name
        TextView inDate = (TextView)vi.findViewById(R.id.list_date); // duration

        ArrayList<Food> f = mDataSource.get(position);
        title.setText(f.get(0).title);
        serial.setText(f.get(0).serial);
        inDate.setText(f.get(0).inFridge.toString());

        return vi;
    }

    public void setParent(ListView lv) {
        parentView = lv;
    }

    public void add(Food newFood) {
        for(ArrayList<Food> fList : mDataSource) {
            if(fList.get(0).serial.equals(newFood.serial)) {
                fList.add(newFood);
                if(parentView != null) parentView.invalidateViews();
                return;
            }
        }

        //Add a new array and put  the new food in it
        mDataSource.add(new ArrayList<Food>());
        mDataSource.get(mDataSource.size() - 1).add(newFood);

        if(parentView != null) parentView.invalidateViews();
    }

    public void clear() {
        mDataSource.clear();
    }


}
