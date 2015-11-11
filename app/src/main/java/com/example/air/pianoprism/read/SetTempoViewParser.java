/*
 * TempoViewParser.java
 * 
 * Created on Nov 25, 2007, 4:49:15 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.SetTempo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.kshoji.javax.sound.midi.Sequence;

/**
 * Used to parse a {@link Sequence} for tempo commands.
 * @author Christine
 */
public class SetTempoViewParser {
    /**
     * Parses a sequence and extracts the tempo events.  Note that the sequence 
     * must have division type {@link javax.sound.midi.Sequence#PPQ pulses 
     * (ticks) per quarter note}.
     * 
     * @param seq The sequence you want to parse
     * @return the tempo changes
     * @throws edu.columbia.ee.csmit.MidiKaraoke.SequenceDivisionTypeException 
     */
    public static SetTemposInMidi parse(Sequence seq) throws SequenceDivisionTypeException{
        
        ArrayList<MidiCommandSorter.Info> commandList = 
               MidiCommandSorter.sort(seq);
        
        LinkedList<SetTempoInTrackImp> tempos = new LinkedList<SetTempoInTrackImp>();
        Iterator<MidiCommandSorter.Info> it = commandList.iterator();
        while(it.hasNext()){
            MidiCommandSorter.Info info = it.next();
            if (info.getMidiCommand() instanceof SetTempo){
                SetTempo st = (SetTempo)info.getMidiCommand();
                TrackMidi trackMidi = new TrackMidiImp(info.getTrack(),
                        info.getSeconds(),info.getTicks());
                SetTempoInTrackImp tti = new SetTempoInTrackImp(
                        trackMidi,st);
                
                tempos.add(tti);
            }
        }
        
        return new SetTemposInMidiImp(tempos);
    }
    
    private static class SetTemposInMidiImp implements SetTemposInMidi{
        private SetTempoInTrack[] tempos;
        
        public SetTemposInMidiImp(List<SetTempoInTrackImp> listTempos){
            tempos = new SetTempoInTrack[listTempos.size()];
            listTempos.toArray(tempos);
        }

        @Override
        public SetTempoInTrack[] getTempos() {
            return tempos;
        }

        @Override
        public double[][] getTemposDoubles() {
            double[][] ret = new double[tempos.length][4];
            
            for(int i=0;i<tempos.length;i++){
                SetTempoInTrack tt = tempos[i];
                ret[i][0] = tt.getMicrosecondsPerQuarterNote();
                ret[i][1] = tt.getTicks();
                ret[i][2] = tt.getSeconds();
                ret[i][3] = tt.getTrackNumber();
            }
            
            return ret;
        }
        
    }
    
    private static class SetTempoInTrackImp implements SetTempoInTrack{
        TrackMidi trackMidi;
        SetTempo setTempo;

        public SetTempoInTrackImp(TrackMidi trackMidi, SetTempo setTempo){
            this.trackMidi = trackMidi;
            this.setTempo = setTempo;
        }

        @Override
        public int getMicrosecondsPerQuarterNote() {
            return setTempo.getMicrosecondsPerQuarterNote();
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
            
            sb.append("Tempo: "+setTempo.getMicrosecondsPerQuarterNote());
            sb.append(" , Time - "+trackMidi.getTicks());
            sb.append("("+trackMidi.getSeconds()+"), Track Number - ");
            sb.append(trackMidi.getTrackNumber());
            
            return sb.toString();
        }

        @Override
        public int getLength() {
            return setTempo.getLength();
        }

        @Override
        public byte[] getMessage() {
            return setTempo.getMessage();
        }
        
    }
}
