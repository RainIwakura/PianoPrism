/*
 * TrackInstrumentNameParser.java
 * 
 * Created on Nov 7, 2007, 11:51:11 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a track intrument name command.
 *
 * @see TrackInstrumentName
 * @author Christine
 */
public class TrackInstrumentNameParser extends MetaCommandParser{
    public TrackInstrumentNameParser(){
        // nothing to do here
    }

    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();
        
        if(message[0] == RawMidiMessageParser.META_EVENT &&
                message[1] == RawMidiMessageParser.TRACK_INSTRUMENT_NAME){
            return new Command(message,parseString(message,2));
        }
        else{        
            return null;
        }
    }
    
    
    private static class Command extends
            MetaCommandParser.Command implements
            TrackInstrumentName{
        
        private String name = null;
        
        public Command(byte[] message, 
                String name){
            super(message);
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        @Override
        public String toString(){
            return new String("Track Instrument Name: "+name);
        }
    }
    
}
