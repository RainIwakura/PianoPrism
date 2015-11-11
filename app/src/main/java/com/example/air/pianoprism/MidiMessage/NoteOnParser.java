/*
 * NoteOnParser.java
 *
 * Created on Nov 7, 2007, 11:26:33 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a note on command.
 *
 * @see NoteOn
 * @author Christine
 */
public class NoteOnParser extends MidiCommandWithChannelParser {

    public NoteOnParser() {
        // nothing to do here
    }

    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();

        byte upper = (byte) (message[0]&0xF0);

        if (upper == RawMidiMessageParser.NOTE_ON) {
            return new Command(message, message[1], message[2]);
        } else {
            return null;
        }
    }

    private static class Command extends MidiCommandWithChannelParser.Command implements NoteOn {

        int noteNumber = 0;
        int velocity = 0;

        public Command(byte[] message, int noteNumber, int velocity) {
            super(message);
            this.noteNumber = noteNumber;
            this.velocity = velocity;
        }

        public int getNoteNumber() {
            return noteNumber;
        }

        public int getVelocity() {
            return velocity;
        }

        @Override
        public String toString() {
            return new String("Note On (channel " + getChannel()+ "): " + 
                    noteNumber + ", velocity " + velocity);
        }
    }
}