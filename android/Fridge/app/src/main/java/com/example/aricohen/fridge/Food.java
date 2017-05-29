package com.example.aricohen.fridge;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aricohen on 5/21/17.
 */

public class Food {
    public String title;
    public String serial;
    public Date inFridge = new Date();
    public Date outFridge = new Date();


    public Food(String serial, String title, String inFridge) {
        this(serial, title, inFridge, "nope");
    }

    public Food(String ser, String tit, String inF, String outF) {
        title = tit;
        serial = ser;

        Log.d("DATE", "Parsing date "+inF.substring(0, inF.length()-2));
        //TODO Fix this format to match Firebase
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
        try {
            inFridge = dateFormat.parse(inF.substring(0, inF.length()-2));
            if(!outF.equals("nope")) outFridge = dateFormat.parse(outF);
            else outFridge = null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
