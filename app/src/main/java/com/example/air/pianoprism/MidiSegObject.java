package com.example.air.pianoprism;

import java.util.ArrayDeque;

/**
 * Created by rednecked_crake on 11/12/15.
 */
public class MidiSegObject {
    double [][] scoreMP;
    double[][] scoreSeg;
    ArrayDeque<Integer> segIdx;

    public MidiSegObject(double[][] scoreMP, double[][] scoreSeg, ArrayDeque<Integer> segIdx) {
        this.scoreMP = scoreMP;
        this.scoreSeg = scoreSeg;
        this.setSegIdx(segIdx);
    }

    public double[][] getScoreMP() {
        return scoreMP;
    }

    public void setScoreMP(double[][] scoreMP) {
        this.scoreMP = scoreMP;
    }

    public double[][] getScoreSeg() {
        return scoreSeg;
    }

    public void setScoreSeg(double[][] scoreSeg) {
        this.scoreSeg = scoreSeg;
    }

    public ArrayDeque<Integer> getSegIdx() {
        return segIdx;
    }

    public void setSegIdx(ArrayDeque<Integer> segIdx) {
        this.segIdx = segIdx;
    }
}
