/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.examples;

import com.example.air.pianoprism.read.NotesInMidi;
import com.example.air.pianoprism.read.PianoRoll;
import com.example.air.pianoprism.read.PianoRollViewParser;
import com.example.air.pianoprism.read.SequenceDivisionTypeException;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;


public class PianoRollExample {



    public void doSmth(File file) {

        try {
            Sequence mySeq = MidiSystem.getSequence(file);
            PianoRoll roll = PianoRollViewParser.parse(mySeq);
            NotesInMidi[] notes = roll.getNotes();
            for(int i=1;i<notes.length;i++){
                System.out.println(notes[i].toString());
            }
        } catch (IOException e) {
            System.out.println("Problem! 1");
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (InvalidMidiDataException e) {
            System.out.println("Problem! 2");
            e.printStackTrace();
        } catch (SequenceDivisionTypeException e) {
            System.out.println("Problem! 3");
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Problem! NPE");

        }

    }



}
