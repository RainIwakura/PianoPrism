/*
To change this template, choose Tools | Templates
and open the template in the editor.
 */
package com.example.air.pianoprism.examples;

import com.example.air.pianoprism.read.NotesInMidi;
import com.example.air.pianoprism.read.PianoRoll;
import com.example.air.pianoprism.read.PianoRollViewParser;
import com.example.air.pianoprism.read.ProgramChangeInTrack;
import com.example.air.pianoprism.read.ProgramChangeViewParser;
import com.example.air.pianoprism.read.ProgramChangesInMidi;
import com.example.air.pianoprism.read.SequenceDivisionTypeException;
import com.example.air.pianoprism.write.SimpleMidiWriter;
import com.example.air.pianoprism.write.SimpleMidiWriter.InvalidSignature;
import com.example.air.pianoprism.write.SimpleMidiWriter.InvalidTempo;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Shows how to use the  {@link SimpleMidiWriter}.
 *
 * <blockquote><pre>
    public static void main(String[] args) throws InvalidMidiDataException,
            InvalidTempo, IOException, InvalidSignature {

        // define the onset of each note in beats
        double[] onset = {0, 2, 5};
        // make each note 2 beats long
        double[] duration = {2, 2, 2};
        // pitches C4 (middle C), D4, E4
        int[] pitch = {60, 62, 64};
        // put each note on a different channel
        int[] channel = {0, 1, 2};
        // use a variety of velocity (loudness) values
        int[] velocity = {40,65, 90};
        // put all the notes on track 1
        int[] track = {1, 1, 1};
        //  channel  |   track   |   patch number:
        //     0           1               0  (acoustic grand piano)
        //     1           1               56 (trumpet)
        //     2           1               22 (harmonica)
        int[][] patches = {{0,1,0},{1,1,56},{2,1,22}};
        // pick half a second per quarter note
        int microsecondsPerQuarterNote = (int) (0.5 * 1000000.0);
        // pick a resolution of 100 ticks per beat.  This means that the
        // shortest note we can specify is 1/100th of a beat.
        int resolution = 100;
        // pick a time signature, 4/4 (common time) with 1 midi clock per
        // quarter note and 8 32nd notes per quarter note.
        int[] timeSignature = {4,4,1,8};
        // pick some name for the resulting file.
        String fileName = "Temp.mid";

        // write the file
        SimpleMidiWriter.write(fileName, onset, duration, channel, pitch,
                velocity, track, microsecondsPerQuarterNote, resolution,
                timeSignature,patches);
        try {
            File myMidiFile = new File(fileName);
            Sequence seq = MidiSystem.getSequence(myMidiFile);

            // and now read the file back out and see what we get.
            PianoRoll pr = PianoRollViewParser.parse(seq);

            NotesInMidi[] notes = pr.getNotes();
            for(int i=0;i &lt notes.length;i++){
                System.out.println(notes[i].toString());
            }

            // also get the program changes
            ProgramChangesInMidi changesInMidi = ProgramChangeViewParser.parse(seq);
            ProgramChangeInTrack[] changesInTrack =
                    changesInMidi.getProgramChanges();
            for(int i=0;i &lt changesInTrack.length;i++){
                System.out.println(changesInTrack[i].toString());
            }

        } catch (SequenceDivisionTypeException ex) {
            Logger.getLogger(SimpleWriteMidiExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

 * </pre></blockquote>
 *
 * @author Christine
 */
public class SimpleWriteMidiExample {

    /**
     * Shows how to use the {@link SimpleMidiWriter} to create a simple midi
     * file with three notes on track one.
     */
    public static void main(String[] args) throws InvalidMidiDataException,
            InvalidTempo, IOException, InvalidSignature {

        // define the onset of each note in beats
        double[] onset = {0, 2, 5};
        // make each note 2 beats long
        double[] duration = {2, 2, 2};
        // pitches C4 (middle C), D4, E4
        int[] pitch = {60, 62, 64};
        // put each note on a different channel
        int[] channel = {0, 1, 2};
        // use a variety of velocity (loudness) values
        int[] velocity = {40,65, 90};
        // put all the notes on track 1
        int[] track = {1, 1, 1};
        //  channel  |   track   |   patch number:
        //     0           1               0  (acoustic grand piano)
        //     1           1               56 (trumpet)
        //     2           1               22 (harmonica)
        int[][] patches = {{0,1,0},{1,1,56},{2,1,22}};
        // pick half a second per quarter note
        int microsecondsPerQuarterNote = (int) (0.5 * 1000000.0);
        // pick a resolution of 100 ticks per beat.  This means that the
        // shortest note we can specify is 1/100th of a beat.
        int resolution = 100;
        // pick a time signature, 4/4 (common time) with 1 midi clock per
        // quarter note and 8 32nd notes per quarter note.
        int[] timeSignature = {4,4,1,8};
        // pick some name for the resulting file.
        String fileName = "Temp.mid";

        // write the file
        SimpleMidiWriter.write(fileName, onset, duration, channel, pitch,
                velocity, track, microsecondsPerQuarterNote, resolution,
                timeSignature,patches);
        try {
            File myMidiFile = new File(fileName);
            Sequence seq = MidiSystem.getSequence(myMidiFile);

            // and now read the file back out and see what we get.
            PianoRoll pr = PianoRollViewParser.parse(seq);

            NotesInMidi[] notes = pr.getNotes();
            for(int i=0;i<notes.length;i++){
                System.out.println(notes[i].toString());
            }

            // also get the program changes
            ProgramChangesInMidi changesInMidi = ProgramChangeViewParser.parse(seq);
            ProgramChangeInTrack[] changesInTrack = 
                    changesInMidi.getProgramChanges();
            for(int i=0;i<changesInTrack.length;i++){
                System.out.println(changesInTrack[i].toString());
            }

        } catch (SequenceDivisionTypeException ex) {
            Logger.getLogger(SimpleWriteMidiExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
