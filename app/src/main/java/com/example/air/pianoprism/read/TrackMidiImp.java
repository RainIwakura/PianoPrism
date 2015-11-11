/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.air.pianoprism.read;

/**
 * Implements a couple of the interfaces for midi messages in the context of
 * tracks and time.
 * @author Christine
 */
public class TrackMidiImp implements TrackMidi {

    int trackNumber;
    double seconds;
    long ticks;

    public TrackMidiImp(int trackNumber, double seconds, long ticks) {
        this.trackNumber = trackNumber;
        this.seconds = seconds;
        this.ticks = ticks;
    }

    @Override
    public int getTrackNumber() {
        return trackNumber;
    }

    @Override
    public double getSeconds() {
        return seconds;
    }

    @Override
    public long getTicks() {
        return ticks;
    }
}

