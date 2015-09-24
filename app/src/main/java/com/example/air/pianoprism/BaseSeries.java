/*
package com.example.air.pianoprism;

import android.graphics.PointF;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {
    private final List<E> mData = new ArrayList();
    private Map<PointF, E> mDataPoints = new HashMap();
    private String mTitle;
    private int mColor = -16746548;
    protected OnDataPointTapListener mOnDataPointTapListener;
    private List<GraphView> mGraphViews = new ArrayList();

    public BaseSeries() {
    }

    public BaseSeries(E[] data) {
        DataPointInterface[] var2 = data;
        int var3 = data.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            DataPointInterface d = var2[var4];
            this.mData.add(d);
        }

    }

    public double getLowestValueX() {
        return this.mData.isEmpty()?0.0D:((DataPointInterface)this.mData.get(0)).getX();
    }

    public double getHighestValueX() {
        return this.mData.isEmpty()?0.0D:((DataPointInterface)this.mData.get(this.mData.size() - 1)).getX();
    }

    public double getLowestValueY() {
        if(this.mData.isEmpty()) {
            return 0.0D;
        } else {
            double l = ((DataPointInterface)this.mData.get(0)).getY();

            for(int i = 1; i < this.mData.size(); ++i) {
                double c = ((DataPointInterface)this.mData.get(i)).getY();
                if(l > c) {
                    l = c;
                }
            }

            return l;
        }
    }

    public double getHighestValueY() {
        if(this.mData.isEmpty()) {
            return 0.0D;
        } else {
            double h = ((DataPointInterface)this.mData.get(0)).getY();

            for(int i = 1; i < this.mData.size(); ++i) {
                double c = ((DataPointInterface)this.mData.get(i)).getY();
                if(h < c) {
                    h = c;
                }
            }

            return h;
        }
    }

    public Iterator<E> getValues(final double from, final double until) {
        return from <= this.getLowestValueX() && until >= this.getHighestValueX()?this.mData.iterator():new Iterator() {
            Iterator<E> org;
            E nextValue;
            E nextNextValue;
            boolean plusOne;

            {
                this.org = BaseSeries.this.mData.iterator();
                this.nextValue = null;
                this.nextNextValue = null;
                this.plusOne = true;
                boolean found = false;
                DataPointInterface prevValue = null;
                if(this.org.hasNext()) {
                    prevValue = (DataPointInterface)this.org.next();
                }

                if(prevValue.getX() >= from) {
                    this.nextValue = prevValue;
                    found = true;
                } else {
                    while(this.org.hasNext()) {
                        this.nextValue = (DataPointInterface)this.org.next();
                        if(this.nextValue.getX() >= from) {
                            found = true;
                            this.nextNextValue = this.nextValue;
                            this.nextValue = prevValue;
                            break;
                        }

                        prevValue = this.nextValue;
                    }
                }

                if(!found) {
                    this.nextValue = null;
                }

            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public E next() {
                if(this.hasNext()) {
                    DataPointInterface r = this.nextValue;
                    if(r.getX() > until) {
                        this.plusOne = false;
                    }

                    if(this.nextNextValue != null) {
                        this.nextValue = this.nextNextValue;
                        this.nextNextValue = null;
                    } else if(this.org.hasNext()) {
                        this.nextValue = (DataPointInterface)this.org.next();
                    } else {
                        this.nextValue = null;
                    }

                    return r;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasNext() {
                return this.nextValue != null && (this.nextValue.getX() <= until || this.plusOne);
            }
        };
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public void setOnDataPointTapListener(OnDataPointTapListener l) {
        this.mOnDataPointTapListener = l;
    }

    public void onTap(float x, float y) {
        if(this.mOnDataPointTapListener != null) {
            DataPointInterface p = this.findDataPoint(x, y);
            if(p != null) {
                this.mOnDataPointTapListener.onTap(this, p);
            }
        }

    }

    protected E findDataPoint(float x, float y) {
        float shortestDistance = 0.0F / 0.0;
        DataPointInterface shortest = null;
        Iterator var5 = this.mDataPoints.entrySet().iterator();

        while(true) {
            Entry entry;
            float distance;
            do {
                if(!var5.hasNext()) {
                    if(shortest != null && shortestDistance < 120.0F) {
                        return shortest;
                    }

                    return null;
                }

                entry = (Entry)var5.next();
                float x1 = ((PointF)entry.getKey()).x;
                float y1 = ((PointF)entry.getKey()).y;
                distance = (float)Math.sqrt((double)((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y)));
            } while(shortest != null && distance >= shortestDistance);

            shortestDistance = distance;
            shortest = (DataPointInterface)entry.getValue();
        }
    }

    protected void registerDataPoint(float x, float y, E dp) {
        this.mDataPoints.put(new PointF(x, y), dp);
    }

    protected void resetDataPoints() {
        this.mDataPoints.clear();
    }

    public void resetData(E[] data) {
        this.mData.clear();
        DataPointInterface[] var2 = data;
        int gv = data.length;

        for(int var4 = 0; var4 < gv; ++var4) {
            DataPointInterface d = var2[var4];
            this.mData.add(d);
        }

        this.checkValueOrder((DataPointInterface)null);
        Iterator var6 = this.mGraphViews.iterator();

        while(var6.hasNext()) {
            GraphView var7 = (GraphView)var6.next();
            var7.onDataChanged(true, false);
        }

    }

    public void onGraphViewAttached(GraphView graphView) {
        this.mGraphViews.add(graphView);
    }

    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints) {
        this.checkValueOrder(dataPoint);
        if(!this.mData.isEmpty() && dataPoint.getX() < ((DataPointInterface)this.mData.get(this.mData.size() - 1)).getX()) {
            throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
        } else {
            List keepLabels = this.mData;
            synchronized(this.mData) {
                int curDataCount = this.mData.size();
                if(curDataCount < maxDataPoints) {
                    this.mData.add(dataPoint);
                } else {
                    this.mData.remove(0);
                    this.mData.add(dataPoint);
                }
            }

            boolean keepLabels1 = this.mData.size() != 1;
            Iterator curDataCount1 = this.mGraphViews.iterator();

            while(curDataCount1.hasNext()) {
                GraphView gv = (GraphView)curDataCount1.next();
                gv.onDataChanged(keepLabels1, scrollToEnd);
                if(scrollToEnd) {
                    gv.getViewport().scrollToEnd();
                }
            }

        }
    }

    public boolean isEmpty() {
        return this.mData.isEmpty();
    }

    protected void checkValueOrder(DataPointInterface onlyLast) {
        if(this.mData.size() > 1) {
            if(onlyLast != null) {
                if(onlyLast.getX() < ((DataPointInterface)this.mData.get(this.mData.size() - 1)).getX()) {
                    throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
                }
            } else {
                double lx = ((DataPointInterface)this.mData.get(0)).getX();

                for(int i = 1; i < this.mData.size(); ++i) {
                    if(((DataPointInterface)this.mData.get(i)).getX() != 0.0D / 0.0) {
                        if(lx > ((DataPointInterface)this.mData.get(i)).getX()) {
                            throw new IllegalArgumentException("The order of the values is not correct. X-Values have to be ordered ASC. First the lowest x value and at least the highest x value.");
                        }

                        lx = ((DataPointInterface)this.mData.get(i)).getX();
                    }
                }
            }
        }

    }
}
*/
