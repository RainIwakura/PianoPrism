/*
 * NoteOn.java
 * 
 * Created on Nov 1, 2007, 6:46:54 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with a note on command.  A note on command
 * with velocity zero should be interpreted in the same manner as a note off 
 * command.
 *
 * @see NoteOff
 * @see NoteOnParser
 * @author Christine
 */
public interface NoteOn extends MidiCommandWithChannel{
    /**
     * Returns the note number associated with this note off command.  C4 is 60, 
     * C#4 is 61, etc.
     * 
     * @return the note number
     */
    public int getNoteNumber();

    /**
     * Returns the midi note velocity.
     *
     * @return the note velocity
     */
    public int getVelocity();
}
