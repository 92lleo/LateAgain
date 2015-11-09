package io.kuenzler.android.lateagain.control;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.5
 * @date 09.11.15 22:00 cleanup + doc
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
     * @param main MainActivity object
     */
    public RequestLoop(MainActivity main) {
        mMain = main;
        mDepartureIndex = 0;
        mCurrent = 0;
    }

    /**
     * Sets local variables (locations, times) and gets possible departures from Crawler
     *
     * @param start
     * @param dest
     */
    public ArrayList<Departure> getDepartures(String start, String dest) {
        // TODO times has to be passed, too (testdata for now)
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

    /**
     * Starts crawler loop and manages countdown
     */
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
     * Set wanted departure index
     *
     * @param index departure index
     */
    public void setDepartureIndex(int index) {
        //TODO index changes with time :( fix!
        mDepartureIndex = index;
    }

    /**
     * Counts down to departure and prints time to activity
     *
     * @param distance distance to count down to
     * @param current  current countdown, cancels if != current
     */
    public void countDown(final long distance, final int current) {
        Log.i("LateAgain", "Started Timer " + current);
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long counterValue = distance;
            int currentValue = current;

            public void run() {
                if (current != mCurrent) {
                    timer.cancel();
                    Log.i("LateAgain", "Dropped Timer " + currentValue);
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
