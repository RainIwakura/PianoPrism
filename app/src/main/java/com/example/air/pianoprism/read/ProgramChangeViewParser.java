/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.ProgramChange;

import java.util.ArrayList;
import java.util.Iterator;

import jp.kshoji.javax.sound.midi.Sequence;

/**
 *
 * @author Christine
 */
public class ProgramChangeViewParser {

    /**
     * Parses a sequence and extracts the lyric events.  Note that the sequence
     * must have division type {@link javax.sound.midi.Sequence#PPQ pulses
     * (ticks) per quarter note}.
     *
     * @param seq The sequence you want to parse
     * @return the lyrics
     * @throws edu.columbia.ee.csmit.MidiKaraoke.SequenceDivisionTypeException
     */
    public static ProgramChangesInMidi parse(Sequence seq) throws
            SequenceDivisionTypeException {
        if (seq.getDivisionType() != Sequence.PPQ) {
            throw new SequenceDivisionTypeException();
        }

        ArrayList<MidiCommandSorter.Info> commandList =
                MidiCommandSorter.sort(seq);
        ArrayList<ProgramChangeInTrack> allPitchWheels =
                new ArrayList<ProgramChangeInTrack>();
        Iterator<MidiCommandSorter.Info> it = commandList.iterator();
        while (it.hasNext()) {
            MidiCommandSorter.Info info = it.next();
            if (info.getMidiCommand() instanceof ProgramChange) {
                ProgramChange change =
                        (ProgramChange) info.getMidiCommand();
                TrackMidi trackMidi = new TrackMidiImp(info.getTrack(),
                        info.getSeconds(), info.getTicks());
                ProgramChangeInTrackImp pcit =
                        new ProgramChangeInTrackImp(trackMidi, change);

                allPitchWheels.add(pcit);
            }
        }
        return new ProgramChangesInMidi(allPitchWheels);
    }

    private static class ProgramChangeInTrackImp
            implements ProgramChangeInTrack {
        private TrackMidi trackMidi;
        private ProgramChange programChange;

        public ProgramChangeInTrackImp(TrackMidi trackMidi,
                ProgramChange programChange){
            this.trackMidi = trackMidi;
            this.programChange = programChange;
        }

        @Override
        public int getProgramNumber() {
            return programChange.getProgramNumber();
        }

        @Override
        public int getLength() {
            return programChange.getLength();
        }

        @Override
        public byte[] getMessage() {
            return programChange.getMessage();
        }

        @Override
        public int getTrackNumber() {
            return trackMidi.getTrackNumber();
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
        public int getChannel() {
            return programChange.getChannel();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Program Change: " + programChange.getProgramNumber());
            sb.append(" , Time - " + trackMidi.getTicks() + "(");
            sb.append(trackMidi.getSeconds() + "), Track Number - ");
            sb.append(trackMidi.getTrackNumber() + " , Channel - ");
            sb.append(programChange.getChannel());
            return sb.toString();
        }
    }
}
