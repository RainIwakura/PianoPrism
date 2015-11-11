/*
 * TextEventParser.java
 * 
 * Created on Nov 7, 2007, 11:34:46 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a text command.
 *
 * @see Text
 * @author Christine
 */
public class TextParser extends MetaCommandParser{
    public TextParser(){
        // nothing here
    }
    
    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();
        
        if(message[0] == RawMidiMessageParser.META_EVENT &&
                message[1] == RawMidiMessageParser.TEXT_EVENT){
            return new Command(message,parseString(message,2));
        }
        else{        
            return null;
        }
    }
    
    
    private static class Command extends
            MetaCommandParser.Command implements Text{
        
        String text = null;
        
        public Command(byte[] message, String text){
            super(message);
            this.text = text;
        }

        public String getText() {
            return text;
        }
        
        @Override
        public String toString(){
            return new String("Text: "+text);
        }
    }
}
