/*
 * ProgramChangeParser.java
 *
 * Created on Dec 2, 2007, 11:19:31 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a program change command.
 *
 * @see ProgramChange
 * @author Christine
 */
public class ProgramChangeParser extends MidiCommandWithChannelParser{

    public ProgramChangeParser() {
        // nothing to do here
    }

    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();

        byte upper = (byte) (message[0]&0xF0);

        if (upper == RawMidiMessageParser.PROGRAM_CHANGE) {
            return new Command(message, message[1]);
        } else {
            return null;
        }
    }

    private class Command extends MidiCommandWithChannelParser.Command implements ProgramChange {
        private int programNumber;

        public Command(byte[] message, int programNumber) {
            super(message);
            this.programNumber = programNumber;
        }

        public int getProgramNumber() {
            return programNumber;
        }
        
        
        @Override
        public String toString() {
            return new String("Program Change (channel " + getChannel() + "): " 
                    + "program number " + programNumber);
        }
    }
}