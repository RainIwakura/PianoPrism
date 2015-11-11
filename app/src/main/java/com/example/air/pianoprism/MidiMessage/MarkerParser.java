/*
 * MarkerParser.java
 * 
 * Created on Nov 7, 2007, 11:13:47 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a marker meta command.
 *
 * @see Marker
 * @author Christine
 */
public class MarkerParser extends MetaCommandParser{
    public MarkerParser(){
        // nothing to do
    }
    
    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();
        
        if(message[0] == RawMidiMessageParser.META_EVENT &&
                message[1] == RawMidiMessageParser.MARKER){
            return new Command(message,parseString(message,2));
        }
        else{        
            return null;
        }
    }

    private static class Command extends 
            MetaCommandParser.Command implements Marker{
        
        private String text = null;
        
        public Command(byte[] message, String text){
            super(message);
            this.text = text;
        }

        public String getText() {
            return text;
        }
        
        @Override
        public String toString(){
            return new String("Marker: "+ text);
        }
    }
    
}
