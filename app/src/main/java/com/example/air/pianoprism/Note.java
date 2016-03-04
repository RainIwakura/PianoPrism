package com.example.air.pianoprism;

/**
 * Created by rednecked_crake on 3/4/16.
 */


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