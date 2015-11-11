/*
 * TrackSequenceNumber.java
 * 
 * Created on Nov 1, 2007, 6:50:37 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with a track sequence number command.
 *
 * @see TrackSequenceNumberParser
 * @author Christine
 */
public interface TrackSequenceNumber extends MetaCommand{
    
    /**
     * Returns the track sequence number.
     * 
     * @return the track sequence number
     */
    public int getTrackSequenceNumber();
}
