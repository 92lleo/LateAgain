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

    private Crawler2 mCrawler;
    private final MainActivity mMain;
    private final int mRefreshRate = 10000; //refresh 10 s
    private String mStart, mDest, mTime, mDate;
    private int mDepartureIndex;
    private DateCalculator dc;
    private int mCurrent;
    //private ArrayList<String> alternatives;

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
        mCrawler = new Crawler2(this);
        Log.i("LateAgain", "Start: " + start + ", Dest: " + dest);
        ArrayList<Departure> departures = mCrawler.getDepartures(null, null, mStart, mDest);
        if (departures == null || departures.isEmpty()) {
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
        Departure departure = null;
        boolean firstRound = true;
        while (!isInterrupted()) {
            if (departure != null && mTime == null) {
                mTime = departure.getTimeStart();
                //TODO calculate time failsafe (23-1:00)
                mDate = dc.getCurrentDate();
            } else if (firstRound) {
                mTime = null;
                mDate = null;
            }

            departures = mCrawler.getDepartures(mDate, mTime, mStart, mDest);
            String type, platform;
            try {
                departure = departures.get(mDepartureIndex);
                type = departure.getType();
                platform = departure.getPlatform();
            } catch (NullPointerException e) {
                Log.e("LateAgain", "Expected departure " + mDepartureIndex + " not there.", e);
                return;
            }

            Log.i("LateAgainX", "Old: " + mTime + " , new: " + departure.getTimeStart());
            if (!firstRound && !mTime.equals(departure.getTimeStart())) {
                //TODO time problem goes here
                int oldIndex = mDepartureIndex;
                if (mDepartureIndex > 0) {
                    mDepartureIndex--;
                }
                Log.i("LateAgainX", "decreased index from " + oldIndex + " to " + mDepartureIndex);
                //skip contdown stuff until right one is found
                continue;
            }
            firstRound = false;
            mCurrent++;
            long distance = dc.countToDeparture(departure);
            countDown(distance, mCurrent, type, platform);
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
    public void countDown(final long distance, final int current, final String type, final String platform) {
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
                String[] notificationData = dc.printDate(counterValue);
                mMain.createNotification(notificationData[0], notificationData[1], type, platform);
                counterValue -= 1000;
                if (counterValue <= 0) {
                    timer.cancel();
                    interrupt();
                }
            }
        }, 0, 1000);
    }

    private boolean hasDelayChanged() {
        //TODO int or departure?
        return false;
    }

    /**
     * @param whichLoc
     * @param location
     */
    public void setAlternativeLocation(int whichLoc, String location) {
        if (whichLoc == 0) {
            mStart = location;
        } else if (whichLoc == 1) {
            mDest = location;
        }
    }

    /**
     * @param alternativeLocations
     * @param whichLoc
     */
    public void getAlternativeLocations(ArrayList<String> alternativeLocations, int whichLoc) {
        mMain.getAlternativeLocations(alternativeLocations, whichLoc);
    }

    public void setCorrectedLocations(String start, String dest) {
        mMain.setCorrectedLocations(start, dest);
    }

    /**
     *
     * @return
     */
    public MainActivity getmMain() {
        return mMain;
    }
}
