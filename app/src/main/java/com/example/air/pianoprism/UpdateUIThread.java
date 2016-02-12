package com.example.air.pianoprism;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Almas on 7/17/2015.
 * Why JTransforms? Because it uses pure Java - no dependencies, no headache.
 * Note: code is quite bulky and convuluted, but I tried to make it as clear as possible
 * Comments divide code into sections for easier understanding
 */


public class UpdateUIThread extends Thread {
    /////////////////////////////////////
    /// variables needed for communication between this thread and main activity
    /////////////////////////////////////
    String TAG = "UpdateUIThread";
    Bundle containerForInfoToSend;
    private Handler handle;
    /////////////////////////////////////


    ///////////
    ////
    //////////
    AudioRecord recorder = null;
    private int bufferSize;
    int column_number; /// number of columns to display on screen


    /// CONTROL OF MAIN LOOP VARIABLE, MANIPULATED FROM MAIN ACTIVITY
    private volatile boolean keepRunning = true;
    // CONTROL ON WHETHER TO ZERO PAD OR NOT THE BUFFER
    boolean needZeropadding = false;
    /////////////////


    /// Class that provides FFT methods
    DoubleFFT_1D fft;

    // tempBuffer for preliminary sound data storage in shorts. Reason: easier to handle than bytes
    short[] tempBuffer;

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




    UpdateUIThread  (
                        AudioRecord recorder, int bufferSize,
                        Handler handle, double res,
                        boolean needZeropadding, int column_number
                    )
    {



        //////////////////////////////////////////////////
        //// Arguments passed from Main Activity
        ////////////////////////////////////////////////
        this.recorder = recorder;
        this.bufferSize = bufferSize;
        this.handle = handle;
        this.res = res;
        this.needZeropadding = needZeropadding;
        this.column_number = column_number;




        ////////////////////////////////////////////////
        // Variable instantiations
        ///////////////////////////////////////////////
        int fft_len = 0;
        keepRunning = true;
        containerForInfoToSend =  new Bundle();
        tempBuffer = new short[this.bufferSize];
        processingBuffer = new double[bufferSize];
        this.notes = fillNotes();
        Arrays.fill(final_result, 0);


        ///////////////////////////////////////////////////////////////
        //  FFT INITIALIZATION AND ZERO PADDING CODE
        //////////////////////////////////////////////////////////////
        this.zeroPadTimes = 3;

        if (needZeropadding)
            fft_len = bufferSize*zeroPadTimes + bufferSize;
        else
            fft_len = bufferSize;

        fft = new DoubleFFT_1D(fft_len); // class that performs FFT - library JTransforms

        //////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////
        ////////// Chroma calculation code instantiation
        /////////////////////////////////////////////////////////////////////////
        mapping_Of_Notes_To_FFT_bins = get_Mapping_Of_Notes_To_FFT_bins(NoteFrequencies.values().length, bufferSize, res, notes);

        pitchClassWithBins = mapPitchClassToBins(this.mapping_Of_Notes_To_FFT_bins);

        ////////////////////////////////////


         this.samplesToStore = new short[sampleNumber][bufferSize];


    }




    ///
    //      --------------------------MAIN LOOP - ALL THE REAL TIME PROCESSING IS BEING --------------------------
    //      --------------------------PERFORMED/CALLED FROM HERE                        --------------------------
    ///

    public void run () {
        Log.d(TAG, "run() -  START"); // inform developer that loop has just started

        int j = 0;
        int column = 0;

        while (keepRunning)
        {

            j++;
            recorder.read(this.tempBuffer, 0, this.bufferSize);

            ///////

            if (sampleCounter < sampleNumber) {
                samplesToStore[sampleCounter] = Arrays.copyOf(this.tempBuffer, this.bufferSize);
                sampleCounter++;
            }
            else {
                sampleCounter = 0;
                /// call onset detection

            }
            ////////////////////
            column++;
            column %= column_number;

            new DoFftTask().execute(j, column);

        }
        Log.d("exit", "0"); // inform Developer that loop has finished running
    }



    public Note[] fillNotes() {

        NoteFrequencies[] vals = NoteFrequencies.values();
        int length = vals.length;
        Note[] notes = new Note[length];


        for (int i = 0; i < length; i++) {
            if (i > 0) {
                double dist = Math.sqrt(notes[i-1].freq*vals[i].getFreq());
                notes[i] = new Note(vals[i].name(), vals[i].getFreq(),  dist, 2*vals[i].getFreq() - dist);
            } else {
                double dist = Math.abs(vals[i].getFreq() - Math.sqrt(vals[i + 1].getFreq()*vals[i].getFreq()));
                notes[i] = new Note(vals[i].name(), vals[i].getFreq(), vals[i].getFreq() - dist, vals[i].getFreq() + dist);
            }
        }


        return notes;
    }



    public void notesEnergy(double[] buf) {
        Arrays.fill(final_result, 0);

        for (int i = 0; i < 12; i++) {
            Object[] bins = pitchClassWithBins[i].toArray();
            for (int j = 0; j < bins.length; j++) {
                double temp = buf[(int) bins[j]];
                final_result[i] +=  temp*temp;
            }

        }

    }





    public String arrayToString(short[] arr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            sb.append(" ");
        }

        return sb.toString();
    }



    class DoFftTask extends AsyncTask {

        @Override
        public String doInBackground(Object ...params) {


            int column = (Integer) params[1];

            for (int i = 0; i < bufferSize; i++) {
                processingBuffer[i] = tempBuffer[i];
            }
            double[] result;
            if (needZeropadding) {
                result = zeroPad(processingBuffer, zeroPadTimes);
            } else
                result = processingBuffer;


            fft.realForward(result);




            for (int i = 0 ; i < result.length; i++) {
                result[i] = Math.abs(result[i]);
            }

            notesEnergy(result);


            double[] fresult = new double[final_result.length + 1];
            System.arraycopy(final_result,0, fresult,0, final_result.length);

            fresult[fresult.length - 1] = column;

            handle.sendMessage(createBundleMsg(fresult));


            return "";
        }
    }



    /*
    ///
        --------------------------METHOD THAT MAPS BINS IN BUFFER (i.e. [1] to C1) TO SPECIFIC
        --------------------------NOTE                                                        --------------------------
    ///
    */

    public int[] get_Mapping_Of_Notes_To_FFT_bins (int length, int buf_len, double res, Note[] notes) {
        int[] arr = new int[length];


        int j = 0;

        for (int i = 0; i < buf_len; i++) {

            while (notes[j].left_bound < (i+1)*res &&  notes[j].left_bound <= (i)*res)
            {
                if (notes[j].right_bound >= i * res)
                    arr[j] = i;
                if (j < notes.length - 1)
                    j++;
                else
                    break;

            }

        }



        return arr;
    }





    /*
    ///
        --------------------------METHOD THAT MAPS PITCH CLASSES TO ARRAY FILLED WITH FFT DATA         --------------------------
    ///
    */

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







    ///
    // NOTE RELATED DECLARATIONS
    ///

    public enum noteEnum {
        C, CSHARP, D, Dsharp, E, F, Fsharp, G, Gsharp, A, Asharp, B;
    }


    final String[] noteNames = {"C", "Csharp", "D", "Dsharp", "E", "F", "Fsharp", "G", "Gsharp", "A", "Asharp", "B"};



    /*
    ///
       --------------------------ENUMERATION STRUCTURE THAT STORES NAMES OF NOTES AND ASSOCIATED--------------------------
       --------------------------FREQUENCIES IN HZ                                              --------------------------
    ///
    */


    public enum NoteFrequencies {
        C0	(16.35), // 0
        Csharp0 	(17.32), //1
        D0	(18.35), // 2
        Dsharp0 (19.45), // 3
        E0	(20.60), // 4
        F0	(21.83), // 5
        Fsharp0 (23.12), // 6
        G0	(24.50), // 7
        Gsharp0	(25.96), //8
        A0	(27.50), // 9
        Asharp0 (29.14), // 10
        B0	(30.87), // 11
        C1	(32.70), // 12
        Csharp1 (34.65), // 13
        D1	(36.71), // 14
        Dsharp1 (38.89), // 15
        E1	(41.20), // 16
        F1	(43.65), // 17
        Fsharp1(46.25), // 18
        G1	(49.00), // 19
        Gsharp1	(51.91), // 20
        A1	(55.00), // 21
        Asharp1 	(58.27), // 22
        B1	(61.74), // 23
        C2	(65.41), // 24
        Csharp2  	(69.30), // 25
        D2	(73.42), // 26
        Dsharp2(77.78), // 27
        E2	(82.41), // 28
        F2	(87.31), // 29
        Fsharp2(92.50), // 30
        G2	(98.00), // 31
        Gsharp2(103.83), // 32
        A2	(110.00), // 33
        Asharp2(116.54), // 34
        B2	(123.47),  // 35
        C3	(130.81), // 36
        Csharp3(138.59), // 37
        D3	(146.83), // 38
        Dsharp3(155.56), // 39
        E3	(164.81), // 40
        F3	(174.61), // 41
        Fsharp3(185.00), // 42
        G3	(196.00), // 43
        Gsharp3(207.65), // 44
        A3	(220.00), // 45
        Asharp3(233.08), // 46
        B3	(246.94), // 47
        C4	(261.63), // 48
        Csharp4(277.18), // 49
        D4	(293.66), // 50
        Dsharp4(311.13), // 51
        E4	(329.63),  // 52
        F4	(349.23), // 53
        Fsharp4(369.99), // 54
        G4	(392.00), // 55
        Gsharp4(415.30), // 56
        A4	(440.00), // 57
        Asharp4(466.16), // 58
        B4	(493.88),  // 59
        C5	(523.25), // 60
        Csharp5(554.37), // 61
        D5	(587.33), // 62
        Dsharp5(622.25), // 63
        E5	(659.25), // 64
        F5	(698.46), // 65
        Fsharp5(739.99), // 66
        G5	(783.99),  // 67
        Gsharp5(830.61), // 68
        A5	(880.00), // 69
        Asharp5(932.33), // 70
        B5	(987.77), // 71
        C6	(1046.50), // 72
        Csharp6(1108.73), // 73
        D6	(1174.66), // 74
        Dsharp6(1244.51), // 75
        E6	(1318.51), // 76
        F6	(1396.91),  // 77
        Fsharp6(1479.98), // 78
        G6	(1567.98),  // 79
        Gsharp6 	(1661.22), // 80
        A6	(1760.00), // 81
        Asharp6 	(1864.66), // 82
        B6	(1975.53), // 83
        C7	(2093.00), // 84
        Csharp7 	(2217.46), // 85
        D7	(2349.32), // 86
        Dsharp7 	(2489.02), // 87
        E7	(2637.02), // 88
        F7	(2793.83), // 89
        Fsharp7 	(2959.96), // 90
        G7	(3135.96), // 91
        Gsharp7 	(3322.44), // 92
        A7	(3520.00),        // 93
        Asharp7 	(3729.31), // 94
        B7	(3951.07), // 95
        C8	(4186.01), // 96
        Csharp8	(4434.92), // 97
        D8	(4698.63), // 98
        Dsharp8	(4978.03), // 99
        E8	(5274.04), // 100
        F8	(5587.65), // 101
        Fsharp8 	(5919.91), // 102
        G8	(6271.93),        // 103
        Gsharp8 	(6644.88), // 104
        A8	(7040.00),         // 105
        Asharp8 	(7458.62), // 106
        B8	(7902.13);          // 107

        private final double freq;

        NoteFrequencies(double freq) {
            this.freq =  freq;
        }


        private double getFreq() { return freq; }

    }


    /*
    ///
       --------------------------DATA STRUCTURE THAT REPRESENTS NOTE AND ITS CHARACTERISTICS:--------------------------
       --------------------------                       1.NAME                               --------------------------
       --------------------------                       2.FREQUENCY (IN HZ)                  --------------------------
       --------------------------                       3. FREQ LEFT BOUND (IN HZ)           --------------------------
       --------------------------                       4. FREQ RIGHT BOUND (IN HZ)          --------------------------
    ///
    */

    public class Note {
        public final String name;

        public final double freq;

        public final double left_bound;
        public final double right_bound;

        public Note( String name , double freq, double left_bound, double right_bound) {
            this.name = name;
            this.freq = freq;
            this.left_bound = left_bound;
            this.right_bound = right_bound;
        }



    }




    /*
      *
      *
      *
      * -----------------------------HELPER METHODS------------------------------------------
      *
      *
      *
      *
    */


    /*
    ///
       --------------------------METHODS TO SEND PACKAGES OF INFO CALCULATED IN THIS THREAD TO--------------------------
       --------------------------MAIN ACTIVITY                                                --------------------------
    ///
    */

    public Message createBundleMsg(String strMsg) {
        containerForInfoToSend.putString("msg_s", strMsg);
        Message msg = handle.obtainMessage();
        msg.setData(containerForInfoToSend);
        return msg;
    }

    public Message createBundleMsg(double[] arr) {
        containerForInfoToSend.putDoubleArray("data", arr);
        Message msg = handle.obtainMessage();
        msg.setData(containerForInfoToSend);
        return msg;
    }


    /*
    ///
       --------------------------METHOD TO STOP OR RESTART MAIN LOOP FROM MAIN ACTIVITY       --------------------------
    ///
    */
    public void setRunning(boolean running) {
        this.keepRunning = running;
    }



    /*
    ///
        --------------------------METHOD TO CHECK WHETHER MAIN LOOP IS RUNNING OR NOT         --------------------------
    ///
    */

    public boolean getStatus() { return this.keepRunning; }



    /*
    ///
       --------------------------METHOD TO TURN 1D ARRAY CONTENTS INTO STRING                 --------------------------
    ///
    */
    public String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            sb.append(" ");
        }

        return sb.toString();
    }


    /*
    ///
       --------------------------METHOD TO ZERO PAD BUFFER OF SOUND PRESSURE AMPLITUDES       --------------------------
    ///
    */

    public double[] zeroPad(double[] input, int times) {

        double[] result = new double[input.length + input.length*times];

        for (int i = 0; i < result.length; i++) {
            if (i < input.length) {
                result[i] = input[i];
            } else
            {
                result[i] = 0;
            }
        }


        return result;
    }

}

