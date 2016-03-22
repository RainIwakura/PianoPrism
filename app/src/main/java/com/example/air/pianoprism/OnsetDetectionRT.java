package com.example.air.pianoprism;

import org.jtransforms.fft.DoubleFFT_1D;

import static com.example.air.pianoprism.MatrixUtils.hammingWin;
import static com.example.air.pianoprism.MatrixUtils.sum;


/**
 * Created by rednecked_crake on 10/8/15.
 */
public class OnsetDetectionRT {
    short[] wavDataBuffer;
    int[][] parameters;
    int frameNum;

    int frameLen;
    int frameHop;
    int window[];
    double[] win;
    int rms_lag;
    int flux_lag;
    int startp;
    int endp;
    double[] data;

    int startp_rms;
    int startp_flux;

    double rmsCur;
    double[] flux;
    DoubleFFT_1D fft;

    double specFlux;

    int min (int  x, int y) {
        return x < y ? x : y;
    }


    double rms(double[] data) {
        double acc = 0;
        int n = data.length;

        for (int i = 0; i < n; i++) {
            acc += data[i]*data[i];
        }

        acc = Math.sqrt(acc/n);
        return acc;
    }

    public OnsetDetectionRT(double[] pastSample, double[] curSample, int[][] para) {
        this.parameters = para;

        this.window = parameters[2];
        this.rms_lag = 0;
        this.flux_lag = 4;

        win = hammingWin(curSample.length, "periodic");



        fft = new DoubleFFT_1D(curSample.length);


        ///////////
        data = new double[curSample.length];
        System.arraycopy(curSample, 0, data, 0, curSample.length);


        /*  */
        for (int i = 0; i < curSample.length; i++) {
            data[i] = data[i]*win[i];
        }
        rmsCur = rms(data);


        ///////////////


        double[] spec = new double[2*curSample.length];
        System.arraycopy(curSample, 0, spec, 0, curSample.length);

        for (int i = 0; i < spec.length; i++) {
            spec[i] *= win[i];
        }
        fft.complexForward(spec);

        for (int i = 0; i < curSample.length - 2; i=i+2) {
            spec[i] = Math.sqrt(spec[i]*spec[i] + spec[i+1]*spec[i+1]);
        }



        //////


        double[] specPrev = new double[2*pastSample.length];

        System.arraycopy(pastSample, 0, specPrev, 0, pastSample.length);
        for (int i = 0; i < specPrev.length; i++) {
            specPrev[i] *= win[i];
        }

        fft.complexForward(specPrev);

        for (int i = 0; i < pastSample.length - 2; i=i+2) {
            specPrev[i] = Math.sqrt(specPrev[i]*specPrev[i] + specPrev[i+1]*specPrev[i+1]);
        }


        //////////

        double gamma = 0.2;

        for (int i = 0;  i < spec.length - 2; i=i+2) {
            spec[i] = Math.log(1 + gamma*spec[i]);
        }


        for (int  i = 0; i < specPrev.length - 2; i=i+2) {
            specPrev[i] = Math.log(1 + gamma*specPrev[i]);
        }


        int ii = 0;

        flux = new double[spec.length];

        for (int i = 0; i < spec.length - 2; i=i-2) {
            flux[ii] = spec[i] - specPrev[i];
            ii++;
        }


        for (int i = 0; i < flux.length; i++) {
            flux[i] = (flux[i] + Math.abs(flux[i]))/2;
        }

        specFlux = sum(flux);


    }


    public double getRms() {
        return rmsCur;
    }


    public double getSpecFlux() {
        return specFlux;
    }

}
