package io.kuenzler.android.lateagain.control;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.control.crawler.Crawler;
import io.kuenzler.android.lateagain.control.util.DateCalculator;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.5
 */
public class RequestLoopAsyncTask extends AsyncTask<String, String, ArrayList<Departure>> {

    private final MainActivity mMain;
    private final int mRefreshRate = 10000; //refresh every 10s
    private String mStart, mLine, mTime, mDate, mFilter;
    private int mDepartureIndex, mCurrent;
    private DateCalculator mDc;

    /**
     * @param main MainActivity object
     */
    public RequestLoopAsyncTask(MainActivity main) {
        mMain = main;
        mDepartureIndex = -1;
        mCurrent = 0;
    }

    @Override
    protected ArrayList<Departure> doInBackground(String[] params) {
        publishProgress("Submitting request");
        Crawler crawler = null; //Crawler.getCrawler(this);
        mDate = params[0];
        mTime = params[1];
        mStart = params[2];
        mLine = params[3];
        mFilter = params[4];
        ArrayList<Departure> departures = crawler.getDepartures(mDate, mTime, mStart, mLine, mFilter);
        if (departures == null || departures.isEmpty()) {
            return null;
        }
        publishProgress("Request successful");
        String oldStart = mStart; // TODO just for log output
        mStart = departures.get(0).getLocStart();
        Log.i("LateAgain", "Given start: " + oldStart + ", corrected Start: " + mStart);
        //mLine = departures.get(0).getLocDestination();
        return departures;
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        // mMain.updateSpinner(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Departure> values) {
        // mMain.updateSpinner("");
        // mMain.showDeps(values);
    }
}