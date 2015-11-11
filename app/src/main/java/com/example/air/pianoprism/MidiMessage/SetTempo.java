/*
 * SetTempo.java
 * 
 * Created on Nov 1, 2007, 7:20:14 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with the set tempo command.
 *
 * @see SetTempoParser
 * @author Christine
 */
public interface SetTempo extends MetaCommand {
    /**
     * Returns the number of microseconds per quarter note in the new tempo.
     * 
     * @return the number of microseconds per quarter note.  Actually a three
     * byte value, so the maximum number is 2^24-1.
     */
    public int getMicrosecondsPerQuarterNote();
}
