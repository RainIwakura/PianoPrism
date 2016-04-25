package com.example.air.pianoprism;


import android.graphics.Matrix;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayDeque;
import java.util.Iterator;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;


import static com.example.air.pianoprism.MatrixUtils._notBool;
import static com.example.air.pianoprism.MatrixUtils.abs;
import static com.example.air.pianoprism.MatrixUtils.addDimensionToArray;
import static com.example.air.pianoprism.MatrixUtils.any;
import static com.example.air.pianoprism.MatrixUtils.any;
import static com.example.air.pianoprism.MatrixUtils.approxEqual;
import static com.example.air.pianoprism.MatrixUtils.assign;
import static com.example.air.pianoprism.MatrixUtils.assignCol;
import static com.example.air.pianoprism.MatrixUtils.assignRow;
import static com.example.air.pianoprism.MatrixUtils.div_elemWise;
import static com.example.air.pianoprism.MatrixUtils.exp;
import static com.example.air.pianoprism.MatrixUtils.find;
import static com.example.air.pianoprism.MatrixUtils.inxsThatSatisfyComparisonCol;
import static com.example.air.pianoprism.MatrixUtils.inxsThatSatisfyComparisonRow;
import static com.example.air.pianoprism.MatrixUtils.log_elemWise;
import static com.example.air.pianoprism.MatrixUtils.maximum;
import static com.example.air.pianoprism.MatrixUtils.minusMatrix;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWiseIntToDouble;
import static com.example.air.pianoprism.MatrixUtils.mult_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mult_matrix;
import static com.example.air.pianoprism.MatrixUtils.nonZeroInxs;
import static com.example.air.pianoprism.MatrixUtils.nonZeroInxsInt;
import static com.example.air.pianoprism.MatrixUtils.plusMatrix;
import static com.example.air.pianoprism.MatrixUtils.rand;
import static com.example.air.pianoprism.MatrixUtils.randn;
import static com.example.air.pianoprism.MatrixUtils.range;
import static com.example.air.pianoprism.MatrixUtils.power_elemWise;
import static com.example.air.pianoprism.MatrixUtils.randInt;
import static com.example.air.pianoprism.MatrixUtils.mean;
import static com.example.air.pianoprism.MatrixUtils.hammingWin;
import static com.example.air.pianoprism.MatrixUtils.remainder;
import static com.example.air.pianoprism.MatrixUtils.repmat;
import static com.example.air.pianoprism.MatrixUtils.retNonZeroElems;
import static com.example.air.pianoprism.MatrixUtils.sliceOf2dArray;
import static com.example.air.pianoprism.MatrixUtils.sliceOfArray;
import static com.example.air.pianoprism.MatrixUtils.sqrt_elemWise;
import static com.example.air.pianoprism.MatrixUtils.toDoubleArray;
import static com.example.air.pianoprism.MatrixUtils.transpose;
import static com.example.air.pianoprism.MatrixUtils.sum;
import java.util.stream.*;
import java.util.ArrayList;
/**
 * Created by rednecked_crake on 2/6/16.
 * TODO: GOTTA CHECK ALL INDICES
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
    int fftLen = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(frameLen * zpf - 1));

    double[] win = hammingWin(fftLen, "periodic");
    double onset_th = 225;
    //////////////////


    /// parameters for chroma features

    int nbin = 12;

    /// parameters for process model
    int parNum = 1000;


    /// bSpecSub = 0 or 1
    boolean bSpecSub = false;


    double[] prevSample;
    double specFlux;
    double rmsCur;
    /*
     * frameNum - calculated from user-inputted time of practice
     */

    public DoScoFo(double[][] nmat, int frameNum, int fs, double[] sample) {


        System.out.println("FFT LEN: " + fftLen);
        DenseDoubleMatrix2D nmatDM = new DenseDoubleMatrix2D(nmat);
        DoubleMatrix1D col1 = nmatDM.viewColumn(1);
        DoubleMatrix1D col2 = nmatDM.viewColumn(2);

        DoubleMatrix1D hund;
        hund = new DenseDoubleMatrix1D(new double[nmatDM.viewColumn(1).cardinality()]);
        hund.assign(100);

        col1.assign(hund, mult);
        col1.assign(round);
        col1.assign(hund, div);

        //  round(nmat (:, 2)*100)/100
        col2.assign(hund, mult);
        col2.assign(round);
        col2.assign(hund, div);

        double minBeat = col1.aggregate(retSmaller, identity);
        double maxBeat = col1.copy().assign(col2.copy(), plus).aggregate(retBigger, identity);

        // 60*(max(nmat(:,1))-min(nmat(:,1))) /(max(nmat(:,6))-min(nmat(:,6)));
        double scoreTempo = 60 * (col1.copy().aggregate(retBigger, identity)
                - col1.copy().aggregate(retSmaller, identity)) /
                (nmatDM.viewColumn(6).copy().aggregate(retBigger, identity)
                        -
                        nmatDM.viewColumn(6).copy().aggregate(retSmaller, identity)
                );


        final double minBPM = 0.75 * scoreTempo;
        final double maxBPM = 1.25 * scoreTempo;
        double sigma_v = scoreTempo / 4;


        FindMidiSeg fms = new FindMidiSeg(nmat);
        MidiSegObject midiSeg = fms.findMidiSeg();
        int scoreSegNum = midiSeg.scoreSeg[0].length;


        // allScoreIdx = 1:scoreSegNum
        Integer[] allScoreIdx = new Integer[scoreSegNum];
        for (int i = 0; i < scoreSegNum; i++) {
            allScoreIdx[i] = i;
        }


        double timeLen = 120 * 1000;


        double f_ctrl = 0;
        double f_std = 0;
        double A0 = 27.5;
        double A440 = 440;
        double f_ctrl_log = Math.log(f_ctrl / A440) / Math.log(2);
        double[][] ff2cm = fft2chromaX(fftLen, nbin, fs, A440, f_ctrl_log, f_std);


        /// xs = zeroes (2, frameNum + 1)
        double[][] xs = new double[2][2];
        Arrays.fill(xs[0], 0);
        Arrays.fill(xs[1], 0);


        int frameHopMin = frameHop / fs / 60;

        double[] F = {1, frameHopMin, 0, 1};

        double[][] scoreSeg = midiSeg.scoreSeg;
        double[][] scoreMP = midiSeg.scoreMP;

        // repmat onsetMat
        double[][] onsetMat = new double[parNum][scoreSeg[0].length];

        for (int i = 0; i < parNum; i++) {
            for (int j = 0; j < scoreSeg[0].length; j++) {
                onsetMat[i][j] = scoreSeg[0][j];
            }
        }


        // repmat offsetMat

        double[][] offsetMat = new double[parNum][scoreSeg[0].length];

        for (int i = 0; i < parNum; i++) {
            for (int j = 0; j < scoreSeg[0].length; j++) {
                onsetMat[i][j] = scoreSeg[1][j];
            }
        }


        int bStart = 0;

        double[] chromaAF = new double[nbin];
        double chromaAFEnergy;


        double[] wx;
        double[][] uChromaMF;
        double[] notes;
        double[][] unotes;

        int bFindOnset = 0;
        double[] binind;

        double[] uChromaMFenergy;
        double[] uwx;
        double angle;

        for (int i = 0; i < frameNum; i++) {
           // int startp = 1 + (i - 1) * frameHop;
           // int endp = startp + frameLen - 1;
         //   double[] data = new double[endp - startp];


            double[] data = new double[sample.length];

            double[][] particles = new double[2][parNum];
            double[] x_init = {minBeat, scoreTempo};


            for (int j = 0; j < sample.length; j++) {
                data[j] = sample[j] * win[j];
            }

            if (bStart == 0) {
                if (mean(power_elemWise(data, 2)) < rms_Th) {
                    continue;
                } else {
                    bStart = 1;


                    for (int k = 0; k < xs.length; k++) {
                        xs[k][1] = x_init[k]; // ?????????????????? //  I think I've changed this
                    }


                    for (int k = 0; k < parNum; k++) {
                        particles[0][k] = minBeat;
                    }

                    for (int k = 0; k < parNum; k++) {
                        Random rand = new Random();
                        particles[1][k] = rand.nextDouble() * (maxBPM - minBPM) + minBPM;
                    }


                }

            }

            //     fx = zeros(1, parNum);                      % corresponding score segment of each particle position
            int[] fx = new int[parNum];

            for (int k = 0; k < parNum; k++) {
                fx[k] = 0;
            }


            //     ppMat = repmat(px(1,:)', 1, scoreSegNum);            % particle position matrix

            double[][] ppMat = new double[particles[0].length][scoreSegNum];

            for (int j = 0; j < scoreSegNum; j++) {
                for (int k = 0; k < particles[0].length; k++) {
                    ppMat[k][j] = particles[0][k];
                }
            }

            // idx = ppMat>=onsetMat & ppMat<=offsetMat;                       % score indices of each particle

            int[] idx = new int[ppMat.length];

            for (int k = 0; k < idx.length; k++) {
                for (int kk = 0; kk < ppMat[0].length; kk++) {
                    idx[k] = ppMat[k][kk] >= onsetMat[k][kk] & ppMat[k][kk] <= offsetMat[k][kk] ? kk : -1;
                }
            }

            //    for i = 1:parNum
            //             fx(i) = allScoreSegIdx(idx(i,:));
            //    end

            for (int k1 = 0; k1 < parNum; k1++) {
                if (idx[k1] >= 0) {
                    fx[k1] = allScoreIdx[idx[k1]];
                }
            }

            MatrixUtils mu = new MatrixUtils();

            ArrayDeque<Integer> idx1 = new ArrayDeque<Integer>();
            int[] idx2 = new int[fx.length];
            int[] ufx = mu.unique(fx, idx1, idx2);

            int uParNum = idx1.size();

            ///
            unotes = new double[scoreMP.length][uParNum];

            Iterator<Integer> idx1_i = idx1.iterator();

            for (int k = 0; k < scoreMP.length; k++) {
                int k1 = 0;
                while (idx1_i.hasNext()) {
                    unotes[k][k1] = idx1_i.next();
                    k1++;
                }
            }

            ///// _notBool - simply elementwise '!' for array of bools
            //// any - return true if array contains non-zero element
            /// any for 2d array, same but returns true/false for rows/columns (dim: 1/2)

            if (!any(_notBool(any(unotes, 1)))
                    &&
                    (mean(power_elemWise(data, 2)) < rms_Th)
                    ) {

                // xs(:, frameNum + 1) = xs(:, frameNum)
                for (int k = 0; k < xs.length; k++) {
                    xs[k][1] = xs[k][0];
                }
                Random rand = new Random();
                for (int k = 0; k < particles[0].length; k++) {
                    particles[0][k] = particles[0][k] + 0.01 * rand.nextGaussian();
                }

                for (int k = 0; k < particles[0].length; k++) {

                    if (particles[0][k] < minBeat) {
                        particles[0][k] = minBeat;
                    } else if (particles[0][k] > maxBeat) {
                        particles[0][k] = maxBeat;
                    }
                }

                continue;

            } else if (mean(power_elemWise(data, 2)) < rms_Th) {
                chromaAF = new double[nbin];
                chromaAFEnergy = 0;
            } else {

                int fft_len = sample.length;
                ///// TO BE CHANGED TO FFT TASK
                DoubleFFT_1D fft = new DoubleFFT_1D(fft_len); // class that performs FFT - library JTransforms
                double[] spec = new double[sample.length];
                System.arraycopy(data, 0, spec, 0, sample.length);

                fft.realForward(spec, fft_len);

                /////


                try {
                    chromaAF = mult_matrix(ff2cm, abs(spec));
                } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {

                }

                chromaAFEnergy = Math.sqrt(sum(power_elemWise(chromaAF, 2)));
                if (approxEqual(chromaAFEnergy, 0) && chromaAFEnergy != 0) {
                    double factor = 1 / chromaAFEnergy;
                    chromaAF = mul_elemWise(chromaAF, factor);
                }

                if (bSpecSub) {
                    OnsetDetectionRT onsetDet = new OnsetDetectionRT(prevSample, sample, null);
                    this.specFlux = onsetDet.getSpecFlux();
                    this.rmsCur = onsetDet.getRms();
                    double onsetVal = specFlux / rmsCur;

                    if (onsetVal > onset_th) {
                        bFindOnset = 1;
                    }
                    if (bFindOnset == 1) {

                    }

                    if (uParNum == 1) {
                        wx  = new double[parNum];
                        Arrays.fill(wx, 1.0/(double) parNum);
                    } else {
                        uChromaMF = new double[nbin][uParNum];
                        boolean[] idx_0;
                        for (int ii = 0; ii < uParNum; ii++) {
                            notes = MatrixUtils.sliceOf2dArray(unotes, 0, unotes.length, ii, 1.0);
                            notes = retNonZeroElems(notes);

                            idx_0 = nonZeroInxs(notes);


                            if (any(idx_0)){
                                continue;
                            }
                            binind = plusMatrix(remainder(minusMatrix(notes,69), 12 ), 1);
                            for (int iii = 0; iii < notes.length; iii++) {
                                uChromaMF[(int) binind[iii]][ii]++;
                            }

                        }

                        uChromaMFenergy = sqrt_elemWise(sum(uChromaMF,1));
                        idx = nonZeroInxsInt(uChromaMFenergy);
                        try {
                            assign(uChromaMF, 0, uChromaMF.length, idx,
                                    div_elemWise(sliceOf2dArray(uChromaMF, 0, uChromaMF.length, idx),
                                            repmat(sliceOfArray(uChromaMFenergy, idx), nbin, 1)
                                    ));
                        } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {

                        }
                        uwx = new double[uParNum];
                        Arrays.fill(uwx, 0);

                        for (int ii = 0; ii < uParNum; ii++){
                            if (chromaAFEnergy == 0 && uChromaMFenergy[ii] == 0) {
                                angle = 0;
                            } else {
                                angle = Math.cos(sum(mult_elemWise(
                                        sliceOf2dArray(uChromaMF, 0, uChromaMF.length, ii, 1.0),
                                        chromaAF
                                )));
                            }
                            uwx[ii] = Math.exp(-angle*angle);

                        }
                        wx = sliceOfArray(uwx, idx2);
                        wx = mul_elemWise(wx, sum(wx));
                    }

                    idx = new Resample(wx).getInxs();
                    try {

                        // px = px(:, idx) + [0.1*randn(1, parNum); randn(1, parNum)]
                        particles = plusMatrix(sliceOf2dArray(particles, 0, particles.length, idx),
                                               addDimensionToArray(mul_elemWise(randn(parNum), 0.1),
                                                                   randn(parNum),
                                                                   1 ) // 1 - dim
                        );
                    } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {
                        e.printStackTrace();
                    }

                     /*
                    *
                    *
                    * */
                    Object[] inxs = inxsThatSatisfyComparisonRow(particles, minBeat, 1).get(0).toArray();
                    int[] inxs_int = new int[inxs.length];
                    for (int ii = 0; ii < inxs.length; ii++) {
                        inxs_int[ii] = (Integer) inxs[ii];
                    }
                    double[][] toFill = new double[1][inxs.length];
                    Arrays.fill(toFill, minBeat);
                    assign(particles, 0,1,inxs_int, toFill);

                     /*
                    *
                    *
                    * */
                    inxs = inxsThatSatisfyComparisonRow(particles, maxBeat, 2).get(0).toArray();
                    inxs_int = new int[inxs.length];
                    for (int ii = 0; ii < inxs.length; ii++) {
                        inxs_int[ii] = (Integer) inxs[ii];
                    }
                    toFill = new double[1][inxs.length];
                    Arrays.fill(toFill, maxBeat);
                    assign(particles, 0,1,inxs_int, toFill);

                    /*
                    *
                    *
                    * */
                    inxs = inxsThatSatisfyComparisonRow(particles, minBPM, 1).get(1).toArray();
                    inxs_int = new int[inxs.length];
                    for (int ii = 0; ii < inxs.length; ii++) {
                        inxs_int[ii] = (Integer) inxs[ii];
                    }
                    toFill = new double[1][inxs.length];
                    Arrays.fill(toFill, maxBeat);
                    assign(particles, 1,2,inxs_int, toFill);

                    /*
                    *
                    * returns ArrayList<Integer> for each row, then turns it into Object[]
                    * */

                    inxs = inxsThatSatisfyComparisonRow(particles, maxBPM, 2).get(1).toArray();
                    inxs_int = new int[inxs.length];            //  new storage for Object[]
                    for (int ii = 0; ii < inxs.length; ii++) {  // cast
                        inxs_int[ii] = (Integer) inxs[ii];      // to
                    }                                           // integer
                    toFill = new double[1][inxs.length];        // storage values that will replaces cells in particles
                    Arrays.fill(toFill, maxBeat);               // values
                    assign(particles, 1,2,inxs_int, toFill);    // process of replacement, see assign in MatrixUtils


                    xs[0][1] = mean(sliceOf2dArray(particles,0, 0, particles[0].length, true));
                    xs[1][1] = mean(sliceOf2dArray(particles,1, 0, particles[0].length, true));


                    /*
                     *   idx1 = xs(1, fnum) >= scoreSeg(1,:);                            % all the segments that have been passed by previous score position
                     */

                    final double cmpr1 = xs[0][0]; // cmpr1 = xs(1, fnum)
                    final double cmpr2 = xs[0][1]; // cmpr2 = xs(1, fnum+1)

                    DoubleMatrix1D d1 = new DenseDoubleMatrix1D (sliceOf2dArray(scoreSeg, 1, 0, scoreSeg[0].length,true));
                    DoubleMatrix1D idx1_d = d1.copy(); // scoreSeg(1,:)

                    idx1_d.assign(new DoubleFunction() {
                        @Override
                        public double apply(double v) {
                            return v <= cmpr1 ? 1 : 0;
                        }
                    });

                    /*
                     *   idx2 = xs(1, fnum) >= scoreSeg(1,:);                            % all the segments that have been passed by previous score position
                     */

                    DoubleMatrix1D idx2_d = d1.copy();
                    idx2_d.assign(new DoubleFunction() {
                        @Override
                        public double apply(double v) {
                            return v <= cmpr2 ? 1 : 0;
                        }
                    });

                    DenseDoubleMatrix2D px = new DenseDoubleMatrix2D(particles);

                    if (idx2_d.aggregate(Functions.plus, Functions.identity) -
                        idx1_d.aggregate(Functions.plus, Functions.identity) > 0
                            )
                    {
                        double[] ones = new double[parNum];
                        Arrays.fill(ones, 1);

                        assignRow(particles, 1, 0, particles[0].length, plusMatrix(mul_elemWise(ones, xs[1][1]), plusMatrix(randn(parNum), sigma_v)));


                        DoubleMatrix1D px_1 = px.viewRow(1).copy();
                        DoubleMatrix1D px_1_copy = px_1.copy();

                        px_1.assign(new DoubleFunction() {
                            @Override
                            public double apply(double v) {
                                return v < minBPM ? 1 : 0;
                            }
                        });

                        px_1_copy.assign(new DoubleFunction() {
                            @Override
                            public double apply(double v) {
                                return v > maxBPM ? 1 : 0;
                            }
                        });

                        px_1.assign(px_1_copy, new DoubleDoubleFunction() {
                            @Override
                            public double apply(double v, double w) {
                                boolean vb = v == 1 ? true : false;
                                boolean wb = w == 1 ? true : false;
                                return vb || wb == true ? 1 : 0;
                            }
                        });

                        Object[] temp = find(px_1.toArray()).toArray();
                        idx = new int[temp.length];
                        for (int ii = 0; ii < temp.length; ii++) {
                            idx[ii] = (Integer) temp[ii];
                        }

                        assign(particles, 1, idx, plusMatrix(mul_elemWise(rand(idx.length), (maxBPM - minBPM)), minBPM));

                    }
                    DenseDoubleMatrix1D res =  new DenseDoubleMatrix1D(new double[particles.length]);
                    px.zMult(new DenseDoubleMatrix1D(F), res); // res = particles*F

                    particles = px.toArray();

                    inxs = inxsThatSatisfyComparisonRow(particles, maxBeat, 2).get(0).toArray();
                    inxs_int = new int[inxs.length];
                    for (int ii = 0; ii < inxs.length; ii++) {
                        inxs_int[ii] = (Integer) inxs[ii];
                    }
                    toFill = new double[1][inxs.length];
                    Arrays.fill(toFill, maxBeat);
                    assign(particles, 0,1,inxs_int, toFill);



                }


            }


        }

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

    DoubleDoubleFunction sum = new DoubleDoubleFunction() {
        @Override
        public double apply(double v, double v1) {
            return v + v1;
        }

    };



    /*
        * Adapted from code by Dan Ellis, LabROSA, Columbia EE
     */

    public double[][] fft2chromaX(int fftLen, int nbin, int fs, double A440, double f_ctrl_log, double f_std) {


        int[] freqRange = range(1, fftLen - 1); // temp container for frequencies
        double factor = 1 / (fftLen * fs);
        double[] frequencies = mul_elemWiseIntToDouble(freqRange, factor);
        double[] fftFrqBinsTemp = mul_elemWise(hertz2octaves(frequencies, A440), nbin);

        double[] fftFrqBins = new double[fftFrqBinsTemp.length + 1];
        System.arraycopy(fftFrqBinsTemp, 0, fftFrqBins, 1, fftFrqBinsTemp.length);
        fftFrqBins[0] = fftFrqBins[1] - 1.5 * nbin;


        double[][] D = new double[nbin][fftFrqBins.length];
        try {
            D = minusMatrix(repmat(fftFrqBins, nbin, 1),
                    transpose(repmat(
                                    toDoubleArray(range(0, nbin - 1)),
                                    1,
                                    fftLen)
                    )
            );
        } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {

        }

        int nbins2 = Math.round(nbin / 2);

        D = minusMatrix(remainder(
                        plusMatrix(D, (double) nbins2 + 10 * nbin), nbin
                )
                ,
                nbins2); // DOES D CHANGE TYPE? STUPID MATLAB


        double[] maxim = new double[fftLen - 1];

        try {
            maxim = maximum(1,
                    minusMatrix(sliceOfArray(fftFrqBins, 1, fftLen - 1),
                            sliceOfArray(fftFrqBins, 0, fftLen - 2)));

        } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {

        }


        double[] binwidthbins = new double[maxim.length + 1];
        System.arraycopy(maxim, 0, binwidthbins, 0, maxim.length);
        binwidthbins[binwidthbins.length - 1] = 1;

        double[][] wts = new double[D.length][D[0].length];

        try {
            wts = exp(mul_elemWise(
                    div_elemWise(mul_elemWise(D, 2),
                            repmat(binwidthbins, nbin, 1)
                    )
                    , -0.5));
        } catch (Exception e) {
        }

        try {
            wts = div_elemWise(wts,
                    repmat(sum(wts), nbin, 1

                    )
            );
        } catch (MatrixUtils.DimensionsDoNotCorrespondException e) {

        }


        for (int i = 0; i < wts.length; i++) {
            // j <= --- ?????
            for (int j = fftLen / 2 + 2 - 1; j < fftLen; j++) {
                wts[i][j] = 0;
            }
        }


        return wts;
    }


    public double[] hertz2octaves(double[] frequencies, double A440) {

        double[] res = new double[frequencies.length];
        System.arraycopy(frequencies, 0, res, 0, frequencies.length);
        double div = A440 / 16;
        for (int i = 0; i < frequencies.length; i++) {
            res[i] /= div;
        }

        res = log_elemWise(frequencies);

        for (int i = 0; i < frequencies.length; i++) {
            res[i] /= Math.log(2);
        }

        return res;
    }


}