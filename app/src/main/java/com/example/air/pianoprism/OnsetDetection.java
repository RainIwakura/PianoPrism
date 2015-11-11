package com.example.air.pianoprism;

import org.jtransforms.fft.DoubleFFT_1D;


/**
 * Created by rednecked_crake on 10/8/15.
 */
public class OnsetDetection {
    short[] wavDataBuffer;
    int[][] parameters;
    int frameNum;

    int frameLen;
    int frameHop;
    int window[];
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

    double[] specFlux;

    int min (int  x, int y) {
        return x < y ? x : y;
    }


    double rms(double []data) {
        double acc = 0;
        int n = data.length;

        for (int i = 0; i < n; i++) {
            acc += data[i]*data[i];
        }

        acc = Math.sqrt(acc/n);
        return acc;
    }

    public OnsetDetection(short[] wdb, int[][] para, int fnum) {
        this.wavDataBuffer = wdb;
        this.parameters = para;
        this.frameNum = fnum;

        this.frameLen = (int) parameters[0][0];
        this.frameHop = (int) parameters[1][0];

        this.window = parameters[2];
        this.rms_lag = 0;
        this.flux_lag = 4;


        specFlux = new double[2];
        specFlux[0] = 0;
        specFlux[1] = 0;

        startp = 1 + (fnum - 1) * frameHop;
        endp = startp + frameLen - 1;

        int j = 0;

        data = new double[2*(endp - startp)];

        for (int i = startp; i < endp; i++) {
            data[j] = wavDataBuffer[i] * window[j];
            j++;
        }


        startp_flux = 1 + (fnum - 1 - flux_lag) * frameHop;               //data to calculate rms and spectral flux
        startp_rms = 1 + (fnum - 1 - rms_lag) * frameHop;


        fft = new DoubleFFT_1D(data.length);

        double [] fftOut = new double[data.length];

        ///////////

        if (min(startp_flux, startp_rms) < 1) {

            rmsCur = rms(data);
            int n  = data.length;
            // flux = fft(data);
            //  flux = (flux + abs(flux)) / 2;
            //  specFlux = sum(flux);
            // 2k - real
            // 2k+1 - imaginary
            fft.realForward(data);

            for (int i = 0; i < n - 2; i=i+2) {
                data[i] = (data[i] + Math.sqrt(data[i]*data[i] +  data[i+1]*data[i]))/2;
            }

            for (int i = 1; i < n - 2; i= i+2) {
                data[i] = data[i]/2;
            }


            for (int i = 0; i < n; i++) {
                if (i % 2  == 0)
                    specFlux[0] += data[i];
                else
                    specFlux[1] += data[i];

            }

        }

        int endp_flux = startp_flux + frameLen - 1;
        int endp_rms = startp_rms + frameLen - 1;




        //wavData(startp_flux : endp_flux) .* win;
        double[] data_flux = new double[2*(endp_flux - startp_flux)];
        int k = 0;
        for (int i = startp_flux; i < endp_flux; i++) {
            data_flux[k] = this.wavDataBuffer[i]*window[k];
        }

        double[] data_rms = new double[2*(endp_rms - startp_rms)];
        int l = 0;

        for (int i = startp_rms; i < endp_rms; i++) {
            data_rms[l] = this.wavDataBuffer[i]*window[k];
        }

        rmsCur = rms(data_rms);


        ///////////////


        double[] spec = new double[2*wavDataBuffer.length];

        for (int i = 0; i < wavDataBuffer.length; i++) {
            spec[i] = wavDataBuffer[i];
        }

        fft.complexForward(spec);

        for (int i = 0; i < wavDataBuffer.length - 2; i=i+2) {
            spec[i] = Math.sqrt(spec[i]*spec[i] + spec[i+1]*spec[i+1]);
        }



        //////


        double[] specPrev = new double[2*data_flux.length];

        for (int i = 0; i < data_flux.length; i++) {
            specPrev[i] = data_flux[i];
        }

        fft.complexForward(specPrev);

        for (int i = 0; i < data_flux.length - 2; i=i+2) {
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




        /*
                % calculate spectral flux

        spec_prev = abs(fft(data_flux));

        gamma = 0.2;                                                % logarithmic compression
        spec= log(1 + gamma*abs(spec));
        spec_prev= log(1 + gamma*abs(spec_prev));

        flux = spec - spec_prev;
        flux = (flux+abs(flux))/2;
        specFlux = sum(flux);

        */

    }


    public double[] getOnsetDetection() {

        double[] res = new double[3];
        res[0] = rmsCur;
        res[1] = specFlux[0];
        res[2] = specFlux[1];

        return res;
    }

}
