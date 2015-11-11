/*
 * TimedTempo.java
 * 
 * Created on Nov 25, 2007, 4:49:35 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.SetTempo;

/**
 * Stores the information associated with a tempo change.
 *
 * @author Christine
 */
public interface SetTempoInTrack extends TrackMidi,SetTempo{
    // nothing to do here...
}
