package com.example.air.pianoprism;

import android.media.AudioRecord;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Air on 7/17/2015.
 */
public class UpdateUIThread extends Thread {
    AudioRecord recorder = null;
    private int bufferSize;
    String TAG = "UpdateUIThread";
    private Handler handle;
    private volatile boolean keepRunning = true;
    Visualizer vz;
    Bundle b;
    short[] buffer;
    DoubleFFT_1D fft;
    double[] bufInDouble;
    Note[] notes;

    int zeroPadTimes;
    double res;
    boolean needZeropadding = false;

    HashMap<String, Integer> note_energy;

    double[] result = new double[Note_freq.values().length];

    double[] final_result = new double[12];



    public enum noteEnum {
        C, CSHARP, D, Dsharp, E, F, Fsharp, G, Gsharp, A, Asharp, B;
    }
    int column_number;


    String[] noteNames = {"C", "Csharp", "D", "Dsharp", "E", "F", "Fsharp", "G", "Gsharp", "A", "Asharp", "B"};

    UpdateUIThread(AudioRecord recorder, int buffersize, Handler handle, double res, boolean needZeropadding,
                   int column_number) {
        this.recorder = recorder;
        this.bufferSize = buffersize;
        this.handle = handle;
        keepRunning = true;
        b =  new Bundle();
        buffer = new short[this.bufferSize];
        this.res = res;
        this.zeroPadTimes = 3;
        this.needZeropadding = needZeropadding;
        int fft_len = 0;
        this.column_number = column_number;

        if (needZeropadding)
            fft_len = bufferSize*zeroPadTimes + buffersize;
        else
            fft_len = buffersize;

        fft = new DoubleFFT_1D(fft_len);

        bufInDouble = new double[bufferSize];
        Note_freq[] vals = Note_freq.values();

        this.notes = fillNotes();
        for (int  i = 0; i < notes.length; i++) {
            Log.d("NOTES", notes[i].name + " Frequency:" + notes[i].freq + "  Left:" +  notes[i].left_bound + "  Right: " + notes[i].right_bound);
        }

        Log.d("Length: ", "" + result.length + " " + noteEnum.valueOf("C").ordinal());
        Arrays.fill(result, 0);
        Arrays.fill(final_result, 0);


        note_energy = new HashMap<String, Integer>();

        for (int i = 0; i < 12; i++) {
            note_energy.put(noteNames[i], i);
        }

    }


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


    public void run () {
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        ArrayDeque<Long> times = new ArrayDeque<>();

        Log.d(TAG, "run() -  SSSSSSSSSSSSSSTAAAAAAAAAAAAAAAAAAAAAAAAAAAAART");
        int j = 0;

        int column = 0;
        while (keepRunning)
        {
            //times.add(System.nanoTime());

           // Log.d("nanoTime start", ": " + System.nanoTime());

            Log.d("nanoTime start", "" + android.os.SystemClock.elapsedRealtime());

            //        Log.d("loop start", "0");
            j++;
   //         Log.d("loop", "id " + j);
            recorder.read(buffer, 0, this.bufferSize);


            column++;
            column %= column_number;

            new DoFftTask().execute(j, column);
   //         Log.d("loop end", "0");
         //   long endTime = System.nanoTime();

            Log.d("nanoTime end", "" + android.os.SystemClock.elapsedRealtime());


            //times.add(System.nanoTime());

        }
        Log.d("exit", "0");
    }


    public Message createBundleMsg(String strMsg) {
        b.putString("msg_s", strMsg);
        Message msg = handle.obtainMessage();
        msg.setData(b);
        return msg;
    }

    public Message createBundleMsg(double[] arr) {
        b.putDoubleArray("data", arr);
        Message msg = handle.obtainMessage();
        msg.setData(b);
        return msg;
    }
    public void setRunning(boolean running) {
        this.keepRunning = running;
    }

    public boolean getStatus() { return this.keepRunning; }


    public String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            sb.append(" ");
        }

        return sb.toString();
    }


    public Note[] fillNotes() {

        Note_freq[] vals = Note_freq.values();
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
        Arrays.fill(result, 0);
        Arrays.fill(final_result, 0);


        int j = 0;
        for (int i = 0; i < buf.length; i++) {

            while (notes[j].left_bound < (i+1)*res &&  notes[j].left_bound <= (i)*res)
            {
                if (notes[j].right_bound >= i * res)
                    result[j] += buf[i]*buf[i];
                if (j < notes.length - 1)
                    j++;
                else
                    break;

            }

        }

        for (int i = 0; i < result.length; i++) {

            String name = notes[i].name;
            name = name.substring(0, name.length()-1);

            final_result[note_energy.get(name)] +=result[i] ;
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

     //       Log.d("run()", "22222222.0");

      //      Log.d("run()", "id " + (Integer) params[0]);


            int column = (Integer) params[1];

            for (int i = 0; i < bufferSize; i++) {
                bufInDouble[i] = buffer[i];
            }
     //       Log.d("run()", "22222222");
            double[] result;
            if (needZeropadding) {
                result = zeroPad(bufInDouble, zeroPadTimes);
            } else
                result = bufInDouble;


            fft.realForward(result);
     //       Log.d("run()", "22222222 plus");

            for (int i = 0 ; i < result.length; i++) {
                result[i] = Math.abs(result[i]);
                // bufInDouble[i] = bufInDouble[i] + 22050;
            }
            notesEnergy(result);


            double[] fresult = new double[final_result.length + 1];
            System.arraycopy(final_result,0, fresult,0, final_result.length);

            fresult[fresult.length - 1] = column;

     //       Log.d("CHECK", arrayToString(final_result));
            //      Log.d("task - buffer", arrayToString(bufInDouble));

            handle.sendMessage(createBundleMsg(fresult));


            return "";
        }
    }





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



    public enum Note_freq {
        C0	(16.35),
        Csharp0 	(17.32),
        D0	(18.35),
        Dsharp0 (19.45),
        E0	(20.60),
        F0	(21.83),
        Fsharp0 (23.12),
        G0	(24.50),
        Gsharp0	(25.96),
        A0	(27.50),
        Asharp0 (29.14),
        B0	(30.87),
        C1	(32.70),
        Csharp1 (34.65),
        D1	(36.71),
        Dsharp1 (38.89),
        E1	(41.20),
        F1	(43.65),
        Fsharp1(46.25),
        G1	(49.00),
        Gsharp1	(51.91),
        A1	(55.00),
        Asharp1 	(58.27),
        B1	(61.74),
        C2	(65.41),
        Csharp2  	(69.30),
        D2	(73.42),
        Dsharp2(77.78),
        E2	(82.41),
        F2	(87.31),
        Fsharp2(92.50),
        G2	(98.00),
        Gsharp2(103.83),
        A2	(110.00),
        Asharp2(116.54),
        B2	(123.47),
        C3	(130.81),
        Csharp3(138.59),
        D3	(146.83),
        Dsharp3(155.56),
        E3	(164.81),
        F3	(174.61),
        Fsharp3(185.00),
        G3	(196.00),
        Gsharp3(207.65),
        A3	(220.00),
        Asharp3(233.08),
        B3	(246.94),
        C4	(261.63),
        Csharp4(277.18),
        D4	(293.66),
        Dsharp4(311.13),
        E4	(329.63),
        F4	(349.23),
        Fsharp4(369.99),
        G4	(392.00),
        Gsharp4(415.30),
        A4	(440.00),
        Asharp4(466.16),
        B4	(493.88),
        C5	(523.25),
        Csharp5(554.37),
        D5	(587.33),
        Dsharp5(622.25),
        E5	(659.25),
        F5	(698.46),
        Fsharp5(739.99),
        G5	(783.99),
        Gsharp5(830.61),
        A5	(880.00),
        Asharp5(932.33),
        B5	(987.77),
        C6	(1046.50),
        Csharp6(1108.73),
        D6	(1174.66),
        Dsharp6(1244.51),
        E6	(1318.51),
        F6	(1396.91),
        Fsharp6(1479.98),
        G6	(1567.98),
        Gsharp6 	(1661.22),
        A6	(1760.00),
        Asharp6 	(1864.66),
        B6	(1975.53),
        C7	(2093.00),
        Csharp7 	(2217.46),
        D7	(2349.32),
        Dsharp7 	(2489.02),
        E7	(2637.02),
        F7	(2793.83),
        Fsharp7 	(2959.96),
        G7	(3135.96),
        Gsharp7 	(3322.44),
        A7	(3520.00),
        Asharp7 	(3729.31),
        B7	(3951.07),
        C8	(4186.01),
        Csharp8	(4434.92),
        D8	(4698.63),
        Dsharp8	(4978.03),
        E8	(5274.04),
        F8	(5587.65),
        Fsharp8 	(5919.91),
        G8	(6271.93),
        Gsharp8 	(6644.88),
        A8	(7040.00),
        Asharp8 	(7458.62),
        B8	(7902.13);

        private final double freq;

        Note_freq(double freq) {
            this.freq =  freq;
        }


        private double getFreq() { return freq; }

    }

}
