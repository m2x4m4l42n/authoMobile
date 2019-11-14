package com.mva.authomobile.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UserLog {
    public static final int START = 1;
    public static final int CONTINUE = 2;
    public static final int STOP = 3;

    public void makeLog(int type, int userID, short stationID, long time){
        StringBuilder builder = new StringBuilder();
        builder.append(makeTime(time));
        builder.append("UserID: "+ userID + "\n");
        builder.append("StationID: " + stationID + "\n");
        switch(type){
            case START:
                builder.append("Loading Started");
                break;
            case CONTINUE:

                break;
            case STOP:
                break;
                default:
        }
    }
    public void makeLog(String text){

    }
    public void makeLog(){

    }
    private String makeTime(long millies){
        Date date = new Date(millies);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
