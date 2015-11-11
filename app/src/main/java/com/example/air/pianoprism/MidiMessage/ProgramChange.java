/*
 * ProgramChange.java
 * 
 * Created on Dec 2, 2007, 11:17:31 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with a program change command.
 *
 * @see ProgramChangeParser
 * @author Christine
 */
public interface ProgramChange extends MidiCommandWithChannel{
    /**
     * Returns the new program number for this program change command.
     * 
     * @return the new program number
     */
    public int getProgramNumber();
}
