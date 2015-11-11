/*
 * ControlChangeParser.java
 * 
 * Created on Dec 2, 2007, 11:03:29 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

import jp.kshoji.javax.sound.midi.MidiMessage;

/**
 * Parses a Control Change event.
 *
 * @see ControlChange
 * @author Christine
 */
public class ControlChangeParser extends MidiCommandWithChannelParser {

    public ControlChangeParser() {
        // nothing to do here
    }

    @Override
    public MidiCommand parse(MidiMessage mm) {
        byte[] message = mm.getMessage();

        byte upper = (byte) (message[0]&0xF0);
        
        if(upper == RawMidiMessageParser.CONTROL_CHANGE){
            return new Command(message,message[1],message[2]);
        }
        else{
            return null;
        }
    }
    
    private class Command extends MidiCommandWithChannelParser.Command implements ControlChange {
        private int controllerNumber;
        private int newValue;
        
        public Command(byte[] message,int controllerNumber, int newValue){
            super(message);
            this.controllerNumber = controllerNumber;
            this.newValue = newValue;
        }
        
        public int getControllerNumber() {
            return controllerNumber;
        }

        public int getNewValue() {
            return newValue;
        }
        
        @Override
        public String toString(){
            return new String("Control Change (channel " + getChannel()+ "): " + 
                    "controller number " +controllerNumber +", new value " + 
                    newValue);
        }
    }

}
