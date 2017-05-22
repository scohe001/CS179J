package com.example.aricohen.fridge;

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

        //TODO Fix this format to match Firebase
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
//        try {
//            inFridge = dateFormat.parse(inF);
//            if(!outF.equals("nope")) outFridge = dateFormat.parse(outF);
//            else outFridge = null;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }
}
