/*
 * TimeSignature.java
 * 
 * Created on Nov 1, 2007, 7:23:30 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores the information associated with a time signature command.
 *
 * @see TimeSignatureParser
 * @author Christine
 */
public interface TimeSignature extends MetaCommand{
    /**
     * Returns the numerator of the time signature.  6 in 6/8 time, for example.
     * 
     * @return the numerator
     */
    public int getNumerator();
    
    /**
     * Returns the denominator of the time signature, where 2 is quarter, 3 is
     * eighth, etc.
     * 
     * @return the denominator
     */
    public int getDenominator();
    
    /**
     * Return the number of midi clocks in a metronome click.
     * 
     * @return ticks
     */
    public int getMetronomeClick();
    
    /**
     * Returns the "number of notated 32nd notes in a midi quarter note" (stolen
     * from <A href=http://www.borg.com/~jglatt/tech/midifile/time.htm>this</A> site).
     * 
     * @return 32nd notes in a midi quarter note
     */
    public int getMidiQuarterNote();
}
