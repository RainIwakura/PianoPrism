/*
 * CuePoint.java
 * 
 * Created on Nov 1, 2007, 7:04:55 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with a cue point meta command.
 *
 * @see CuePointParser
 * @author Christine
 */
public interface CuePoint extends MetaCommand{
    /**
     * Returns the cue point text.
     * 
     * @return the cue point text
     */
    public String getText();
}
