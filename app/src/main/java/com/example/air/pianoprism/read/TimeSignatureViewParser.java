/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.TimeSignature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Parses a {@link Sequence} for time signature events.
 * @author Christine
 */
public class TimeSignatureViewParser {

    /**
     * Parses a sequence and extracts the time signatures events.  Note that the sequence 
     * must have division type {@link javax.sound.midi.Sequence#PPQ pulses 
     * (ticks) per quarter note}.
     * 
     * @param seq The sequence you want to parse
     * @return the tempo changes
     * @throws edu.columbia.ee.csmit.MidiKaraoke.SequenceDivisionTypeException 
     */
    public static TimeSignaturesInMidi parse(Sequence seq) throws SequenceDivisionTypeException{
        
        ArrayList<MidiCommandSorter.Info> commandList = 
               MidiCommandSorter.sort(seq);
        
        LinkedList<TimeSignatureInTrackImp> timeSignatures =
                new LinkedList<TimeSignatureInTrackImp>();
        Iterator<MidiCommandSorter.Info> it = commandList.iterator();
        while(it.hasNext()){
            MidiCommandSorter.Info info = it.next();
            if (info.getMidiCommand() instanceof TimeSignature){
                TimeSignature ts = (TimeSignature)info.getMidiCommand();
                TrackMidi trackMidi = new TrackMidiImp(info.getTrack(),
                        info.getSeconds(),info.getTicks());
                
                TimeSignatureInTrackImp tti = new TimeSignatureInTrackImp(
                        trackMidi,ts);
                
                timeSignatures.add(tti);
            }
        }
        
        return new TimeSignatureRollImp(timeSignatures);
    }    
    
    private static class TimeSignatureRollImp implements TimeSignaturesInMidi{
        private TimeSignatureInTrack[] timeSignatures;
        
        public TimeSignatureRollImp(List<TimeSignatureInTrackImp> listTimeSignatures){
            timeSignatures = new TimeSignatureInTrack[listTimeSignatures.size()];
            listTimeSignatures.toArray(timeSignatures);
        }

        @Override
        public TimeSignatureInTrack[] getTimeSignatures() {
            return timeSignatures;
        }
        
        /*
         * Columns:
         * 0 - numerator
         * 1 - denominator
         * 2 - metronome click
         * 3 - midi quarter note
         * 4 - time in ticks
         * 5 - time in seconds
         * 6 - track number
         */
        @Override
        public double[][] getTimeSignaturesDoubles() {
            double[][] ret = new double[timeSignatures.length][7];
            
            for(int i=0;i<timeSignatures.length;i++){
                ret[i][0] = timeSignatures[i].getNumerator();
                ret[i][1] = timeSignatures[i].getDenominator();
                ret[i][2] = timeSignatures[i].getMetronomeClick();
                ret[i][3] = timeSignatures[i].getMidiQuarterNote();
                ret[i][4] = timeSignatures[i].getTicks();
                ret[i][5] = timeSignatures[i].getSeconds();
                ret[i][6] = timeSignatures[i].getTrackNumber();
            }
            return ret;
        }
        
    }
    
    private static class TimeSignatureInTrackImp implements TimeSignatureInTrack{
        private TrackMidi trackMidi;
        private TimeSignature timeSignature;
                
        public TimeSignatureInTrackImp(TrackMidi trackMidi,
                TimeSignature timeSignature){
            this.timeSignature = timeSignature;
            this.trackMidi = trackMidi;
        }

        @Override
        public int getNumerator() {
            return timeSignature.getNumerator();
        }

        @Override
        public int getDenominator() {
            return timeSignature.getDenominator();
        }

        @Override
        public int getMetronomeClick() {
            return timeSignature.getMetronomeClick();
        }

        @Override
        public int getMidiQuarterNote() {
            return timeSignature.getMidiQuarterNote();
        }

        @Override
        public double getSeconds() {
            return trackMidi.getSeconds();
        }

        @Override
        public long getTicks() {
            return trackMidi.getTicks();
        }

        @Override
        public int getTrackNumber(){
            return trackMidi.getTrackNumber();
        }
        
        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();
            
            sb.append("Time Signature: "+timeSignature.getNumerator());
            sb.append("/"+timeSignature.getDenominator());
            sb.append(", Metronome Click "+timeSignature.getMetronomeClick());
            sb.append(", Midi Quarter Note "+timeSignature.getMidiQuarterNote());
            sb.append(" , Time - "+trackMidi.getTicks()+"(");
            sb.append(trackMidi.getSeconds()+"), Track Number - ");
            sb.append(trackMidi.getTrackNumber());
            
            return sb.toString();
        }

        @Override
        public int getLength() {
            return timeSignature.getLength();
        }

        @Override
        public byte[] getMessage() {
            return timeSignature.getMessage();
        }
        
    }
}
