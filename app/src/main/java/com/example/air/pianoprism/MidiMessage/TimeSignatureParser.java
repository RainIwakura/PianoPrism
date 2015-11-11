/*
 * TimeSignatureParser.java
 * 
 * Created on Nov 7, 2007, 11:40:07 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a time signature command.
 *
 * @see TimeSignature
 * @author Christine
 */
public class TimeSignatureParser extends MetaCommandParser{
    public TimeSignatureParser(){
        // nothing to do here
    }
    
    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();

        if (message[0] == RawMidiMessageParser.META_EVENT 
                && message[1] == RawMidiMessageParser.TIME_SIGNATURE) {
            
            return new Command(message,message[3],message[4],message[5],
                    message[6]);
        } else {
            return null;
        }
    }
    
    
    public static class Command extends
            MetaCommandParser.Command implements TimeSignature{
        
        private int numerator = 0;
        private int denominator = 0;
        private int metronomeClick = 0;
        private int midiQuarterNote = 0;
        
        public Command(byte[] message, int numerator,
                int denominator, int metronomeClick, int midiQuarterNote){
            super(message);
            this.numerator = numerator;
            this.denominator = denominator;
            this.metronomeClick = metronomeClick;
            this.midiQuarterNote = midiQuarterNote;
        }

        public int getNumerator() {
            return numerator;
        }

        public int getDenominator() {
            return denominator;
        }

        public int getMetronomeClick() {
            return metronomeClick;
        }

        public int getMidiQuarterNote() {
            return midiQuarterNote;
        }
        
        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();
            
            sb.append("Time Signature: "+numerator);
            int true_denom = 1;
            for(int i=0;i<denominator;i++){
                true_denom = true_denom*2;
            }
            sb.append("/"+true_denom);
            
            sb.append(" ("+metronomeClick+" midi clocks in metronome click)");
            sb.append(" ("+midiQuarterNote+
                    " notated 32nd notes in a midi quarter note)");
                   
                    
            return sb.toString();
        }
    }
    

}
