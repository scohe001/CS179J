package com.example.aricohen.fridge;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.aricohen.fridge.FoodListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    public Context appContext;
    private FirebaseDatabase database;
    private FrameLayout mainframe;
    private ImageButton addButt;
    private ListView inFridgeList;
    private FoodListAdapter inFridgeAdapter;
    private ListView historyList;
    private FoodListAdapter historyAdapter;
    private Activity me;

    //Called when add button is clicked
    private ImageButton.OnClickListener addClickListener
            = new ImageButton.OnClickListener() {

        public void onClick(View v) {
            inFridgeAdapter.add(new Food("Added!", "a", "2017-05-23T22:20:21.962Z"));
        }
    };

    //Called when new navigation option selected
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            mainframe.removeAllViews();

            DatabaseReference myRef = database.getReference("message");
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    addButt.setVisibility(View.INVISIBLE);
                    LayoutInflater.from(appContext).inflate(R.layout.history, mainframe, true);
                    myRef.setValue("History");

                    historyList = (ListView) findViewById(R.id.history_list);
                    historyAdapter.setParent(historyList);
                    historyList.setAdapter(historyAdapter);

                    return true;
                case R.id.navigation_dashboard:
                    addButt.setVisibility(View.INVISIBLE);
                    LayoutInflater.from(appContext).inflate(R.layout.dashboard, mainframe, true);
                    myRef.setValue("Dashboard");
                    return true;
                case R.id.navigation_fridge:
                    addButt.setVisibility(View.VISIBLE);
                    LayoutInflater.from(appContext).inflate(R.layout.fridge, mainframe, true);
                    myRef.setValue("Fridge");

                    inFridgeList = (ListView) findViewById(R.id.in_fridge_list);
                    inFridgeAdapter.setParent(inFridgeList);
                    inFridgeList.setAdapter(inFridgeAdapter);

                    inFridgeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            System.out.println("Clicked "+ String.valueOf(i));
                        }
                    });

                    return true;
            }
            return false;
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();

        mainframe = (FrameLayout) findViewById(R.id.flayout);

        database = FirebaseDatabase.getInstance();

        me = this;

        // Read from the database
        database.getReference("In Fridge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                inFridgeAdapter.clear();

                for(DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    if(foodSnapshot.getKey().equals("na")) continue;
                    String fSerial = foodSnapshot.child("Serial").getValue().toString();
                    inFridgeAdapter.add(new Food(fSerial, "name", foodSnapshot.child("inDate").getValue().toString()));

                    Log.d("DB", "Updating things...");
//                    if(!inFridgeSet.containsKey(fSerial)) {
//                        inFridgeSet.put(fSerial, new ArrayList<Food>());
//                    }
//                    inFridgeSet.get(fSerial).add(new Food(fSerial, "name", foodSnapshot.child("inDate").toString()));
//
//                    Log.d("DB", "Value is: " + foodSnapshot.child("Serial").toString());
                    //Log.d("DB", "Value is: " + foodSnapshot.toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", error.toException());
            }
        });

        database.getReference("History").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyAdapter.clear();

                for(DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    if(foodSnapshot.getKey().equals("na")) continue;
                    String fSerial = foodSnapshot.child("Serial").getValue().toString();
                    historyAdapter.add(new Food(fSerial, "name", foodSnapshot.child("inDate").getValue().toString()));

                    Log.d("DB", "Updating history...");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", databaseError.toException());
            }
        });

        //Setup the bottom navigation window
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Start off with dashboard showing
        LayoutInflater.from(appContext).inflate(R.layout.dashboard, mainframe, true);

        //Set colors for top and bottom bars to be fancy
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        window.setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        //Setup add button for fridge page
        ViewGroup vg = (ViewGroup)(getWindow().getDecorView().getRootView());
        LayoutInflater.from(appContext).inflate(R.layout.add_button, vg, true);

        addButt = (ImageButton) findViewById(R.id.testButt);
        addButt.setOnClickListener(addClickListener);
        addButt.setVisibility(View.INVISIBLE);

        inFridgeList = (ListView) findViewById(R.id.in_fridge_list);
        inFridgeAdapter = new FoodListAdapter(me, new ArrayList<ArrayList<Food>>(), inFridgeList);

        historyList = (ListView) findViewById(R.id.history_list);
        historyAdapter = new FoodListAdapter(me, new ArrayList<ArrayList<Food>>(), historyList);

    }

}
