package de.mva.basestation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Log {
    private static Log instance;
    private static Log getInstance(){
        if(instance == null)
            instance = new Log();
        return instance;
    }
    private PrintWriter log;

    private Log(){
        final File logFile = new File("log/" + createSessionKey() + ".txt");
        try{
            System.out.println(logFile.getParentFile().mkdirs());
            System.out.println(logFile.createNewFile());
        }catch (IOException e){
            e.printStackTrace();
        }
        try{
            log = new PrintWriter(logFile);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void print(String s){
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(currentTimeMillis);
        StringBuilder builder = new StringBuilder();
        builder.append(resultdate);
        builder.append(" --- ");
        builder.append(s);
        String res = builder.toString();
        System.out.println("Logged :" + res);

        Log.getInstance().log.println(res);
        Log.getInstance().log.flush();
    }


    private String createSessionKey(){
        return UUID.randomUUID().toString();
    }
}
