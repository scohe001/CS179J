package com.example.aricohen.fridge;

import android.widget.BaseAdapter;

/**
 * Created by aricohen on 5/21/17.
 */

import java.util.ArrayList;
import java.util.HashMap;

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
    private ArrayList<Food> mDataSource;
    private static LayoutInflater inflater=null;
    private ListView parentView;

    public FoodListAdapter(Activity a, ArrayList<Food> d, ListView lv) {
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

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image

        Food song = mDataSource.get(position);

        title.setText(mDataSource.get(position).serial);
        // Setting all values in listview
        //title.setText(song.get(CustomizedListView.KEY_TITLE));
        //artist.setText(song.get(CustomizedListView.KEY_ARTIST));
        //duration.setText(song.get(CustomizedListView.KEY_DURATION));
        return vi;
    }

    public void setParent(ListView lv) {
        parentView = lv;
    }

    public void add(Food newFood) {
        mDataSource.add(newFood);
        parentView.invalidateViews();
    }
}
