/*
 * TrackName.java
 * 
 * Created on Nov 1, 2007, 6:54:17 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the track name.
 *
 * @see TrackNameParser
 * @author Christine
 */
public interface TrackName extends MetaCommand{
    /**
     * Returns this track's name.
     * 
     * @return the track name
     */
    public String getName();
}
