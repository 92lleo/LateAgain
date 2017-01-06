package io.kuenzler.android.lateagain.control.crawler;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;

import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.control.util.StringSimilarity;
import io.kuenzler.android.lateagain.model.Departure;

/**
 * @author Leonhard Künzler
 * @version 0.5
 * @date 17.02.16
 */
public class DBCrawler extends Crawler {

    private final static String DB_URL = "http://mobile.bahn.de/bin/mobil/bhftafel.exe/dox?ld=15061&rt=1&use_realtime_filter=1&webview=&";

    public DBCrawler(RequestLoop reqLoop) {
        super(reqLoop, DB_URL);
    }

    /**
     * @param date
     * @param time
     * @param start
     * @param dest
     * @return
     */
    public ArrayList<Departure> getDepartures(String date, String time, String start, String dest, String filter) {
        this.start = start;
        this.dest = dest;
        this.date = date;
        this.time = time;
        //Todo
        setTestData();
        sendRequest();
        try {
            cleanAndParseResults();
        } catch (NoSuchElementException e) {
            //mReqLoop.getmMain().showToast("No departure in near future!");
            Log.e("LateAgain", e.toString());
        }
        return departures;
    }

    /**
     * Testdata for use without gui
     */
    protected void setTestData() {
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
    }

    /**
     * Cleans results and parses alternative start and dest locations, sets them
     * when possible
     */
    protected boolean cleanAndParseAlternativeLocations() {
        boolean changed = true;
        while (mDocument == null) {
            //wait for request
        }
        if (!mDocument.title().contains("Abfahrt/Ankunft")) {
            throw new NoSuchElementException(
                    "Did expect drop down dialog, found site " + mDocument.title());
        }
        // get alternatives for start (select[class=sqproduct)
        Elements alternatives = mDocument.select("select[class=sqproduct").get(0).children();
        ArrayList<String> alternativeLocations = new ArrayList<>();
        //TODO System.out.println("--Alternatives to " + start);
        for (Element x : alternatives) {
            System.out.println(x.text()); // TODO
            alternativeLocations.add(x.text());
        }
        if (!alternativeLocations.isEmpty()) {
            mReqLoop.getAlternativeLocations(alternativeLocations, 0);
            //TODO start
            //TODO automative - start = getBestAlternative(start, alternativeLocations);
        }
        return changed;
    }

    /**
     * Returns String with highest similarity to location string
     *
     * @param location     Location string
     * @param alternatives Alternative strings from db site
     * @return best matching string from array
     */
    protected String getBestAlternative(String location, ArrayList<String> alternatives) {
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
     * Cleans the document and parses departures. Expects Query to be sent.
     * 1. blocks until mDocument document is present (change)
     */
    protected void cleanAndParseResults() {
        if(mDocument == null){
            throw new NoSuchElementException(
                    "No departures in near future!");
        }
        if (mDocument.text().contains("Ihre Eingabe ist nicht eindeutig")) {
            if (cleanAndParseAlternativeLocations()) {
                departures = null;//TODO
                return;
            }
            mDocument = null;
            sendRequest();
        }
        if (mDocument == null || mDocument.text().contains("Keine aktuellen Informationen verfügbar.")) {
            throw new NoSuchElementException(
                    "No departures in near future!");
        }
        Elements information = mDocument.select("div[class=fline stdpadding").get(0).children();
        String info = information.first().text();
        String newStart;
        try {
            newStart = info.substring(0, info.indexOf(" - ")).trim();
        } catch (Exception e) {
            newStart = info;
        }
        if (newStart != null) {
            start = newStart;
        }
        Log.i("LateAgainStart", start);
        mReqLoop.setCorrectedLocations(start, dest);
        Elements departureList;
        try {
            departureList = mDocument.select("div[class=clicktable").get(0).children();
        } catch (IndexOutOfBoundsException e) {
            Log.e("LateAgain", "No departures with that line");
            return;
        }

        departures = new ArrayList<>();
        for (Element departureTable : departureList) {
            Departure dep = new Departure();
            String delay = departureTable.select("span[class=okmsg").text();
            if (delay.trim().isEmpty()) {
                delay = departureTable.select("span[class=red").text();
            }
            if (delay.trim().isEmpty()) {
                delay = "-0";
            }
            if (delay.trim().contains("ca")) {
                delay = delay.substring(3).trim();
            }
            dep.setLocStart(start);
            dep.setDelay(delay);
            Elements classBold = departureTable.select("span[class=bold");
            dep.setType(classBold.get(0).text().replace(" ", ""));
            dep.setTimeStart(classBold.get(1).text());
            String ownText = departureTable.ownText();
            String platform, target;
            if (ownText.contains(",  Gl.")) {
                target = ownText.substring(2, (ownText.indexOf(",  Gl.") - 1)).trim();
                platform = ownText.substring(ownText.indexOf("Gl.") + 3).trim();
            } else if (ownText.contains("Gl.")) {
                target = ownText.substring(2, (ownText.indexOf("Gl.") - 3)).trim();
                platform = ownText.substring(ownText.indexOf("Gl.") + 3).trim();
            } else if (ownText.contains("k.A.")) {
                target = ownText.substring(2, (ownText.indexOf("k.A.") - 1)).trim();
                platform = "-";
            } else {
                target = ownText.substring(2, ownText.length() - 1).trim();
                platform = "-";
            }
            dep.setPlatform(platform);
            dep.setLocDestination(target);
            dep.setReady(false);
            departures.add(dep);
        }
    }

    /**
     * prepare and send request to mBahnUrl and save output in mDocument
     */

    protected void sendRequest() {
        Log.i("LateAgainRequest", "Trying input:" + start + ", date:" + date + ", time:" + time);
        //TODO: Thread is just there to bypass androids main thread network activity watch
        Thread t = new Thread() {
            public void run() {
                try {
                    mDocument = Jsoup
                            .connect(mBahnUrl)
                            //start location
                            .data("input", start)
                            .data("inputRef", "#")
                            // date
                            .data("date", date)
                            // time
                            .data("time", time)
                            .data("productsFilter", "1111111111000000") //TODO check filters
                            .data("REQTrain_name", dest) //TODO change
                            .data("maxJourneys", "10") //todo fewer?
                            .data("boardType", "Abfahrt")
                            .data("ao", "yes").data("start", "Suchen")
                            .userAgent("Mozilla").post();
                } catch (UnknownHostException e) {
                    Log.e("LateAgain", "No connection!!");
                } catch (IOException | NoSuchElementException e) {
                    Log.e("LateAgain", "Request failed:\n" + e.toString());
                } catch (IllegalArgumentException e) {
                    Log.e("LateAgain", "Request failed: Nullvalue in post");
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}