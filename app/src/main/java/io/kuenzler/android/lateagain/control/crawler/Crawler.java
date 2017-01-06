package io.kuenzler.android.lateagain.control.crawler;

import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.control.util.StringSimilarity;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.1
 * @date 17.02.16
 */
public abstract class Crawler {

    private final static int NO_NET = 0;
    private final static int DB = 1;
    private final static int MVG = 2;

    //TODO: what is really needed here?
    protected final RequestLoop mReqLoop;
    protected final String mBahnUrl;
    protected Document mDocument;
    protected String date, time, start, dest;
    protected ArrayList<Departure> departures;


    /**
     * Creates crawler object with corresponding loop given
     */
    public Crawler(RequestLoop reqLoop, String url) {
        this.mReqLoop = reqLoop;
        mBahnUrl = url;
        mDocument = null;
    }

    public static Crawler getCrawler(RequestLoop requestLoop) {
        int currentTransportNet = 0;
        //currentTransportNet = requestLoop.getCurrentTransportNet();
        switch (currentTransportNet) {
            case NO_NET:
                return new DBCrawler(requestLoop);
            case DB:
                return new DBCrawler(requestLoop);
            case MVG:
                return new MVGCrawler(requestLoop);
            default:
                return new DBCrawler(requestLoop);
        }
    }

    /**
     * Returns a list of departures for given data
     *
     * @param date
     * @param time
     * @param start
     * @param dest
     * @return
     * @see Departure
     */
    public abstract ArrayList<Departure> getDepartures(String date, String time, String start, String dest, String filter);

    /**
     * Cleans results and parses alternative start and dest locations, sets them
     * when possible
     */
    protected abstract boolean cleanAndParseAlternativeLocations(); //should be private

    /**
     * Cleans the document and parses departures. Expects Query to be sent.
     * Triggers search for alternative locations if unsuccessful
     */
    protected abstract void cleanAndParseResults(); //shoud be private

    /**
     * prepare and send request to mBahnUrl and save output in mDocument
     */
    protected abstract void sendRequest(); //shoud be private

    /**
     * Returns String with highest similarity to location string
     *
     * @param location     Location string
     * @param alternatives Alternative strings from db site
     * @return best matching string from array
     */
    private String getBestAlternative(String location, ArrayList<String> alternatives) {
        double currentDistance, distance = 0;
        String result = "";
        for (String s : alternatives) {
            currentDistance = StringSimilarity
                    .getSimilarityPercent(location, s);
            if (currentDistance > distance) {
                distance = currentDistance;
                result = s;
            }
        }
        return result;
    }

    /**
     * Testdata for use without gui //TODO: delete when no longer used
     */
    private void setTestData() {
        Date today = new Date();
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.GERMANY);
        if (this.date == null) {
            date = formatter.format(today);
        }
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        if (this.time == null) {
            time = ft.format(today);
        }
        //TODO start = "Eching";
        //dest = "Feldmoching";
        //System.out.println(date + "-" + time + ", " + start + " to " + dest);
    }
}