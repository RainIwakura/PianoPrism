package com.example.air.pianoprism;

/**
 * Created by rednecked_crake on 11/12/15.
 */
public  class CalculateCondProb
{


    public static double returnCondProb(double[] midi_chroma, double[] audio_chroma) {
        double res = returnAngle(midi_chroma, audio_chroma);
        res = Math.exp(-res*res);

        res = res/2.50662827463; // divide by sqrt(2*PI)

        return res;
    }


    public static double returnAngle(double[] midi_chroma, double[] audio_chroma) {

        double res       = 0;

        double numerator = 0;
        double midi_len  = 0;
        double audio_len = 0;

        for (int  i = 0;  i < midi_chroma.length; i++) {
                numerator += midi_chroma[i]*audio_chroma[i];
                midi_len  += midi_chroma[i]*midi_chroma[i];
                audio_len += audio_chroma[i]*audio_chroma[i];

        }

        midi_len  = Math.sqrt(midi_len);
        audio_len = Math.sqrt(audio_len);

        res = (numerator)/(midi_len*audio_len);

        res = Math.acos(res);

        return res;
    }


}
