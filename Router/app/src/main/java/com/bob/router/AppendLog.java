package com.bob.router; /**
 * Created by bob on 11/4/2016.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


public class AppendLog {
    private static final String     TAG = MainActivity.TAG;
    private static final String 	LOG_FILE_NAME = "log.txt";
    private String externalFilesDir;
    private Context context;
    private File logFile;

    public AppendLog(Context context, String externalFilesDir) {
        this.externalFilesDir = externalFilesDir;
        this.context = context;
        //		this will put the files on the sd card in this app's memory space
        //		these files will not remain when the app is uninstalled
        //		these files are on the logical sd card
//        String externalFilesDir = getExternalFilesDir(null).getPath();		//  KitKat: /sdcard/Android/data/com.bob.develop/files/

        logFile = new File(externalFilesDir, LOG_FILE_NAME);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, "failed to create log file: exception  " + e);
                Toast.makeText(context, "failed to create log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
            }
        }
    }


    public AppendLog(Context context, String externalFilesDir, String filename) {
        this.externalFilesDir = externalFilesDir;
        this.context = context;
        //		this will put the files on the sd card in this app's memory space
        //		these files will not remain when the app is uninstalled
        //		these files are on the logical sd card
//        String externalFilesDir = getExternalFilesDir(null).getPath();		//  KitKat: /sdcard/Android/data/com.bob.develop/files/

        Log.d(TAG, externalFilesDir + "   " + filename);
        logFile = new File(externalFilesDir, filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, "failed to create log file: exception  " + e);
                Toast.makeText(context, "failed to create log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void initLog(String text) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile));
            buf.write(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }

    public void appendLog(String text) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }

    public void appendLog(String[] returnResults) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            for (int j=0; j<returnResults.length; j++) {
                buf.append(returnResults[j]);
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }

    public void appendLog(Map<String, String> matches, String text) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            for (Map.Entry<String, String> couple : matches.entrySet()) {
                buf.append(couple.getKey() + text + couple.getValue());
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }


    public void appendLogX(Map<String, String> matches, String text) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            for (Map.Entry<String, String> couple : matches.entrySet()) {
                buf.append(couple.getKey() + text + couple.getValue());
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }

    public void appendLog(Map<String, List<String>> prefers) {
//        try {
            //BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            for (Map.Entry<String, List<String>> me : prefers.entrySet()) {
                String key = me.getKey();
                List<String> valueList = me.getValue();
                appendLog(key);
                appendLog(valueList);
            }
//            buf.newLine();
//            buf.close();
//        } catch (IOException e) {
//            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
//        }
    }

    public void appendLog(List<String> list) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(Arrays.toString(list.toArray()));
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Toast.makeText(context, "failed to write log file: exception   " + e + "\n", Toast.LENGTH_LONG).show();
        }
    }
}
