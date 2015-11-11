/*
 * LyricsViewParser.java
 * 
 * Created on Nov 12, 2007, 3:07:24 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.*;

import java.util.ArrayList;
import java.util.Iterator;

import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Parses a {@link Sequence} for lyric commands, which it combines together into
 * an {@link LyricsInMidi}.
 * 
 * @author Christine
 */
public class LyricsViewParser {

    /**
     * Parses a sequence and extracts the lyric events.  Note that the sequence 
     * must have division type {@link javax.sound.midi.Sequence#PPQ pulses 
     * (ticks) per quarter note}.
     * 
     * @param seq The sequence you want to parse
     * @return the lyrics
     * @throws edu.columbia.ee.csmit.MidiKaraoke.SequenceDivisionTypeException 
     */
    public static LyricsInMidi parse(Sequence seq) throws SequenceDivisionTypeException{
        if(seq.getDivisionType() != Sequence.PPQ){
            throw new SequenceDivisionTypeException();
        }
        
        
        
        ArrayList<MidiCommandSorter.Info> commandList = MidiCommandSorter.sort(seq);
        ArrayList<LyricInTrackImp> allLyrics = new ArrayList<LyricInTrackImp>();
        Iterator<MidiCommandSorter.Info> it = commandList.iterator();
        while(it.hasNext()){
            MidiCommandSorter.Info info = it.next();
            if (info.getMidiCommand() instanceof Lyric){
                Lyric lyric = (Lyric)info.getMidiCommand();
                TrackMidi trackMidi = new TrackMidiImp(info.getTrack(),
                        info.getSeconds(),info.getTicks());
                LyricInTrackImp tli = new LyricInTrackImp(trackMidi,lyric);
                
                allLyrics.add(tli);
            }
        }        

        ArrayList<LyricInTrack> tmp = new ArrayList<LyricInTrack>(allLyrics);
        return  new LyricsInMidi(tmp);
                
    }
    
        
    /**
     * Implementation of {@link LyricInTrack} for the LyricsViewParser.
     */
    private static class LyricInTrackImp implements LyricInTrack{
        private TrackMidi trackMidi;
        private Lyric lyric;
        
        /**
         * Constructor.
         */
        public LyricInTrackImp(TrackMidi trackMidi, Lyric lyric){
            this.trackMidi = trackMidi;
            this.lyric = lyric;
        }
        

        @Override
        public long getTicks() {
            return trackMidi.getTicks();
        }

        @Override
        public double getSeconds() {
            return trackMidi.getSeconds();
        }

        @Override
        public int getTrackNumber(){
            return trackMidi.getTrackNumber();
        }

        @Override
        public byte[] getTextBytes() {
            return lyric.getTextBytes();
        }
        
        /**
         * Writes out the lyric as follows:
         * Lyric: "This is the lyric" , Time - ticks (seconds), Track Number - number
         */
        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();
            
            sb.append("Lyric: \""+lyric.getText()+"\"");
            sb.append(" , Time - "+trackMidi.getTicks()+"(");
            sb.append(trackMidi.getSeconds()+"), Track Number - ");
            sb.append(trackMidi.getTrackNumber());
            
            return sb.toString();
        }

        @Override
        public String getText() {
            return lyric.getText();
        }

        @Override
        public int getLength() {
            return lyric.getLength();
        }

        @Override
        public byte[] getMessage() {
            return lyric.getMessage();
        }
    }
}
