package io.kuenzler.android.lateagain.control.crawler;

import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * Crawler for munich mvg (tram, bus, ubahn).
 *
 * @author Leonhard Künzler
 * @version 0.1
 * @date 17.02.16
 */
public class MVGCrawler extends Crawler {

    public static final String MVG_URL = "http://www.mvg-live.de/MvgLive/MvgLive.jsp";
    //http://www.mvg-live.de/MvgLive/MvgLive.jsp#haltestelle=Eching&gehweg=10&zeilen=7&ubahn=true&bus=true&tram=true&sbahn=false

    public MVGCrawler(RequestLoop requestLoop) {
        super(requestLoop, MVG_URL);
    }

    @Override
    public ArrayList<Departure> getDepartures(String date, String time, String start, String dest, String filter) {
        return null;
    }

    @Override
    protected boolean cleanAndParseAlternativeLocations() {
        //TODO use db alternative locations here and recheck if they exist in the mvg net
        return false;
    }

    @Override
    protected void cleanAndParseResults() {

    }

    @Override
    protected void sendRequest() {
        Log.i("LateAgainRequest", "Trying input:" + start + ", date:" + date + ", time:" + time);
        final String request;
        String tmpRequest = mBahnUrl + "#";
        //http://www.mvg-live.de/MvgLive/MvgLive.jsp#haltestelle=Eching&gehweg=10&zeilen=7&ubahn=true&bus=true&tram=true&sbahn=false
        tmpRequest += "haltestelle=";
        tmpRequest += "Eching"; //TODO: start here
        tmpRequest += "gehweg=";
        tmpRequest += "0"; //TODO: time to get to station (let be 0) here
        tmpRequest += "zeilen=";
        tmpRequest += "7"; //TODO: lines to show here (7-10, max 15)
        tmpRequest += "ubahn";
        tmpRequest += "true"; //TODO: show undergrounds here
        tmpRequest += "bus";
        tmpRequest += "true"; //TODO: show buses here
        tmpRequest += "tram";
        tmpRequest += "true"; //TODO: show trams here
        tmpRequest += "sbahn";
        tmpRequest += "false"; //TODO: show sbahn here (false, dbcrawler should do that)
        request = tmpRequest;
        new Thread() {
            public void run() {
                try {
                    mBahn = Jsoup
                            .connect(request)
                            .userAgent("Mozilla").post();
                } catch (UnknownHostException e) {
                    Log.e("LateAgain", "No connection!!");
                } catch (IOException | NoSuchElementException e) {
                    Log.e("LateAgain", "Request failed:\n" + e.toString());
                } catch (IllegalArgumentException e) {
                    Log.e("LateAgain", "Request failed: Nullvalue in post");
                }
            }
        }.start();
    }
}
