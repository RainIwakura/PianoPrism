package com.example.air.pianoprism.examples;

/**
 * Created by Air on 7/6/2015.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.example.air.pianoprism.read.PianoRoll;
import com.example.air.pianoprism.read.PianoRollViewParser;
import com.example.air.pianoprism.read.SequenceDivisionTypeException;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;


public class PianoRollExampleDoubles {

    public int mapFrom(int i) {
        i = i + 1;
        int res = 0;
        switch(i) {
            case 1:
                res = 4;
                break;
            case 2:
                res = 5;
                break;
            case 8:
                res = 3;
                break;
            case 3:
                res = -1;
                break;
            case 4:
                res = 1;
                break;
            case 5:
                res = 2;
                break;
            case 6:
                res = 6;
                break;
            case 7:
                res = 7;
                break;

        }
        return res - 1;
    }


    public double[][] rearrange(double[][] in) {
        double[][] result = new double[in.length][in[0].length - 1];

        for (int i = 0; i < in.length; i++) {

            for (int j = 0; j < 8; j++ ) {
                int j1 = mapFrom(j);
                if (j1 >= 0) {
                    result[i][j1] = j1 < 2 ? in[i][j]/960 : in[i][j];
                }
            }
        }

        return result;
    }

    public double[][] doSmth(File file) {
        double[][] notes = null;
        try {
            Sequence mySeq = MidiSystem.getSequence(file);
            PianoRoll roll = PianoRollViewParser.parse(mySeq);
            notes = roll.getNotesDoubles();

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
        return rearrange(notes);
    }



}
