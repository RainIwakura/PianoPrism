package com.example.air.pianoprism;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.*;

import static com.example.air.pianoprism.MatrixUtils.fillNotes;
import static com.example.air.pianoprism.MatrixUtils.printArray;
import static com.example.air.pianoprism.MatrixUtils.zeroPad;
import static com.example.air.pianoprism.MatrixUtils.get_Mapping_Of_Notes_To_FFT_bins;

/**
 * Created by rednecked_crake on 3/3/16.
 */
public class ProcessingThread extends Thread {

    double[] sample;
    boolean toRun = true;
    private Handler updateUIHandler;
    Bundle containerForInfoToSend;


    private int bufferSize;
    int column_number; /// number of columns to display on screen


    /// CONTROL OF MAIN LOOP VARIABLE, MANIPULATED FROM MAIN ACTIVITY
    private volatile boolean keepRunning = true;
    // CONTROL ON WHETHER TO ZERO PAD OR NOT THE BUFFER
    boolean needZeropadding = false;
    /////////////////


    /// Class that provides FFT methods
    DoubleFFT_1D fft;

    // tempBuffer for final sound data storage and processing usage
    double[] processingBuffer;

    // variable that stores Notes with their information
    //  Freq
    //  Limits from left and right
    //  Name
    Note[] notes;

    // self explanatory
    int zeroPadTimes;

    // resolution value, i.e. frequency distance that one FFT bin covers, for example
    // res - 40 means that first bin in array returned by FFT covers frequency range from
    // 0 - 40 Hz
    double res;






    double[] final_result = new double[12];

    ////////////////////
    /// variables used for Chroma calculation
    // 1. stores indices of array returned by FFT for each Note
    // 2. Stores indices of array returned by FFT for each PITCH CLASS, such as C or C#
    //////////////////
    int[] mapping_Of_Notes_To_FFT_bins;
    HashSet[] pitchClassWithBins;
    ///////////////////


    short samplesToStore[][];
    int sampleNumber = 5;
    int sampleCounter = 0;





    public ProcessingThread(Handler updateUIHandler, int bufferSize, double res) {
        this.updateUIHandler = updateUIHandler;

        this.bufferSize = bufferSize;

        this.notes = fillNotes();
        this.res =res;

        fft = new DoubleFFT_1D(bufferSize);
        zeroPadTimes = 3;
        containerForInfoToSend = new Bundle();
        processingBuffer = new double[bufferSize];

        Arrays.fill(final_result, 0);


        mapping_Of_Notes_To_FFT_bins = get_Mapping_Of_Notes_To_FFT_bins(MatrixUtils.NoteFrequencies.values().length, bufferSize, res, notes);

        pitchClassWithBins = mapPitchClassToBins(this.mapping_Of_Notes_To_FFT_bins);


    }

    Handler processHandle = new Handler() {
        public void handleMessage(Message msg) {

            Bundle b = msg.getData();

            sample = b.getDoubleArray("data");
            int column = b.getInt("col");


            double[] result;
            if (needZeropadding) {
                result = zeroPad(sample, zeroPadTimes);
            } else
                result = sample;



            fft.realForward(result);




            for (int i = 0 ; i < result.length; i++) {
                result[i] = Math.abs(result[i]);
            }

            notesEnergy(result, final_result);


            double[] fresult = new double[final_result.length + 1];
            System.arraycopy(final_result,0, fresult,0, final_result.length);

            fresult[fresult.length - 1] = column;

            updateUIHandler.sendMessage(createBundleMsg(fresult));


        }
    };

    public Handler getHandle( ){
        return processHandle;
    }

    public void run() {
        Looper.prepare();

        Looper.loop();

    }




    public Message createBundleMsg(String strMsg) {
        containerForInfoToSend.putString("msg_s", strMsg);
        Message msg = updateUIHandler.obtainMessage();
        msg.setData(containerForInfoToSend);
        return msg;
    }

    public Message createBundleMsg(double[] arr) {
        containerForInfoToSend.putDoubleArray("data", arr);
        Message msg = updateUIHandler.obtainMessage();
        msg.setData(containerForInfoToSend);
        return msg;
    }


    public void notesEnergy(double[] buf, double[] final_result) {
        Arrays.fill(final_result, 0);

        for (int i = 0; i < 12; i++) {
            Object[] bins = pitchClassWithBins[i].toArray();
            for (int j = 0; j < bins.length; j++) {
                double temp = buf[(int) bins[j]];
                final_result[i] +=  temp*temp;
            }

        }

    }


    public HashSet<Integer>[] mapPitchClassToBins(int[] mappingOfNotes_To_FFT_bins) {
        HashSet<Integer>[] set = new HashSet[12];



        for (int i = 0; i < 12; i++) {
            set[i] = new HashSet<>();
            for (int j = 0;  j < 8; j++) {
                set[i].add(mappingOfNotes_To_FFT_bins[i + j*12]);
            }
        }




        return set;
    }


}
