/*
 * Marker.java
 * 
 * Created on Nov 1, 2007, 7:00:51 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores all the information associated with a Marker midi command.
 *
 * @see MarkerParser
 * @author Christine
 */
public interface Marker extends MetaCommand {
    /**
     * Returns the text in this command.
     * 
     * @return the command text.
     */
    public String getText();
}
