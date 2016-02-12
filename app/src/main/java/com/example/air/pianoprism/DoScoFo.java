package com.example.air.pianoprism;


import java.util.Arrays;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * Created by rednecked_crake on 2/6/16.
 */
public class DoScoFo {

    static double cmpr;


    String pieceName = "MAPS_MUS-grieg_butterfly_ENSTDkCl";



	/*
	 *   SET PARAMETERS CHROMA
	 */


    /// parameters for processing audio signal
    double rms_Th = 0.025;
    int frameLen = 2048;
    int frameHop = 441;
    int zpf = 4;
    int fftLen = (int) Math.pow(2, frameLen * zpf);
    double[] win = hammingWin(fftLen, "periodic");
    //////////////////


    /// parameters for chroma features

    int nbin = 12;

    /// parameters for process model
    int parNum = 1000;


    /// bSpecSub = 0 or 1
    boolean bSpecSub = false;



    /*
     * frameNum - calculated from user inputted time of practice
     */

    public DoScoFo (double[][] nmat, int frameNum, int fs) {
        DenseDoubleMatrix2D nmatDM = new DenseDoubleMatrix2D(nmat);
        DoubleMatrix1D col1 = nmatDM.viewColumn(1);
        DoubleMatrix1D col2 = nmatDM.viewColumn(2);

        DoubleMatrix1D hund;
        hund = new DenseDoubleMatrix1D(new double[nmatDM.viewColumn(1).cardinality()]);
        hund.assign(100);

        col1.assign(hund, mult);
        col1.assign(round);
        col1.assign(hund, div);


        col2.assign(hund, mult);
        col2.assign(round);
        col2.assign(hund, div);

        double minBeat = col1.aggregate(retSmaller, identity);
        double maxBeat = col1.copy().assign(col2.copy(), plus).aggregate(retBigger, identity);

        // 60*(max(nmat(:,1))-min(nmat(:,1))) /(max(nmat(:,6))-min(nmat(:,6)));
        double scoreTempo = 60* ( col1.copy().aggregate(retBigger, identity)
                                - col1.copy().aggregate(retSmaller, identity))/
                                (nmatDM.viewColumn(6).copy().aggregate(retBigger, identity)
                                        -
                                 nmatDM.viewColumn(6).copy().aggregate(retSmaller, identity)
                                );


        double minBPM = 0.75 * scoreTempo;
        double maxBPM = 1.25 * scoreTempo;
        double sigma_v = scoreTempo/4;

        FindMidiSeg fms = new FindMidiSeg(nmat);
        MidiSegObject result = fms.findMidiSeg();

        int scoreSegNum = result.scoreSeg[0].length;


        Integer[] allScoreIdx = new Integer[scoreSegNum];
        for (int i = 0; i < scoreSegNum; i++) {
            allScoreIdx[i] = i;
        }

        double timeLen = 120 * 1000;

        double[][] xs = new double[2][frameNum+1];
        Arrays.fill(xs[0], 0);
        Arrays.fill(xs[1], 0);


        int frameHopMin = frameHop/fs/60;

        int[] F = {1, frameHopMin, 0, 1};

        


    }





    public double[] hammingWin(int fftLen, String type) {
        double[] res = new double[fftLen];
        switch(type) {
            case "periodic": {
                int  n = fftLen + 1;
                double[] resTemp = new double[n];
                for(int i = 0; i < n; i++){
                    resTemp[i] = (float) (( 0.53836 - ( 0.46164 * Math.cos( 2*Math.PI * (double)i  / (double)( n - 1 ) ) ) ) );
                }

                for (int i = 0; i < fftLen; i++) {
                    res[i] = resTemp[i];
                }
            }
            case "none": {
                for(int i = 0; i < fftLen; i++){
                    res[i] = (float) (( 0.53836 - ( 0.46164 * Math.cos( 2*Math.PI * (double)i  / (double)( fftLen - 1 ) ) ) ) );
                }

            }

        }
        return res;
    }



    DoubleDoubleFunction plus = new DoubleDoubleFunction() {
        public double apply(double a, double b) {
            return a + b;
        }
    };


    DoubleDoubleFunction mult = new DoubleDoubleFunction() {
        public double apply(double a, double b) {
            return a * b;
        }
    };


    DoubleDoubleFunction div = new DoubleDoubleFunction() {
        public double apply(double a, double b) {
            return a / b;
        }
    };

    DoubleDoubleFunction lessCmpr = new DoubleDoubleFunction() {

        @Override
        public double apply(double v, double v1) {
            if (v1 < cmpr)
                return 1;
            else
                return 0;
        }

    };



    DoubleDoubleFunction moreCmpr = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            if (v1 > cmpr) {
                return 1;
            } else {
                return 0;
            }

        }

    };

    DoubleDoubleFunction equalCmpr = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {

            if (v1 == cmpr) {
                return 1;
            } else
                return 0;
        }

    };

    DoubleDoubleFunction or = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            return ((v == 1) || (v1 == 1)) ? 1 : 0;
        }

    };

    DoubleDoubleFunction and = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            return ((v1 == 1) && (v == 1)) ? 1 : 0;
        }

    };

    DoubleFunction round = new DoubleFunction() {
        @Override
        public double apply(double v) {
            return Math.round(v);
        }
    };

    DoubleDoubleFunction retBigger = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            return v > v1 ? v : v1;
        }
    };


    DoubleDoubleFunction retSmaller = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            return v < v1 ? v : v1;
        }
    };

    DoubleFunction identity = new DoubleFunction() {
        @Override
        public double apply(double v) {
            return v;
        }
    };
}