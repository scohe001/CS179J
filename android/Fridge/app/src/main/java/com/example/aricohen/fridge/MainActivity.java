package com.example.aricohen.fridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.ResourcesCompat;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private int numOutFridge;
    private String food_to_add;


    private AdapterView.OnItemClickListener onFridgeFoodClick
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

    private AdapterView.OnItemClickListener onHistoryFoodClick
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //System.out.println("Tapped "+ inFridgeAdapter.getItem(i).toString());
            mainframe.removeAllViews();
            addButt.setVisibility(View.INVISIBLE);

            LayoutInflater.from(appContext).inflate(R.layout.food_info, mainframe, true);
            LinearLayout foodLayout = (LinearLayout) findViewById(R.id.food_info_layout);

            TextView foodTitle = (TextView) findViewById(R.id.food_info_title);

            if(((ArrayList<Food>)historyAdapter.getItem(i)).get(0).serial.startsWith("(")) {
                foodTitle.setText(((ArrayList<Food>) historyAdapter.getItem(i)).get(0).title);
            } else {
                foodTitle.setText(serials.get(((ArrayList<Food>) historyAdapter.getItem(i)).get(0).serial));
            }

            for(Food f : (ArrayList<Food>)historyAdapter.getItem(i)) {
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
                    setupHistory();
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
        TextView itemsOut = (TextView) findViewById(R.id.num_items_out);
        itemsIn.setText(String.valueOf(numInFridge));
        itemsOut.setText(String.valueOf(numOutFridge));

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm a");
        TextView lastShop = (TextView) findViewById(R.id.last_shop);
        lastShop.setText(sdf.format(historyAdapter.getNewest()));

        //Calculate purchases from history...
        //for(historyAdapter.getCount()
        ArrayList<Integer> days = historyAdapter.getUsage();

        GraphView graph = (GraphView) findViewById(R.id.usage_graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                //new DataPoint(0, 1),
                new DataPoint(1, days.get(0)),
                new DataPoint(2, days.get(1)),
                new DataPoint(3, days.get(2)),
                new DataPoint(4, days.get(3)),
                new DataPoint(5, days.get(4)),
                new DataPoint(6, days.get(5)),
                new DataPoint(7, days.get(6)),
        });
        graph.addSeries(series);

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null);
            }
        });

        series.setSpacing(10);

        // draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);

        graph.getGridLabelRenderer().setNumHorizontalLabels(8);

        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);

        //set manual x axis limits to shows days of week
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(7.5);
        //Manually create lables for x axis
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show days of week instead of x vals
                    switch ((int) value) {
                        case 1: return "S";
                        case 2: return "M";
                        case 3: return "T";
                        case 4: return "W";
                        case 5: return "R";
                        case 6: return "F";
                        case 7: return "S";
                        default: return super.formatLabel(value, isValueX);
                    }
                } else {
                    // show normal y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });
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

        historyList.setOnItemClickListener(onHistoryFoodClick);
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

        inFridgeList.setOnItemClickListener(onFridgeFoodClick);
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
                if(itemsIn != null) setupDashboard();
                //if(itemsIn != null) itemsIn.setText(String.valueOf(numInFridge));

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", error.toException());
            }
        });

        // Attach listener to watch changes to what's out of the fridge
        database.getReference("Taken Out").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numOutFridge = 0;
                for(DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    if(foodSnapshot.getKey().equals("na")) continue;
                    numOutFridge++;
                }

//                TextView itemsOut = (TextView) findViewById(R.id.num_items_out);
//                //If dashboard tab is selected and visible...
//                setupDashboard();

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
        window.setStatusBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        window.setNavigationBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

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
