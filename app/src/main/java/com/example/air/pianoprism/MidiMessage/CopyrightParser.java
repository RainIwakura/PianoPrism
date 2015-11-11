/*
 * CopyrightParser.java
 * 
 * Created on Nov 7, 2007, 10:30:43 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a Copywrite meta command.
 *
 * @see Copyright
 * @author Christine
 */
public class CopyrightParser extends MetaCommandParser{
    public CopyrightParser(){
        // nothing to do
    }
    
    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();
        
        if(message[0] == RawMidiMessageParser.META_EVENT &&
                message[1] == RawMidiMessageParser.COPYRIGHT_TEXT_EVENT){
            return new Command(message,parseString(message,2));
        }
        else{        
            return null;
        }
    }

    private static class Command extends MetaCommandParser.Command implements Copyright {

        private String copyright = null;

        public Command(byte[] message, String copyright) {
            super(message);
            this.copyright = copyright;
        }

        public String getCopyright() {
            return copyright;
        }

        @Override
        public String toString() {
            return new String("Copyright:  "+ copyright);
        }
    }

    
}
