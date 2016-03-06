package com.example.air.pianoprism;


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




import static com.example.air.pianoprism.MatrixUtils._notBool;
import static com.example.air.pianoprism.MatrixUtils.any;
import static com.example.air.pianoprism.MatrixUtils.any;
import static com.example.air.pianoprism.MatrixUtils.div_elemWise;
import static com.example.air.pianoprism.MatrixUtils.exp;
import static com.example.air.pianoprism.MatrixUtils.log_elemWise;
import static com.example.air.pianoprism.MatrixUtils.maximum;
import static com.example.air.pianoprism.MatrixUtils.minusMatrix;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWise;
import static com.example.air.pianoprism.MatrixUtils.mul_elemWiseIntToDouble;
import static com.example.air.pianoprism.MatrixUtils.plusMatrix;
import static com.example.air.pianoprism.MatrixUtils.range;
import static com.example.air.pianoprism.MatrixUtils.power_elemWise;
import static com.example.air.pianoprism.MatrixUtils.randInt;
import static com.example.air.pianoprism.MatrixUtils.mean;
import static com.example.air.pianoprism.MatrixUtils.hammingWin;
import static com.example.air.pianoprism.MatrixUtils.remainder;
import static com.example.air.pianoprism.MatrixUtils.repmat;
import static com.example.air.pianoprism.MatrixUtils.sliceOfArray;
import static com.example.air.pianoprism.MatrixUtils.toDoubleArray;
import static com.example.air.pianoprism.MatrixUtils.transpose;
import static com.example.air.pianoprism.MatrixUtils.sum;

/**
 * Created by rednecked_crake on 2/6/16.
 *  TODO: GOTTA CHECK ALL INDICES
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
    int fftLen = (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(frameLen*zpf - 1));

    double[] win = hammingWin(fftLen, "periodic");
    //////////////////


    /// parameters for chroma features

    int nbin = 12;

    /// parameters for process model
    int parNum = 1000;


    /// bSpecSub = 0 or 1
    boolean bSpecSub = false;



    /*
     * frameNum - calculated from user-inputted time of practice
     */

    public DoScoFo (double[][] nmat, int frameNum, int fs, double[] sample) {


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
        MidiSegObject midiSeg = fms.findMidiSeg();

        int scoreSegNum = midiSeg.scoreSeg[0].length;

        // allScoreIdx = 1:scoreSegNum
        Integer[] allScoreIdx = new Integer[scoreSegNum];
        for (int i = 0; i < scoreSegNum; i++) {
            allScoreIdx[i] = i;
        }

        double timeLen = 120 * 1000;

        /// xs = zeroes (2, frameNum + 1)
        double[][] xs = new double[2][frameNum+1];
        Arrays.fill(xs[0], 0);
        Arrays.fill(xs[1], 0);


        int frameHopMin = frameHop/fs/60;

        int[] F = {1, frameHopMin, 0, 1};

        double[][] scoreSeg = midiSeg.scoreSeg;
        double[][] scoreMP =  midiSeg.scoreMP;

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

        double[] chromaAF;
        double chromaAFEnergy;

       for (int i =  0;  i < frameNum; i++) {
            int startp           = 1 + (i - 1)*frameHop;
            int endp             = startp + frameLen - 1;
            double[] data        = new double[endp - startp];
            double[][] particles = new double[2][parNum];
            double[] x_init      =  {minBeat, scoreTempo};;

            int jj = 0;
            for (int j = startp; j < endp; j++) {
                data[jj] = sample[j]*win[jj];
            }

            if (bStart == 0) {
                if (mean(power_elemWise(data, 2)) < rms_Th) {
                    continue;
                } else {
                    bStart = 1;


                    for (int k = 0; k < xs.length; k++) {
                        xs[k][i] = x_init[k]; // ??????????????????
                    }


                    for (int k = 0; k < parNum; k++) {
                        particles[1][k] = minBeat;
                    }

                    for (int k = 0; k < parNum; k++) {
                        Random rand = new Random ();
                        particles[2][k] = randInt(1, frameNum) * (maxBPM - minBPM) + minBPM ;
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
                for (int kk = 0;  kk < ppMat[0].length; kk++) {
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
            int[]               idx2 = new int[fx.length];
            int[]               ufx  = mu.unique(fx, idx1, idx2);

            int uParNum = idx1.size();

           ///
            double[][] unotes = new double[scoreMP.length][uParNum];

            Iterator<Integer> idx1_i = idx1.iterator();

            for (int k = 0; k < scoreMP.length; k++) {
                int k1 = 0;
                while (idx1_i.hasNext()) {
                    unotes[k][k1] =  idx1_i.next();
                    k1++;
                }
            }

            ///// _notBool simply elementwise '!' for array of bools
            //// any - return true if array contains non-zero element
            /// any for 2d array, same but returns true/false for rows/columns (dim: 1/2)

            if (!any(_notBool(any(unotes, 1)))
                    &&
                (mean(power_elemWise(data, 2)) < rms_Th)
               )
            {

                // xs(:, frameNum + 1) = xs(:, frameNum)
                for (int k = 0; k < xs.length; k++) {
                    xs[i][frameNum + 1] = xs[i][frameNum];
                }

                for (int k = 0; k < particles[0].length; k++) {
                    particles[0][k] = particles[0][k] + 0.01*randInt(1, parNum);
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
                double [] spec = new double[sample.length];
                System.arraycopy(data, 0, spec, 0, sample.length);

                fft.realForward(spec, fft_len);

                //////






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


        int[] freqRange = range(1,fftLen-1); // temp container for frequencies
        double factor = 1/(fftLen*fs);
        double[] frequencies = mul_elemWiseIntToDouble(freqRange, factor);
        double[] fftFrqBinsTemp  = mul_elemWise(hertz2octaves(frequencies, A440), nbin);

        double[] fftFrqBins = new double[fftFrqBinsTemp.length + 1];
        System.arraycopy(fftFrqBinsTemp, 0, fftFrqBins, 1, fftFrqBinsTemp.length);
        fftFrqBins[0] = fftFrqBins[1] - 1.5*nbin;


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

        int nbins2 = Math.round(nbin/2);

        D = minusMatrix( remainder(
                                   plusMatrix(D, (double) nbins2 + 10*nbin), nbin
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
                               repmat(sum(wts),nbin,1

                  )
            );
        } catch ( MatrixUtils.DimensionsDoNotCorrespondException e) {

        }


        for (int i = 0; i < wts.length; i++) {
            // j <= --- ?????
            for (int j = fftLen/2 + 2 - 1; j < fftLen; j++) {
                wts[i][j] = 0;
            }
        }



        return wts;
    }


    public double[] hertz2octaves(double[] frequencies, double A440) {

        double[] res = new double[frequencies.length];
        System.arraycopy(frequencies, 0, res, 0, frequencies.length);
        double div = A440/16;
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