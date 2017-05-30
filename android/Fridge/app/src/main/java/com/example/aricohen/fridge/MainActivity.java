package com.example.aricohen.fridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.aricohen.fridge.FoodListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    public Context appContext;
    public HashMap<String, String> serials = new HashMap<String, String>();
    private FirebaseDatabase database;
    private FrameLayout mainframe;
    private ImageButton addButt;
    private ListView inFridgeList;
    private FoodListAdapter inFridgeAdapter;
    private ListView historyList;
    private FoodListAdapter historyAdapter;
    private MainActivity me;
    private int numInFridge;
    private String food_to_add;


    private AdapterView.OnItemClickListener onFoodClick
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //System.out.println("Tapped "+ inFridgeAdapter.getItem(i).toString());
            mainframe.removeAllViews();
            addButt.setVisibility(View.INVISIBLE);

            LayoutInflater.from(appContext).inflate(R.layout.food_info, mainframe, true);
            LinearLayout foodLayout = (LinearLayout) findViewById(R.id.food_info_layout);

            TextView foodTitle = (TextView) findViewById(R.id.food_info_title);

            if(((ArrayList<Food>)inFridgeAdapter.getItem(i)).get(0).serial.startsWith("(")) {
                foodTitle.setText(((ArrayList<Food>) inFridgeAdapter.getItem(i)).get(0).title);
            } else {
                foodTitle.setText(serials.get(((ArrayList<Food>) inFridgeAdapter.getItem(i)).get(0).serial));
            }

            for(Food f : (ArrayList<Food>)inFridgeAdapter.getItem(i)) {
                View v = LayoutInflater.from(appContext).inflate(R.layout.food_info_item, foodLayout, false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                TextView tv = (TextView) v.findViewById(R.id.food_info_date);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MM/dd/yy hh:mm a");
                tv.setText(sdf.format(f.inFridge));
                foodLayout.addView(v, params);
            }

            Button backButt = (Button) findViewById(R.id.food_info_back);
            backButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupFridge();
                }
            });
        }
    };

    //Called when add button is clicked
    private ImageButton.OnClickListener addClickListener
            = new ImageButton.OnClickListener() {

        public void onClick(View v) {
            food_to_add = null;

            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.add_perishable);

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button cancelButt = (Button) dialog.findViewById(R.id.add_perishable_cancel);
                    Button addButt = (Button) dialog.findViewById(R.id.add_perishable_add);

                    cancelButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    addButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(food_to_add != null) {
                                database.getReference("Functions/PutIn/PPP" + food_to_add).setValue(true);
                                dialog.dismiss();
                            }
                        }
                    });

                }
            });

            dialog.show();
        }
    };

    //Called when a radio button is clicked
    public void onRightRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        RadioGroup rightGroup = (RadioGroup) view.getParent();
        RadioGroup LeftGroup = (RadioGroup) ((TableRow) view.getParent().getParent()).getChildAt(0);

        if(checked) {
            LeftGroup.clearCheck();
            food_to_add = String.valueOf(((RadioButton) view).getText());
            Log.d("Check Buttons", "Got string of val " + food_to_add);
        }
    }

    public void onLeftRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        //setChecked

        RadioGroup rightGroup = (RadioGroup) ((TableRow) view.getParent().getParent()).getChildAt(1);
        RadioGroup LeftGroup = (RadioGroup) view.getParent();

        if(checked) {
            rightGroup.clearCheck();
            food_to_add = String.valueOf(((RadioButton) view).getText());
            Log.d("Check Buttons", "Got string of val " + food_to_add);
        }
    }

    //Called when dashboard tab is selected
    private void setupDashboard() {
        mainframe.removeAllViews();

        addButt.setVisibility(View.INVISIBLE);
        LayoutInflater.from(appContext).inflate(R.layout.dashboard, mainframe, true);
        database.getReference("message").setValue("Dashboard");

        TextView itemsIn = (TextView) findViewById(R.id.num_items_in);
        itemsIn.setText(String.valueOf(numInFridge));
    }

    //Called when history tab is selected
    private void setupHistory() {
        mainframe.removeAllViews();

        addButt.setVisibility(View.INVISIBLE);
        LayoutInflater.from(appContext).inflate(R.layout.history, mainframe, true);
        database.getReference("message").setValue("History");

        historyList = (ListView) findViewById(R.id.history_list);
        historyAdapter.setParent(historyList);
        historyList.setAdapter(historyAdapter);

        historyList.setOnItemClickListener(onFoodClick);
    }

    //Called when fridge tab is selected
    private void setupFridge() {
        mainframe.removeAllViews();

        addButt.setVisibility(View.VISIBLE);
        LayoutInflater.from(appContext).inflate(R.layout.fridge, mainframe, true);
        database.getReference("message").setValue("Fridge");

        inFridgeList = (ListView) findViewById(R.id.in_fridge_list);
        inFridgeAdapter.setParent(inFridgeList);
        inFridgeList.setAdapter(inFridgeAdapter);

        inFridgeList.setOnItemClickListener(onFoodClick);
    }

    //Called when new navigation option selected
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_history:
                    setupHistory();
                    return true;
                case R.id.navigation_dashboard:
                    setupDashboard();
                    return true;
                case R.id.navigation_fridge:
                    setupFridge();
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

        //A run once listener to get the names of all serials in the db
        database.getReference("Serials").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    serials.put(child.getKey(), child.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", databaseError.toException());
            }
        });

        // Attach listener to watch changes to what's in the fridge
        database.getReference("In Fridge").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                inFridgeAdapter.clear();

                numInFridge = 0;
                for(DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    if(foodSnapshot.getKey().equals("na")) continue;
                    numInFridge++;
                    String fSerial = foodSnapshot.child("Serial").getValue().toString();
                    if(fSerial.startsWith("PPP")) {
                        Log.d("DB", "Adding food named: " + fSerial.substring(3));
                        inFridgeAdapter.add(new Food("("+fSerial.substring(3)+")", fSerial.substring(3), foodSnapshot.child("inDate").getValue().toString()));
                    } else {
                        inFridgeAdapter.add(new Food(fSerial, serials.get(fSerial), foodSnapshot.child("inDate").getValue().toString()));
                    }

                    Log.d("DB", "Updating things...");
                }

                TextView itemsIn = (TextView) findViewById(R.id.num_items_in);
                //If dashboard tab is selected and visible...
                if(itemsIn != null) itemsIn.setText(String.valueOf(numInFridge));

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", error.toException());
            }
        });

        //Attach listener to watch changes to history of the fridge
        database.getReference("History").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                historyAdapter.clear();

                for(DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    if(foodSnapshot.getKey().equals("na")) continue;
                    String fSerial = foodSnapshot.child("Serial").getValue().toString();
                    if(fSerial.startsWith("PPP")) {
                        historyAdapter.add(new Food("("+fSerial.substring(3)+")", fSerial.substring(3), foodSnapshot.child("inDate").getValue().toString()));
                    } else {
                        historyAdapter.add(new Food(fSerial, serials.get(fSerial), foodSnapshot.child("inDate").getValue().toString()));
                    }

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

        //Lists aren't actually created since their tabs aren't selected, so pass null for now
        inFridgeAdapter = new FoodListAdapter(this, new ArrayList<ArrayList<Food>>(), null);
        historyAdapter = new FoodListAdapter(this, new ArrayList<ArrayList<Food>>(), null);

        //Start off with dashboard showing
        setupDashboard();
    }

}
