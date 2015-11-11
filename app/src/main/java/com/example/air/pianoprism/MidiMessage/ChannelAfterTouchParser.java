/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a Channel after-touch command
 * @author Christine
 */
public class ChannelAfterTouchParser extends MidiCommandWithChannelParser {
    public ChannelAfterTouchParser(){
        // nothing to do...
    }

    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();

        byte upper = (byte) (message[0]&0xF0);

        if(upper == RawMidiMessageParser.CHANNEL_AFTER_TOUCH){
            return new Command(message,message[1]);
        }
        else{
            return null;
        }
    }

    private class Command extends MidiCommandWithChannelParser.Command implements ChannelAfterTouch {
        private byte channel;

        public Command(byte[] message, byte channel){
            super(message);
            this.channel = channel;
        }

        @Override
        public byte getChannelNumber() {
            return channel;
        }

    }
}
