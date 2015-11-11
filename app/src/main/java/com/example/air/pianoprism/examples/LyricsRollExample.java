/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.examples;

import com.example.air.pianoprism.read.LyricsInMidi;
import com.example.air.pianoprism.read.LyricsViewParser;

import java.io.File;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Shows how to use the {@link LyricsViewParser}.
 *
 * <blockquote><pre>
     public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Usage: LyricsRollExample [midi file name]");
            return;
        }


        try {

            File file = new File(args[0]);
            Sequence mySeq = MidiSystem.getSequence(file);

            LyricsInMidi lyricsRoll = LyricsViewParser.parse(mySeq);

            // get the track numbers with lyrics
            int[] tracks = lyricsRoll.getTrackNumbers();

            // Separate out the lyrics by track...
            for(int i=0;i &lt tracks.length;i++){
                System.out.format("****** Track %d:\n", tracks[i]);
                LyricsInMidi.Line[] lines = lyricsRoll.getLines(tracks[i]);
                for(int j=0;j &lt lines.length;j++){
                    // Print out the time in seconds and the line
                    System.out.format("%f: %s\n",lines[j].getSeconds(),
                            lines[j].getLine());
                }
                System.out.println("");
            }

        } catch (Exception e) {
            System.out.println("Problem!");
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
 
 * </blockquote></pre>
 *
 * @author Christine
 */
public class LyricsRollExample {

    /**
     * Shows how to use the {@link LyricsViewParser} to parse just the lyrics
     * in a {@link Sequence}.  Prints out the midi commands it reads.
     * 
     *
     * @param args the command line arguments.  The first (and only) argument
     * should be the name of a midi file.
     */
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Usage: LyricsRollExample [midi file name]");
            return;
        }


        try {

            File file = new File(args[0]);
            Sequence mySeq = MidiSystem.getSequence(file);

            LyricsInMidi lyricsRoll = LyricsViewParser.parse(mySeq);

            // get the track numbers with lyrics
            int[] tracks = lyricsRoll.getTrackNumbers();

            // Separate out the lyrics by track...
            for(int i=0;i<tracks.length;i++){
                System.out.format("****** Track %d:\n", tracks[i]);
                LyricsInMidi.Line[] lines = lyricsRoll.getLines(tracks[i]);
                for(int j=0;j<lines.length;j++){
                    // Print out the time in seconds and the line
                    System.out.format("%f: %s\n",lines[j].getSeconds(),
                            lines[j].getLine());
                }
                System.out.println("");
            }

        } catch (Exception e) {
            System.out.println("Problem!");
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
