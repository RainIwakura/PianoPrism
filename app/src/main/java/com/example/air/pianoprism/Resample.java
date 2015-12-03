package com.example.air.pianoprism;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import java.io.*;

/**
 * Created by rednecked_crake on 11/18/15.
 */
public class Resample<T> {

    double[] weights;
    int[] inxs;



    public Resample (double[] wghts) {
        this.weights = Arrays.copyOf(wghts, wghts.length);

        double sum = 0;

        /*
         *
         *  W = W ./ sum(W);
         *  W = cumsum(W);
         *
         */
        double sum_w  = sum(weights);

        System.out.println("sum: " + sum_w + "\n\n\n");
        System.out.println("factor: " + 1.0/sum_w + "\n\n\n");
        mult_arr(weights, 1.0/sum_w);

        cumsum(weights);





        /*
         *
         *  R = sort(rand(1,size(W,2)));
         *
         */

        double[] r = randDistribution(weights.length);
        Arrays.sort(r);




        printArr(r);


        /*
         *
         *  B1 = [zeros(1,size(W,2)) ones(1,size(R,2))];
         *  [tmp,I] = sort([W R]);
         *
         */

        int [] b1 = new int[weights.length + r.length];

        Arrays.fill(b1, 0, weights.length, 0);
        Arrays.fill(b1, weights.length, weights.length + r.length,  1);


        /*
         *     [tmp,I] = sort([W R]);
         */
        double[] wr = new double[weights.length + r.length];

        assign(wr, weights, 0,              weights.length             );
        assign(wr, r,       weights.length, (weights.length + r.length));


        int[] indices = new int[wr.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }



        quicksort(wr, indices);

        /*
         *  B1 = B1(I);  
         */
        int[] b1_copy = Arrays.copyOf(b1,b1.length);

        b1 = new int[indices.length];
        for (int i = 0; i < b1.length; i++) {
            b1[i] = b1_copy[indices[i]];
        }


        /*
         *    B2 = [0 B1(1:end-1).*(B1(2:end)==0)]; % Places where R turns into W
         *    I2 = find(B2);                        % Corresponding indices
         *
         */
        int[] b2 = new int[b1.length];

        b2[0]    =  0;
        int j    =  0;
        int k   =  1;



        for (int i = 1; i < b2.length; i++) {
            b2[i] = b1[j] * (b1[k] == 0 ? 1 : 0);

            j++;
            k++;
        }



        Integer[] i2 = find(b2);

        /*
         *   WI1 = cumsum(B2);    % Indices to W's through I2 cumulatively
         */



        int[] WI1 = Arrays.copyOf(b2, b2.length);

        cumsum(WI1);


        /*
         *  WI2 = (B1.*(WI1+1)); % Indices to W's after R's placed over R's
         */

        int[] WI2 = new int[b1.length];

        for (int i = 0; i < WI2.length; i++) {
            WI2[i] = b1[i] * (WI1[i] + 1);
        }

        /*   
         *   ind = WI2~=0;        % We want only nonzero indices
         *   WI2 = WI2(ind);
         *   WI3 = I2(WI2);       % Indices to actual W's in S
         *   ind = I(WI3);        % Indices before sorting
         */


        double[] ind = new double[WI2.length];

        ArrayDeque<Integer> wi3 = new ArrayDeque<Integer>();


        for (int i = 0; i < ind.length; i++) {
            if (WI2[i] != 0) {
                wi3.add(i2[WI2[i] - 1]);
            }
        }


        Integer[] wi3_arr = new Integer[wi3.size()];
        wi3.toArray(wi3_arr);

        inxs = new int[wi3.size()];
        for (int i = 0; i < wi3.size(); i++) {
            inxs[i] = indices[wi3_arr[i]];
        }

    }



    /*
     * GETTERS
     *
     */



    public double[] getWeights() {
        double [] retValue  = Arrays.copyOf(weights,weights.length);

        return retValue;
    }


    public int[] getInxs() {
        int[] retValue = Arrays.copyOf(inxs, inxs.length);
        return retValue;
    }











    


    /*
     * PRINT ARR
     *
     *
     */

    public void printArr(Integer[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print("]\n");

    }



    public void printArr(int[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print("]\n");

    }

    public void printArr(double[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print("]\n");

    }





    /*
     * FIND
     *
     *
     */


    public Integer[] find (double[] arr) {
        ArrayDeque<Integer> indices = new ArrayDeque<>();
        for (int i = 0;  i < arr.length; i++) {
            if (arr[i] != 0)
                indices.add(i);
        }

        Integer[] res = new Integer[indices.size()];
        indices.toArray(res);
        return res;
    }


    public Integer[] find (int[] arr) {
        ArrayDeque<Integer> indices = new ArrayDeque<>();
        for (int i = 0;  i < arr.length; i++) {
            if (arr[i] != 0)
                indices.add(i);
        }

        Integer[] res = new Integer[indices.size()];
        indices.toArray(res);
        return res;
    }


    public Integer[] find (double[][] arr) {
        ArrayDeque<Integer> indices = new ArrayDeque<>();
        for (int i = 0;  i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                if (arr[i][j] != 0)
                    indices.add(i+j);
            }
        }

        Integer[] res = new Integer[indices.size()];
        indices.toArray(res);
        return res;
    }


    public Integer[] find (int[][] arr) {
        ArrayDeque<Integer> indices = new ArrayDeque<>();
        for (int i = 0;  i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                if (arr[i][j] != 0)
                    indices.add(i+j);
            }
        }

        Integer[] res = new Integer[indices.size()];
        indices.toArray(res);
        return res;
    }



    /*
     * CUMSUM
     *
     *
     */


    public void cumsum(int[] arr) {
        for (int i = 1;  i < arr.length; i++) {
            arr[i] += arr[i - 1];
        }
    }

    public void cumsum(double[] arr) {
        for (int i = 1;  i < arr.length; i++) {
            arr[i] += arr[i - 1];
        }
    }



    /*
     * SUM
     *
     *
     */


    public int sum (int[] arr) {
        int res = 0;
        for (int i = 0; i < arr.length; i++) {
            res += arr[i];
        }
        return res;
    }

    public double sum (double[] arr) {
        double res = 0;
        for (int i = 0; i < arr.length; i++) {
            res += arr[i];
        }
        return res;
    }







    /*
     * ADD ARR
     *
     *
     */


    public void add_arr(double[] arr, double operand) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] += operand;
        }
    }

    public void add_arr(int[] arr, int operand) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] += operand;
        }
    }



    /*
     * MULT ARR
     *
     *
     */


    public void mult_arr(double[] arr, double factor) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= factor;
        }
    }

    public void mult_arr(int[] arr, double factor) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= factor;
        }
    }



    public double[] randDistribution(int n) {
        double[] res = new double[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            res[i] = rand.nextDouble();
        }

        return res;
    }

    // Assigns arr2 values to arr1 from startIndex to endInxed

    public void assign (double[] arr1, double[] arr2, int startIndex, int endIndex) {
        int j = 0;
        for (int i = startIndex; i < endIndex; i++) {
            arr1[i] = arr2[j];
            j++;
        }
    }




     /*
     * Tuning parameters.
     */

    /**
     * If the length of an array to be sorted is less than this
     * constant, insertion sort is used in preference to Quicksort.
     */
    private static final int INSERTION_SORT_THRESHOLD = 32;


    /**
     * Sorts the specified range of the array into ascending order by the
     * Dual-Pivot Quicksort algorithm.
     *
     * @param a the array to be sorted
     * @param left the index of the first element, inclusive, to be sorted
     * @param right the index of the last element, inclusive, to be sorted
     */
    private static void dualPivotQuicksort(double[] a, int [] inx, int left, int right) {
        // Compute indices of five evenly spaced elements
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; // The midpoint
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;

        // Sort these elements using a 5-element sorting network
        double ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];

        //////////////////////
        int ae1_inx = inx[e1];
        int ae2_inx = inx[e2];
        int ae3_inx = inx[e3];
        int ae4_inx = inx[e4];
        int ae5_inx = inx[e5];
        //////////////////////

        if (ae1 > ae2) {
            double t = ae1;    ae1 = ae2;          ae2 = t;
            int ti = ae1_inx;  ae1_inx = ae2_inx;  ae2_inx = ti;
        }
        if (ae4 > ae5) {
            double t = ae4;    ae4 = ae5;          ae5 = t;
            int ti = ae4_inx;  ae4_inx = ae5_inx;  ae5_inx = ti;
        }
        if (ae1 > ae3) {
            double t = ae1;   ae1 = ae3;         ae3 = t;
            int ti = ae1_inx; ae1_inx = ae3_inx; ae3_inx = ti;
        }
        if (ae2 > ae3) {
            double t = ae2;   ae2 = ae3;          ae3 = t;
            int ti = ae2_inx; ae2_inx = ae3_inx;  ae3_inx = ti;
        }
        if (ae1 > ae4) {
            double t = ae1;   ae1 = ae4;         ae4 = t;
            int ti = ae1_inx; ae1_inx = ae4_inx; ae4_inx = ti;
        }
        if (ae3 > ae4) {
            double t = ae3;   ae3 = ae4;         ae4 = t;

            int ti = ae3_inx; ae3_inx = ae4_inx; ae4_inx = ti;
        }
        if (ae2 > ae5) {
            double t = ae2;
            ae2 = ae5;
            ae5 = t;


            int ti = ae2_inx;
            ae2_inx = ae5_inx;
            ae5_inx = ti;
        }
        if (ae2 > ae3) {
            double t = ae2;
            ae2 = ae3;
            ae3 = t;


            int ti = ae2_inx;
            ae2_inx = ae3_inx;
            ae3_inx = ti;
        }
        if (ae4 > ae5) {
            double t = ae4;
            ae4 = ae5;
            ae5 = t;



            int ti = ae4_inx;
            ae4_inx = ae5_inx;
            ae5_inx = ti;
        }




        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;

        //  inx[e1] = inx[ae1_inx]; inx[e3] = inx[ae3_inx]; inx[e5] = inx[ae5_inx];
        inx[e1] = ae1_inx; inx[e3] = ae3_inx; inx[e5] = ae5_inx;



        /*
         * Use the second and fourth of the five sorted elements as pivots.
         * These values are inexpensive approximations of the first and
         * second terciles of the array. Note that pivot1 <= pivot2.
         *
         * The pivots are stored in local variables, and the first and
         * the last of the elements to be sorted are moved to the locations
         * formerly occupied by the pivots. When partitioning is complete,
         * the pivots are swapped back into their final positions, and
         * excluded from subsequent sorting.
         */
        double pivot1 = ae2;    a[e2] = a[left];
        double pivot2 = ae4;    a[e4] = a[right];

        int piv1_inx = ae2_inx; inx[e2] = inx[left];
        int piv2_inx = ae4_inx; inx[e4] = inx[right];

        // Pointers
        int less  = left  + 1; // The index of first element of center part
        int great = right - 1; // The index before first element of right part

        boolean pivotsDiffer = (pivot1 != pivot2);

        if (pivotsDiffer) {

            outer:
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                int ak_i = inx[k];
                if (ak < pivot1) { // Move a[k] to left part
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;

                        inx[k] = inx[less];
                        inx[less] = ak_i;
                    }
                    less++;
                } else if (ak > pivot2) { // Move a[k] to right part
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        inx[k] = inx[less];


                        inx[less] = inx[great];
                        a[less++] = a[great];


                        inx[great] = ak_i;
                        a[great--] = ak;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                        inx[k] = inx[great];

                        inx[great] = ak_i;
                        a[great--] = ak;

                    }
                }
            }
        } else { // Pivots are equal

            for (int k = less; k <= great; k++) {
                double ak = a[k];
                int ak_i = inx[k];

                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { // Move a[k] to left part
                    if (k != less) {
                        a[k] = a[less];
                        inx[k] = inx[less];
                        a[less] = ak;
                        inx[less] = ak_i;
                    }
                    less++;
                } else { // (a[k] > pivot1) -  Move a[k] to right part
                    /*
                     * We know that pivot1 == a[e3] == pivot2. Thus, we know
                     * that great will still be >= k when the following loop
                     * terminates, even though we don't test for it explicitly.
                     * In other words, a[e3] acts as a sentinel for great.
                     */
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        inx[k] = inx[less];


                        inx[less] = inx[great];
                        a[less++] = a[great];

                        inx[great] = ak_i;
                        a[great--] = ak;
                    } else { // a[great] == pivot1
                        a[k] = pivot1;
                        inx[k] = inx[piv1_inx];

                        inx[great] = ak_i;
                        a[great--] = ak;
                    }
                }
            }
        }

        // Swap pivots into their final positions
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;   inx[left] = inx[less - 1]; inx[less - 1] = inx[piv1_inx];
        a[right] = a[great + 1]; a[great + 1] = pivot2;   inx[right] = inx[great + 1]; inx[great +1] = inx[piv2_inx];

        // Sort left and right parts recursively, excluding known pivot values
        doSort(a, inx, left,   less - 2);
        doSort(a, inx, great + 2, right);

        /*
         * If pivot1 == pivot2, all elements from center
         * part are equal and, therefore, already sorted
         */
        if (!pivotsDiffer) {
            return;
        }

        /*
         * If center part is too large (comprises > 2/3 of the array),
         * swap internal pivot values to ends
         */
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }

            outer:
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                int ak_i = inx[k];
                if (ak == pivot2) { // Move a[k] to right part
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        inx[k] = inx[less];

                        inx[less] = inx[piv1_inx];
                        a[less++] = pivot1;
                    } else { // pivot1 < a[great] < pivot2
                        a[k] = a[great];
                        inx[k] = inx[great];
                    }

                    inx[great] = inx[piv2_inx];
                    a[great--] = pivot2;
                } else if (ak == pivot1) { // Move a[k] to left part
                    a[k] = a[less];
                    inx[k] = inx[less];

                    inx[less] = inx[piv1_inx];
                    a[less++] = pivot1;
                }
            }
        }

        // Sort center part recursively, excluding known pivot values
        doSort(a, inx, less, great);
    }




    public static int binarySearch(short[] array, int startIndex, int endIndex, short value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            short midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    private static void checkBinarySearchBounds(int startIndex, int endIndex, int length) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException();
        }
        if (startIndex < 0 || endIndex > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }


    public static void checkStartAndEnd(int len, int start, int end) {
        if (start < 0 || end > len) {
            throw new ArrayIndexOutOfBoundsException("start < 0 || end > len."
                    + " start=" + start + ", end=" + end + ", len=" + len);
        }
        if (start > end) {
            throw new IllegalArgumentException("start > end: " + start + " > " + end);
        }
    }



    /**
     * Returns the index of some zero element in the specified range via
     * binary search. The range is assumed to be sorted, and must contain
     * at least one zero.
     *
     * @param a the array to be searched
     * @param low the index of the first element, inclusive, to be searched
     * @param high the index of the last element, inclusive, to be searched
     */
    private static int findAnyZero(double[] a, int low, int high) {
        while (true) {
            int middle = (low + high) >>> 1;
            double middleValue = a[middle];

            if (middleValue < 0.0d) {
                low = middle + 1;
            } else if (middleValue > 0.0d) {
                high = middle - 1;
            } else { // middleValue == 0.0d
                return middle;
            }
        }
    }

    /**
     * Sorts the specified range of the array into ascending order. This
     * method differs from the public {@code sort} method in three ways:
     * {@code right} index is inclusive, it does no range checking on
     * {@code left} or {@code right}, and it does not handle negative
     * zeros or NaNs in the array.
     *
     * @param a the array to be sorted, which must not contain -0.0d and NaN
     * @param left the index of the first element, inclusive, to be sorted
     * @param right the index of the last element, inclusive, to be sorted
     */
    private static void doSort(double[] a, int[] inx, int left, int right) {
        // Use insertion sort on tiny arrays
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                double ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else { // Use Dual-Pivot Quicksort on large arrays
            dualPivotQuicksort(a, inx, left, right);
        }
    }


    /**
     * Sorts the specified array into ascending numerical order.
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * @param a the array to be sorted
     */
    public static void sort(double[] a, int[] inx) {
        sortNegZeroAndNaN(a, inx, 0, a.length - 1);
    }

    /**
     * Sorts the specified range of the array into ascending order. The range
     * to be sorted extends from the index {@code fromIndex}, inclusive, to
     * the index {@code toIndex}, exclusive. If {@code fromIndex == toIndex},
     * the range to be sorted is empty (and the call is a no-op).
     *
     * <p>The {@code <} relation does not provide a total order on all double
     * values: {@code -0.0d == 0.0d} is {@code true} and a {@code Double.NaN}
     * value compares neither less than, greater than, nor equal to any value,
     * even itself. This method uses the total order imposed by the method
     * {@link Double#compareTo}: {@code -0.0d} is treated as less than value
     * {@code 0.0d} and {@code Double.NaN} is considered greater than any
     * other value and all {@code Double.NaN} values are considered equal.
     *
     * @param a the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     * @throws IllegalArgumentException if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     if {@code fromIndex < 0} or {@code toIndex > a.length}
     */
    public static void sort(double[] a, int[] inx, int fromIndex, int toIndex) {
        checkStartAndEnd(a.length, fromIndex, toIndex);
        sortNegZeroAndNaN(a,inx, fromIndex, toIndex - 1);
    }

    /**
     * Sorts the specified range of the array into ascending order. The
     * sort is done in three phases to avoid expensive comparisons in the
     * inner loop. The comparisons would be expensive due to anomalies
     * associated with negative zero {@code -0.0d} and {@code Double.NaN}.
     *
     * @param a the array to be sorted
     * @param left the index of the first element, inclusive, to be sorted
     * @param right the index of the last element, inclusive, to be sorted
     */
    private static void sortNegZeroAndNaN(double[] a, int[] inx, int left, int right) {
        /*
         * Phase 1: Count negative zeros and move NaNs to end of array
         */
        final long NEGATIVE_ZERO = Double.doubleToLongBits(-0.0d);
        int numNegativeZeros = 0;
        int n = right;

        for (int k = left; k <= n; k++) {
            double ak = a[k];
            if (ak == 0.0d && NEGATIVE_ZERO == Double.doubleToRawLongBits(ak)) {
                a[k] = 0.0d;
                numNegativeZeros++;
            } else if (ak != ak) { // i.e., ak is NaN
                a[k--] = a[n];
                a[n--] = Double.NaN;
            }
        }

        /*
         * Phase 2: Sort everything except NaNs (which are already in place)
         */
        doSort(a,inx, left, n);

        /*
         * Phase 3: Turn positive zeros back into negative zeros as appropriate
         */
        if (numNegativeZeros == 0) {
            return;
        }

        // Find first zero element
        int zeroIndex = findAnyZero(a, left, n);

        for (int i = zeroIndex - 1; i >= left && a[i] == 0.0d; i--) {
            zeroIndex = i;
        }

        // Turn the right number of positive zeros back into negative zeros
        for (int i = zeroIndex, m = zeroIndex + numNegativeZeros; i < m; i++) {
            a[i] = -0.0d;
        }
    }



    public static void quicksort(double[] main, int[] index) {
        quicksort(main, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(double[] a, int[] index, int left, int right) {
        if (right <= left) return;
        int i = partition(a, index, left, right);
        quicksort(a, index, left, i-1);
        quicksort(a, index, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(double[] a, int[] index,
                                 int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[++i], a[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[right], a[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, index, i, j);               // swap two elements into place
        }
        exch(a, index, i, right);               // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(double x, double y) {
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(double[] a, int[] index, int i, int j) {
        double swap = a[i];
        a[i] = a[j];
        a[j] = swap;
        int b = index[i];
        index[i] = index[j];
        index[j] = b;
    }

}
