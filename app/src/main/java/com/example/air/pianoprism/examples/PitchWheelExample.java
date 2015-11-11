/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.examples;

import com.example.air.pianoprism.read.PitchWheelChangeInTrack;
import com.example.air.pianoprism.read.PitchWheelChangeViewParser;
import com.example.air.pianoprism.read.PitchWheelChangesInMidi;

import java.io.File;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Shows how to use the {@link PitchWheelChangeViewParser}.
 *
 * <blockquote><pre>
    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Usage: PitchWheelExample [midi file name]");
            return;
        }


        try {
            File file = new File(args[0]);
            Sequence mySeq = MidiSystem.getSequence(file);
            PitchWheelChangesInMidi roll = PitchWheelChangeViewParser.parse(mySeq);
            PitchWheelChangeInTrack[] pitchWheels = roll.getPitchWheelChanges();
            for(int i=1;i &lt pitchWheels.length;i++){
                System.out.println(pitchWheels[i].toString());
            }
        } catch (Exception e) {
            System.out.println("Problem!");
            e.printStackTrace();
            System.out.println(e.toString());
        }

    }

 * </pre></blockquote>
 *
 * @author Christine
 */
public class PitchWheelExample {
    /**
     * Shows how to use the {@link PitchWheelChangeViewParser} to parse midi commands
     * in a {@link PitchWheelChangesInMidi}.  Prints out the pitch wheel commands.
     *
     * @param args the command line arguments.  The first (and only) argument
     * should be the name of a midi file.
     */
    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Usage: PitchWheelExample [midi file name]");
            return;
        }


        try {
            File file = new File(args[0]);
            Sequence mySeq = MidiSystem.getSequence(file);
            PitchWheelChangesInMidi roll = PitchWheelChangeViewParser.parse(mySeq);
            PitchWheelChangeInTrack[] pitchWheels = roll.getPitchWheelChanges();
            for(int i=1;i<pitchWheels.length;i++){
                System.out.println(pitchWheels[i].toString());
            }
        } catch (Exception e) {
            System.out.println("Problem!");
            e.printStackTrace();
            System.out.println(e.toString());
        }

    }



}
