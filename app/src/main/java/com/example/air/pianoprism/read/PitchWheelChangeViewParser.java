/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.air.pianoprism.read;

import com.example.air.pianoprism.MidiMessage.PitchWheelChange;

import java.util.ArrayList;
import java.util.Iterator;

import jp.kshoji.javax.sound.midi.Sequence;

/**
 *
 * @author Christine
 */
public class PitchWheelChangeViewParser {

    /**
     * Parses a sequence and extracts the lyric events.  Note that the sequence
     * must have division type {@link javax.sound.midi.Sequence#PPQ pulses
     * (ticks) per quarter note}.
     *
     * @param seq The sequence you want to parse
     * @return the lyrics
     * @throws edu.columbia.ee.csmit.MidiKaraoke.SequenceDivisionTypeException
     */
    public static PitchWheelChangesInMidi parse(Sequence seq) throws
            SequenceDivisionTypeException {
        if (seq.getDivisionType() != Sequence.PPQ) {
            throw new SequenceDivisionTypeException();
        }

        ArrayList<MidiCommandSorter.Info> commandList =
                MidiCommandSorter.sort(seq);
        ArrayList<PitchWheelChangeInTrack> allPitchWheels =
                new ArrayList<PitchWheelChangeInTrack>();
        Iterator<MidiCommandSorter.Info> it = commandList.iterator();
        while (it.hasNext()) {
            MidiCommandSorter.Info info = it.next();
            if (info.getMidiCommand() instanceof PitchWheelChange) {
                PitchWheelChange pitchWheel = 
                        (PitchWheelChange) info.getMidiCommand();
                TrackMidi trackMidi = new TrackMidiImp(info.getTrack(),
                        info.getSeconds(),info.getTicks());
                PitchWheelChangeInTrackImp tpwi =
                        new PitchWheelChangeInTrackImp(trackMidi,pitchWheel);

                allPitchWheels.add(tpwi);
            }
        }
        return new PitchWheelChangesInMidi(allPitchWheels);
    }

    private static class PitchWheelChangeInTrackImp
            implements PitchWheelChangeInTrack {
        private TrackMidi trackMidi;
        private PitchWheelChange pitchWheel;

        public PitchWheelChangeInTrackImp(TrackMidi trackMidi,
                PitchWheelChange pitchWheelChange) {
            this.trackMidi = trackMidi;
            this.pitchWheel = pitchWheelChange;
        }

        @Override
        public int getValue() {
            return pitchWheel.getValue();
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
        public int getTrackNumber() {
            return trackMidi.getTrackNumber();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Pitch Wheel Change: " + pitchWheel.getValue());
            sb.append(" , Time - " + trackMidi.getTicks() + "(");
            sb.append(trackMidi.getSeconds() + "), Track Number - ");
            sb.append(trackMidi.getTrackNumber() + " , Channel - ");
            sb.append(pitchWheel.getChannel());
            return sb.toString();
        }

        @Override
        public int getChannel() {
            return pitchWheel.getChannel();
        }

        @Override
        public int getLength() {
            return pitchWheel.getLength();
        }

        @Override
        public byte[] getMessage() {
            return pitchWheel.getMessage();
        }
    }
}
