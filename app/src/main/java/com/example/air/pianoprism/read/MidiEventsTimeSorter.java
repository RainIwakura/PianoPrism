/*
 * Viewer.java
 * 
 * Created on Nov 12, 2007, 10:29:58 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.air.pianoprism.read;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Track;

/**
 * Sorts midi commands by tick time.
 *
 * @see MidiEventsTimeSorter#sort(javax.sound.midi.Sequence)
 * @author Christine
 */
public class MidiEventsTimeSorter {
    /**
     * Returns a list of midi events in sequential order.  The list contains
     * EventAndTrack objects so that each event's associated track can be
     * recovered.
     * @param seq the sequence to sort
     * @return a list of events in sequential time order
     */
    public static ArrayList<EventAndTrack> sort(Sequence seq){
        // We are going to put all the events on ArrayLists associated with
        // their tick number.  So, all the events at tick time 0 will be in one
        // list, all the events at tick time 1 will be in one list, etc.
        // Because these lists are being stored in a tree map keyed by the tick
        // number, when we retrieve the lists, they will be in sorted order.
        Map<Long,LinkedList<EventAndTrack>> map =
                new TreeMap<Long,LinkedList<EventAndTrack>>();
        
        Track[] tracks = seq.getTracks();
        
        // go through all the tracks and add them to the map
        for (int i = 0; i < tracks.length; i++) {
            for (int j = 0; j < tracks[i].size(); j++) {
                MidiEvent ev = tracks[i].get(j);
                long tick = ev.getTick();
                EventAndTrack eat = new EventAndTrack(i,ev);

                // retrieve the appropriate list, if it exists
                LinkedList<EventAndTrack> list = map.get(tick);

                if (list == null) {
                    list = new LinkedList<EventAndTrack>();
                }
                list.add(eat);
                map.put(tick, list);
            }
        }
        
        // Now that we've put all the events into listed, we can get them back
        // out again in order and stick them in the returned list
        
        ArrayList<EventAndTrack> ret = new ArrayList<EventAndTrack>();
        
        Iterator<Long> mapIt = map.keySet().iterator();
        
        while(mapIt.hasNext()){
            LinkedList<EventAndTrack> eat = map.get(mapIt.next());
            Iterator<EventAndTrack> listIt = eat.iterator();
            while(listIt.hasNext()){
                ret.add(listIt.next());
            }
        }
        
        return ret;
    }
    
    /**
     * This is a helper class that holds an midi event and its track number.
     */
    public static class EventAndTrack{
        private int trackNumber;
        private MidiEvent midiEvent;
        
        public EventAndTrack(int trackNumber, MidiEvent midiEvent){
            this.trackNumber = trackNumber;
            this.midiEvent = midiEvent;
        }
        
        public int getTrackNumber(){
            return trackNumber;
        }
        
        public MidiEvent getMidiEvent(){
            return midiEvent;
        }
    }
}
