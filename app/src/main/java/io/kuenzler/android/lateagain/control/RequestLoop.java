package io.kuenzler.android.lateagain.control;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.model.Departure;

/**
 *
 */
public class RequestLoop extends Thread {

    private Crawler mCrawler;
    private final MainActivity mMain;
    private final int mRefreshRate = 10000; //refresh 10 s
    private String mStart, mDest;
    private int mDepartureIndex;
    private DateCalculator dc;
    private int mCurrent;

    /**
     * @param main
     */
    public RequestLoop(MainActivity main) {
        mMain = main;
        mDepartureIndex = 0;
        mCurrent = 0;
    }

    /**
     * @param start
     * @param dest
     */
    public ArrayList<Departure> getDepartures(String start, String dest) {
        mStart = start;
        mDest = dest;
        mCrawler = new Crawler(this);
        Log.i("LateAgain", "Start: " + start + ", Dest: " + dest);
        ArrayList<Departure> departures = mCrawler.getDepartures(null, null, mStart, mDest);
        if (departures.isEmpty()) {
            return null;
        }
        mStart = departures.get(0).getLocStart();
        mDest = departures.get(0).getLocDestination();
        return departures;
    }

    public void run() {
        dc = new DateCalculator();
        ArrayList<Departure> departures;
        Departure departure;
        while (!isInterrupted()) {
            departures = mCrawler.getDepartures(null, null, mStart, mDest);
            try {
                departure = departures.get(mDepartureIndex);
            } catch (NullPointerException e) {
                Log.e("LateAgain", "Expected departure " + mDepartureIndex + " not there.", e);
                return;
            }
            mCurrent++;
            long distance = dc.countToDeparture(departure);
            countDown(distance, mCurrent);
            try {
                sleep(mRefreshRate);
            } catch (InterruptedException e) {
                mCurrent = -1;
                interrupt();
            }
        }
    }

    /**
     * @param index
     */
    public void setDepartureIndex(int index) {
        mDepartureIndex = index;
    }

    public void countDown(final long distance, final int current) {
        Log.i("LateAgain", "Started Timer "+current);
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long counterValue = distance;
            int currentValue = current;
            public void run() {
                if (current != mCurrent) {
                    timer.cancel();
                    Log.i("LateAgain", "Dropped Timer "+currentValue);
                }
                String[] notData = dc.printDate(counterValue);
                mMain.createNotification(notData[0], notData[1]);
                counterValue -= 1000;
                if (counterValue <= 0) {
                    timer.cancel();
                    interrupt();
                }
            }
        }, 0, 1000);
    }

}
