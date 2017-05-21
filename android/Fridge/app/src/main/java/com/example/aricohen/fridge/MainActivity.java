package com.example.aricohen.fridge;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;

public class MainActivity extends AppCompatActivity {

    public Context appContext;
    private TextView mTextMessage;
    private FirebaseDatabase database;
    private FrameLayout mainframe;
    private View dashView;
    private View me;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {


            mainframe.removeAllViews();

            System.out.println("Clicked!");
            DatabaseReference myRef = database.getReference("message");
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    mTextMessage.setText(R.string.title_history);
                    myRef.setValue("Home");
                    return true;
                case R.id.navigation_dashboard:
                    LayoutInflater.from(appContext).inflate(R.layout.dashboard, mainframe, true);
                    mTextMessage.setText(R.string.title_dashboard);
                    myRef.setValue("Dashboard");
                    return true;
                case R.id.navigation_fridge:
                    mTextMessage.setText(R.string.title_fridge);
                    myRef.setValue("Notifications");
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();

        mainframe = (FrameLayout) findViewById(R.id.flayout);

        database = FirebaseDatabase.getInstance();

        // Read from the database
        database.getReference("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("DB", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DB", "Failed to read value.", error.toException());
            }
        });

        //dashView = (View) findViewById(R.layout.dashboard);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LayoutInflater.from(appContext).inflate(R.layout.dashboard, mainframe, true);
        mTextMessage = (TextView) findViewById(R.id.num_items_out);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        window.setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
    }

}
