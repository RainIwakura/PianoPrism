package com.example.air.pianoprism;

import com.jjoe64.graphview.series.DataPointInterface;

 /**
 * Created by Air on 7 21 2015.
 */
public class DataPointFaster implements DataPointInterface {

        double x;
        double y;

        DataPointFaster (double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public double getX() {
            return this.x;
        }

        @Override
        public double getY() {
            return this.y;
        }

        public void setX(double x){
            this.x = x;
        }

        public void setY(double y){
            this.y = y;
        }
}

