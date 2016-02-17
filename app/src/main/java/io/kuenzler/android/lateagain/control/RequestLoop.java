package io.kuenzler.android.lateagain.control;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.control.crawler.Crawler;
import io.kuenzler.android.lateagain.control.crawler.DBCrawler;
import io.kuenzler.android.lateagain.control.crawler.GenericCrawler;
import io.kuenzler.android.lateagain.control.util.DateCalculator;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.5
 */
public class RequestLoop extends Thread {

    private final MainActivity mMain;
    private final int mRefreshRate = 10000; //refresh every 10s
    private String mStart, mLine, mTime, mDate, mFilter;
    private int mDepartureIndex, mCurrent;
    private DateCalculator mDc;

    /**
     * @param main MainActivity object
     */
    public RequestLoop(MainActivity main) {
        mMain = main;
        mDepartureIndex = -1;
        mCurrent = 0;
    }

    /**
     * Sets local variables (locations, times) and gets possible departures from Crawler
     *
     * @param start
     * @param line
     */
    public ArrayList<Departure> getDepartures(String date, String time, String start, String line, String filter) {
        Crawler crawler = GenericCrawler.getCrawler(this);
        mDate = date;
        mTime = time;
        mStart = start;
        mLine = line;
        mFilter = filter;
        ArrayList<Departure> departures = crawler.getDepartures(mDate, mTime, mStart, mLine, mFilter);
        if (departures == null || departures.isEmpty()) {
            return null;
        }
        String oldStart = mStart; // TODO just for log output
        mStart = departures.get(0).getLocStart();
        Log.i("LateAgain", "Given start: " + oldStart + ", corrected Start: " + mStart);
        //mLine = departures.get(0).getLocDestination();
        return departures;
    }

    /**
     * Starts crawler loop and manages countdown
     */
    public void run() {
        mCurrent = 0;
        mDc = new DateCalculator();
        ArrayList<Departure> departures;
        Departure departure;
        DBCrawler crawler;
        int i = 0;
        while (!isInterrupted()) {
            crawler = new DBCrawler(this);
            i++;
            Log.i("LateAgain RequestLoop", "#" + i);
            departures = crawler.getDepartures(mDate, mTime, mStart, mLine, mFilter);

            try {
                departure = departures.get(mDepartureIndex);
                Log.e("LateAgain2", departure.toString());
            } catch (NullPointerException e) {
                Log.e("LateAgain", "Expected departure " + mDepartureIndex + " not there.", e);
                return;
            }
            Log.i("LateAgainTime (RL)", "Old: " + mTime + " , new: " + departure.getTimeStart());
            /*if (!(mTime == null) && !mTime.isEmpty() && !mTime.equals(departure.getTimeStart())) {
                //TODO: time problem goes here (fixed)
                int oldIndex = mDepartureIndex;
                if (mDepartureIndex > 0) {
                    mDepartureIndex--;
                }
                Log.e("LateAgainBugNow", "decreased index from " + oldIndex + " to " + mDepartureIndex);
                //skip contdown stuff until right one is found
                continue;
            }*/
            mCurrent++;
            long distance = mDc.countToDeparture(departure);
            countDown(distance, mCurrent, departure);
            try {
                Log.d("LateAgainD", "RequestLoop #" + i + " tries to sleep now");
                sleep(mRefreshRate);
            } catch (InterruptedException e) {
                mCurrent = -1;
                interrupt();
            }
        }
        //stop timer by setting mCurrent to invalid value
        mCurrent = -1;
        interrupt(); //needed?
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
    public void countDown(final long distance, final int current, final Departure departure) {
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
                String[] notificationData = mDc.printDate(counterValue);
                mMain.createNotification(notificationData[0], notificationData[1], departure);
                counterValue -= 1000;
                if (counterValue <= 0) {
                    timer.cancel();
                    interrupt();
                }
            }
        }, 0, 1000);
    }

    /**
     * @param whichLoc
     * @param location
     */
    public void setAlternativeLocation(int whichLoc, String location) {
        if (whichLoc == 0) {
            mStart = location;
        } else if (whichLoc == 1) {
            mLine = location;
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
     * @return
     */
    public MainActivity getmMain() {
        return mMain;
    }
}
