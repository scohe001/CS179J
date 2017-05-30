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

        Log.d("DATE", "Parsing date "+inF);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        try {
            inFridge = dateFormat.parse(inF);
            //Convert Universal to PDT
            inFridge = new Date(inFridge.getTime() - 7 * 3600 * 1000);
            if(!outF.equals("nope")) {
                outFridge = dateFormat.parse(outF);
                outFridge = new Date(outFridge.getTime() - 7 * 3600 * 1000);
            } else outFridge = null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
