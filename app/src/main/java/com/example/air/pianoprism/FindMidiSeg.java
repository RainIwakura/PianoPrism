package com.example.air.pianoprism;

import android.util.Log;

import com.android.internal.util.Predicate;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import mikera.vectorz.*;

/**
 * Created by Air on 7/6/2015.
 * author:Almas Abdibayev
 */
public class FindMidiSeg {

    DenseDoubleMatrix2D dm;
    static double cmpr;
    int scoreMPsize = 32;

    public FindMidiSeg(double[][] nmat) {
        dm = new DenseDoubleMatrix2D(nmat);
    }

    public MidiSegObject findMidiSeg() {

        cern.colt.matrix.doublealgo.Formatter format = new cern.colt.matrix.doublealgo.Formatter();

        DoubleMatrix1D dm1 = dm.viewColumn(0).copy();
        DoubleMatrix1D dm2 = dm.viewColumn(1).copy();
        MatrixUtils<Double> mu = new MatrixUtils<Double>();

        dm2 = dm1.copy().assign(dm2, plus);



        ArrayDeque<Integer> segIdx = new ArrayDeque<>();

        double[] onsets_d = mu.unique(dm1.copy().toArray(), segIdx);
        double[] offsets_d = mu.unique(dm2.copy().toArray(), null);

        DoubleMatrix1D onsets = new DenseDoubleMatrix1D(onsets_d);
        DoubleMatrix1D offsets = new DenseDoubleMatrix1D(offsets_d);


        DenseDoubleMatrix2D onsets_t = mu.transposeOf1D(onsets);
        DenseDoubleMatrix2D offsets_t = mu.transposeOf1D(offsets);


        DenseDoubleMatrix2D scoreSeg = new DenseDoubleMatrix2D(new double[2][onsets_t.columns()]);


        IntArrayList list = new IntArrayList(new int[]{1});
        int[] longIntArr = new int[onsets_t.columns()];

        for (int i = 0; i < onsets_t.columns(); i++) {
            longIntArr[i] = i;
        }

        IntArrayList longList = new IntArrayList(longIntArr);


        int segnum = onsets_d.length;

        double[][] scrSg = new double[2][segnum];

        // Log.d("prog", "onsets_d len: " + onsets_d.length +  " offsets_d len: " + offsets_d.length);

        for (int i = 0; i < 2; i++) {
            int k = 1;
            for (int j = 0; j < segnum; j++) {
                if (i == 0) {
                    scrSg[i][j] = onsets_d[j];
                } else {
                    if (j < segnum - 1) {
                        scrSg[i][j] = onsets_d[k];
                    } else {
                        scrSg[i][j] = offsets_d[offsets_d.length - 1];
                    }
                }
            }
        }

        double[] scoreMP_init = new double[segnum];
        double[][] scoreMP = new double[scoreMPsize][segnum];

        /// FILL WITH ZEROES --> zeros(512, segnum)


        for (int i = 0; i < scoreMPsize; i++) {
            for (int j = 0; j < segnum; j++) {
                scoreMP[i][j] = 0;
            }
        }


        double[][] scoreMP_t = new double[segnum][scoreMPsize];



        Arrays.fill(scoreMP_init, 0);


        DoubleMatrix1D temp1 = dm.viewColumn(0).copy();
        DoubleMatrix1D temp11 = dm.viewColumn(0).copy();

        DoubleMatrix1D temp2 = dm.viewColumn(1).copy();
        temp2.assign(temp1, plus);


        int scorempActLen = 0;

        for (int i = 0; i < segnum; i++) {

            this.cmpr = scrSg[0][i];

            temp1.assign(dm1.copy(), lessCmpr);

            temp11.assign(dm1.copy(), equalCmpr);

            temp1.assign(temp11, or);

            temp2.assign(dm2.copy(), moreCmpr);

            temp1.assign(temp2, and);

            ////////
            //        Log.d("temp1111", format.toString(temp1));
            ///////


            IntArrayList inxs = new IntArrayList();
            temp1.getNonZeros(inxs, null);


            for (int j = 0; j < inxs.size(); j++) {
                scoreMP[j][i] = dm.viewColumn(3).copy().get(inxs.get(j));
                scoreMP_t[i][j] = scoreMP[j][i];
            }

            if (inxs.size() > scorempActLen)
                scorempActLen = inxs.size();
        }


        for (int i = 0;  i < segnum; i++) {
              Arrays.sort(scoreMP_t[i]);
              for (int j = scoreMPsize -1; j > scoreMPsize - 1 - scorempActLen; j--) {
                  scoreMP[scoreMPsize - j - 1][i] = scoreMP_t[i][j];
              }
        }

        
        return new MidiSegObject(scoreMP,scrSg,segIdx);
    }


    DoubleDoubleFunction plus = new DoubleDoubleFunction() {
        public double apply(double a, double b) {
            return a + b;
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

    public class Indices {
        ArrayDeque<Integer> idx;
        ArrayDeque<Integer[]> idx_arr;
        boolean isArr = false;

        Indices(ArrayDeque<Integer> idx) {
            this.idx = idx;
        }

        Indices(ArrayDeque<Integer[]> idx_arr, boolean isArr) {
            this.idx_arr = idx_arr;
            this.isArr = true;
        }

        public int length() {
            if (isArr) {
                return idx_arr.size();
            } else
                return idx.size();
        }

    }


    public double[] castObjectArrTodouble(Object[] arr) {
        double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (double) arr[i];
        }

        return result;
    }

    public Double[] castObjectArrToDoubleArr(Object[] arr) {
        Double[] result = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (Double) arr[i];
        }

        return result;
    }

    public Double[] fromdoubleToDouble(double[] arr) {
        Double[] result = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (Double) arr[i];
        }

        return result;
    }


    // public scoreMP(double[][])




    /*
      System.out.println("[");
        for (int i = 0; i < scorempActLen; i++) {
            for (int j = 0; j < scoreMP[0].length; j++) {
                DecimalFormat df = new DecimalFormat("00.00");
                System.out.print(df.format(scoreMP[i][j]) + " ");
            }
            System.out.print("\n");
        }
        System.out.println("]\n");
     */



}
