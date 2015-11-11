/*
 * TempoRoll.java
 * 
 * Created on Nov 25, 2007, 5:16:50 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

/**
 * Stores all the tempo changes for a particular sequence in time order.  It's 
 * the same idea as {@link PianoRoll PianoRoll}, but for tempo changes.
 * 
 * @see SetTempoViewParser
 * @author Christine
 */
public interface SetTemposInMidi {
    /**
     * Returns the tempos in the order in which they were read.
     *
     * @return the tempos
     */
    public SetTempoInTrack[] getTempos();
    /**
     * Returns the tempos in an array format.
     * 
     * columns:
     * 0 - microseconds per quarter note
     * 1 - time in ticks
     * 2 - time in seconds
     * 3 - track number
     * 
     * @return tempo change information
     */
    public double[][] getTemposDoubles();
}
