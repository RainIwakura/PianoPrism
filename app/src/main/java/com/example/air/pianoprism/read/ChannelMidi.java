/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

/**
 * Stores the channel information for midi commands.
 * 
 * @author Christine
 */
public interface ChannelMidi {

    /**
     * Gets the channel associated with this midi.
     * @return the channel (0x0-0xF)
     */
    public int getChannel();

}
