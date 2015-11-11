/*
 * TextEvent.java
 * 
 * Created on Nov 1, 2007, 6:51:14 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.MidiMessage;

/**
 * Stores text commands.
 *
 * @see TextParser
 * @author Christine
 */
public interface Text extends MetaCommand{
    /**
     * Returns the text in this command.
     * 
     * @return the text
     */
    public String getText();
}
