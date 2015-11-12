package com.example.air.pianoprism;

/**
 * Created by rednecked_crake on 11/12/15.
 */
public class MidiSegObject {
    double [][] scoreMP;
    double[][] scoreSeg;
    int[] segIdx;

    public MidiSegObject(double[][] scoreMP, double[][] scoreSeg, int[] segIdx) {
        this.scoreMP = scoreMP;
        this.scoreSeg = scoreSeg;
        this.segIdx = segIdx;
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
}
