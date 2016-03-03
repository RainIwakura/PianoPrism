package com.example.air.pianoprism;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import java.util.concurrent.*;

import static com.example.air.pianoprism.MatrixUtils.printArray;
/**
 * Created by rednecked_crake on 3/3/16.
 */
public class ProcessingTread extends Thread {

    double[] sample;
    boolean toRun = true;


    Handler processHandle = new Handler() {
        public void handleMessage(Message msg) {

            Bundle b = msg.getData();

            sample = b.getDoubleArray("data");

            System.out.println(printArray(sample));
            Log.d("Processing Thread: ", "hey");
        }
    };

    public Handler getHandle( ){
        return processHandle;
    }

    public void run() {
        Looper.prepare();

        Looper.loop();

    }

}
