/*
 * Note.java
 * 
 * Created on Nov 8, 2007, 11:09:58 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

/**
 * Stores the information associated with a midi note.
 * 
 * @author Christine
 */
public interface TimedNote extends TrackMidi, ChannelMidi{
    /**
     * Returns the midi number associated with the pitch of this note.
     * 
     * @return the note
     */
    public int getNote();
    
    /**
     * Returns the velocity of this note.
     * 
     * @return the velocity.
     */
    public int getVelocity();
    
    /**
     * Returns the tick number of the start of this note.  The tick is in 
     * fractions of a quarter note.
     * 
     * @see #getDurationTick()
     * @see javax.sound.midi.Sequence#getResolution()
     * @return the note start tick
     */
    public long getStartTick();
    
    /** 
     * Returns the number of ticks in the note.
     * 
     * @see #getStartTick()
     * @return the note duration tick
     */
    public long getDurationTick();
    
    /**
     * Returns the start time of the note in seconds.
     * 
     * @see #getDurationSeconds()
     * @return the note start time
     */
    public double getStartSeconds();
    
    /**
     * Returns the note duration in seconds.
     * 
     * @see #getStartSeconds()
     * @return the note duration in seconds
     */
    public double getDurationSeconds();
        
    /**
     * Returns the channel for this note.
     * 
     * @return the channel
     */
    @Override
    public int getChannel();
    
}
